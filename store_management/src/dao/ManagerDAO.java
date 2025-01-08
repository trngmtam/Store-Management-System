import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ManagerDAO {
    private Connection connection;
    private MongoCollection<Document> ordersCollection;

    public ManagerDAO() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        MongoDatabase database = MongoDBConnection.getDatabase();
        ordersCollection = database.getCollection("order");
    }

    public Map<String, Integer> extractSalesData() {
        String query = "SELECT productID, SUM(quantity) as totalQuantity FROM orderDetails GROUP BY productID";
        Map<String, Integer> salesData = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
             ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String productID = rs.getString("productID");
                int totalQuantity = rs.getInt("totalQuantity");
                salesData.put(productID, totalQuantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesData;
    }

    public void updateRecentCustomers() {
        try (Jedis jedis = RedisDB.getJedis()) {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> orderCollection = database.getCollection("order");

            // Fetch customerID and orderDate from MongoDB
            FindIterable<Document> orders = orderCollection.find();
            for (Document order : orders) {
                Integer customerID = order.getInteger("customerID");  // customerID as Integer
                Date orderDate = order.getDate("orderDate");

                if (customerID != null && orderDate != null) {
                    // Convert customerID to String
                    String customerIDStr = customerID.toString();

                    // Convert orderDate to UNIX timestamp
                    long timestamp = orderDate.getTime();

                    // Store the customerID (as String) and the timestamp in Redis sorted set
                    jedis.zadd("recent_customer", timestamp, customerIDStr);
                }
            }
            System.out.println("Recent customers updated in Redis.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getRecentCustomerDetails() {
        List<String[]> customerDetails = new ArrayList<>();
        try (Jedis jedis = RedisDB.getJedis()) {
            MongoDatabase mongoDatabase = MongoDBConnection.getDatabase();
            MongoCollection<Document> customerCollection = mongoDatabase.getCollection("customer");

            updateRecentCustomers();
            List<Tuple> recentCustomers = jedis.zrevrangeWithScores("recent_customer", 0, -1);

            for (Tuple customer : recentCustomers) {
                String customerID = customer.getElement();
                Date orderDate = new Date((long) customer.getScore());

                int ID = Integer.valueOf(customerID);
                Document customerDoc = customerCollection.find(Filters.eq("customerID", ID)).first();
                if (customerDoc != null) {
                    String customerName = customerDoc.getString("customerName");
                    String customerPhone = customerDoc.getString("customerPhone");
                    String customerAddress = customerDoc.getString("customerAddress");

                    customerDetails.add(new String[]{
                            customerID, customerName, customerPhone, customerAddress, orderDate.toString()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerDetails;
    }

    public void updateMonthlyEarnings() {
        try (Jedis jedis = RedisDB.getJedis()) {
            MongoDatabase database = MongoDBConnection.getDatabase();
            MongoCollection<Document> orderCollection = database.getCollection("order");

            // Fetch all orders from MongoDB
            FindIterable<Document> orders = orderCollection.find();
            for (Document order : orders) {
                double totalAmount = order.getDouble("totalAmount");
                Date orderDate = order.getDate("orderDate");

                // Extract the month and year from the orderDate
                String monthYear = getMonthYear(orderDate);

                // Store the earnings in Redis
                jedis.hincrByFloat("monthly_earnings", monthYear, totalAmount);
            }
            System.out.println("Monthly earnings updated in Redis.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMonthYear(Date orderDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(orderDate);  // Format to "YYYY-MM"
    }

    public List<String[]> getMonthlyEarnings() {
        List<String[]> earningsList = new ArrayList<>();
        try (Jedis jedis = RedisDB.getJedis()) {
            updateMonthlyEarnings();
            // Retrieve all entries from the hash "monthly_earnings"
            Map<String, String> monthlyEarnings = jedis.hgetAll("monthly_earnings");

            // Parse and sort the earnings data by month
            List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(monthlyEarnings.entrySet());
            sortedEntries.sort((a, b) -> b.getKey().compareTo(a.getKey())); // Sort by month in descending order

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy");

            for (Map.Entry<String, String> entry : sortedEntries) {
                String monthYearKey = entry.getKey(); // e.g., "2024-06"
                double totalEarnings = Double.parseDouble(entry.getValue());

                // Convert the month key into a readable format (e.g., "June 2024")
                Date month = inputFormat.parse(monthYearKey);
                String formattedMonth = outputFormat.format(month);

                // Add data to the earnings list
                earningsList.add(new String[]{
                        formattedMonth,
                        String.format("%.2f", totalEarnings)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return earningsList;
    }

    public List<String[]> getBestSellingProducts() {
        List<String[]> bestSellingProducts = new ArrayList<>();

        try (Jedis jedis = RedisDB.getJedis()) {
            MongoDatabase mongoDatabase = MongoDBConnection.getDatabase();
            MongoCollection<Document> orderCollection = mongoDatabase.getCollection("order");

            // Step 1: Aggregate productID and total quantity from orderDetails
            Map<String, Integer> productSales = new HashMap<>();
            FindIterable<Document> orders = orderCollection.find();

            for (Document order : orders) {
                List<Document> orderDetails = (List<Document>) order.get("orderDetails");
                for (Document detail : orderDetails) {
                    // Use get() and convert to String
                    Object productIDObj = detail.get("productID");
                    String productID = productIDObj != null ? productIDObj.toString() : null;

                    if (productID != null) {
                        int quantity = detail.getInteger("quantity", 0);
                        productSales.put(productID, productSales.getOrDefault(productID, 0) + quantity);
                    }
                }
            }


            // Step 2: Store data in Redis sorted set (best_selling_products)
            for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
                String productID = entry.getKey();
                int totalSold = entry.getValue();
                jedis.zadd("best_selling_products", totalSold, productID);
            }

            // Step 3: Retrieve sorted data from Redis
            List<Tuple> sortedProducts = jedis.zrevrangeWithScores("best_selling_products", 0, -1);

            // Step 4: Map productID to productName using MySQL
            for (Tuple product : sortedProducts) {
                String productID = product.getElement();
                int totalSold = (int) product.getScore();

                String productName = null;
                String query = "SELECT productName FROM Product WHERE productID = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, productID);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    productName = resultSet.getString("productName");
                }

                // Add product details to the list
                bestSellingProducts.add(new String[]{
                        productID,
                        productName != null ? productName : "Unknown",
                        String.valueOf(totalSold)
                });

                statement.close();
            }
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestSellingProducts;
    }

}
