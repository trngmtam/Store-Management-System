import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Connection connection;

    public ProductDAO() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addProduct(Product product) {
        String sql = "INSERT INTO Product (productID, productName, productDescription, productPrice) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, product.getProductID());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getProductDescription());
            stmt.setDouble(4, product.getProductPrice());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getProductByName(String productName) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product WHERE productName LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1,"%" + productName + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("productID"),
                        rs.getString("productName"),
                        rs.getString("productDescription"),
                        rs.getDouble("productPrice")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductByID(int productID) {
        String sql = "SELECT * FROM Product WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Product(
                    rs.getInt("productID"),
                    rs.getString("productName"),
                    rs.getString("productDescription"),
                    rs.getDouble("productPrice")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Product> getAllProduct() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Product";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("productID"),
                    rs.getString("productName"),
                    rs.getString("productDescription"),
                    rs.getDouble("productPrice")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Method to get the price of a product from the database
    public double getProductPrice(int productID) {
        double price = 0.0;
        String query = "SELECT productPrice FROM Product WHERE productID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("productPrice");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    public String getProductName(int productID) {
        String name = "";
        String query = "SELECT productName FROM Product WHERE productID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name = rs.getString("productName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    // Method to delete Product
    public boolean deleteProduct(int productID) {
        String sql = "DELETE FROM Product WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to update Product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE Product SET productName = ?, productDescription = ?, productPrice = ? WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getProductDescription());
            stmt.setDouble(3, product.getProductPrice());
            stmt.setInt(4, product.getProductID());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
