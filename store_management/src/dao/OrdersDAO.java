import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersDAO {
    private Connection connection;
    private MongoCollection<Document> ordersCollection;

    public OrdersDAO() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        MongoDatabase database = MongoDBConnection.getDatabase();
        ordersCollection = database.getCollection("order");
    }

    // Method to get all orders from MongoDB
    public List<Orders> getAllOrders() {
        List<Orders> ordersList = new ArrayList<>();
        for (Document doc : ordersCollection.find()) {
            Orders order = new Orders(
                    doc.getInteger("orderID"),
                    doc.getInteger("customerID"),
                    doc.getDate("orderDate"),
                    doc.getDouble("totalAmount"),
                    doc.getString("status")
            );
            ordersList.add(order);
        }
        return ordersList;
    }

    // Get orders by customerID from MongoDB
    public List<Orders> getAllOrdersByCustomerID(int customerID) {
        List<Orders> ordersList = new ArrayList<>();
        for (Document doc : ordersCollection.find(Filters.eq("customerID", customerID))) {
            Orders order = new Orders(
                    doc.getInteger("orderID"),
                    doc.getInteger("customerID"),
                    doc.getDate("orderDate"),
                    doc.getDouble("totalAmount"),
                    doc.getString("status")
            );
            ordersList.add(order);
        }
        return ordersList;
    }

    // Get a specific order by orderID from MongoDB
    public Orders getAllOrdersByOrderID(int orderID) {
        Document doc = ordersCollection.find(Filters.eq("orderID", orderID)).first();
        if (doc != null) {
            return new Orders(
                    doc.getInteger("orderID"),
                    doc.getInteger("customerID"),
                    doc.getDate("orderDate"),
                    doc.getDouble("totalAmount"),
                    doc.getString("status")
            );
        }
        return null;
    }

    // Method to save order to database
    public void saveOrderToDatabase(int orderID, int customerID, Map<Integer, Integer> cart) {
        ProductDAO productDAO = new ProductDAO();

        // Calculate totalAmount
        double totalAmount = calculateTotalAmount(cart);

        // Create Order document for MongoDB
        Document orderDoc = new Document("orderID", orderID)
                .append("customerID", customerID)
                .append("orderDate", new java.util.Date())
                .append("totalAmount", totalAmount)
                .append("status", "Pending");

        // Prepare OrderDetails for MongoDB
        List<Document> orderDetails = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productID = entry.getKey();
            int quantity = entry.getValue();
            double unitPrice = productDAO.getProductPrice(productID);
            String productName = productDAO.getProductName(productID);

            orderDetails.add(new Document("productID", productID)
                    .append("productName", productName)
                    .append("quantity", quantity)
                    .append("unitPrice", unitPrice));
        }

        // Add OrderDetails to Order document
        orderDoc.append("orderDetails", orderDetails);

        // Insert the Order document into MongoDB
        ordersCollection.insertOne(orderDoc);

        System.out.println("Order ID: " + orderID + " has been placed successfully.");
    }

    // Method to calculate the total amount for the current order
    private double calculateTotalAmount(Map<Integer, Integer> cart) {
        double total = 0.0;
        ProductDAO productDAO = new ProductDAO();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productID = entry.getKey();
            int quantity = entry.getValue();
            double price = productDAO.getProductPrice(productID);
            total += price * quantity;
        }
        return total;
    }

    // Generate order ID (using MongoDB for consistency)
    public int generateOrderID() {
        Document lastOrder = ordersCollection.find().sort(new Document("orderID", -1)).first();
        return (lastOrder != null) ? lastOrder.getInteger("orderID") + 1 : 1;
    }

    public List<Object[]> getCartDetails(Map<Integer, Integer> cart) {
        List<Object[]> cartDetails = new ArrayList<>();
        String query = "SELECT productID, productName, ?, productPrice FROM Product WHERE productID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                stmt.setInt(1, entry.getValue()); // Quantity
                stmt.setInt(2, entry.getKey());   // Product ID
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    cartDetails.add(new Object[]{
                            rs.getInt("productID"),
                            rs.getString("productName"),
                            entry.getValue(),
                            rs.getDouble("productPrice")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartDetails;
    }

    public List<Object[]> getOrderDetails(int orderID) {
        List<Object[]> orderDetails = new ArrayList<>();
        Document orderDoc = ordersCollection.find(Filters.eq("orderID", orderID)).first();

        if (orderDoc != null) {
            List<Document> orderDetailsDocs = (List<Document>) orderDoc.get("orderDetails");

            for (Document detailDoc : orderDetailsDocs) {
                Object[] detail = new Object[]{
                        detailDoc.getString("productName"),
                        detailDoc.getInteger("quantity"),
                        detailDoc.getDouble("unitPrice")
                };
                orderDetails.add(detail);
            }
        }
        return orderDetails;
    }


    // Method to update an existing order
    public boolean updateOrder(Orders order) {
        Document updatedOrder = new Document("customerID", order.getCustomerID())
                .append("orderDate", order.getOrderDate())
                .append("totalAmount", order.getTotalAmount())
                .append("status", order.getStatus());

        long updatedCount = ordersCollection.updateOne(
                Filters.eq("orderID", order.getOrderID()),
                new Document("$set", updatedOrder)
        ).getModifiedCount();

        return updatedCount > 0;
    }


    // Method to delete an order by order ID
    public boolean deleteOrder(int orderID) {
        long deletedCount = ordersCollection.deleteOne(Filters.eq("orderID", orderID)).getDeletedCount();
        return deletedCount > 0;
    }

}
