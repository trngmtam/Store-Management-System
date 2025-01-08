import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private Connection connection;

    public InventoryDAO() {
        try {
            this.connection = DBConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getQuantityByProductID(int productID) {
        String sql = "SELECT quantity FROM Inventory WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if no quantity is found
    }

    public void addQuantity(int productID, int quantity) {
        String sql = "INSERT INTO Inventory (productID, quantity) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            stmt.setInt(2, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve inventory details by productID
    public Inventory getInventoryByProductID(int productID) {
        String sql = "SELECT * FROM Inventory WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Inventory(
                        rs.getInt("productID"),
                        rs.getInt("quantity"),
                        rs.getString("location"),
                        rs.getTimestamp("lastUpdated")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieve all inventory records
    public List<Inventory> getAllInventory() {
        List<Inventory> inventoryList = new ArrayList<>();
        String sql = "SELECT * FROM Inventory";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventoryList.add(new Inventory(
                        rs.getInt("productID"),
                        rs.getInt("quantity"),
                        rs.getString("location"),
                        rs.getTimestamp("lastUpdated")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryList;
    }

    // Update an existing inventory record
    public boolean updateInventory(Inventory inventory) {
        String sql = "UPDATE Inventory SET quantity = ?, location = ?, lastUpdated = ? WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, inventory.getQuantity());
            stmt.setString(2, inventory.getLocation());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Update last updated to current timestamp
            stmt.setInt(4, inventory.getProductID());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateQuantity(int productID, int newQuantity) {
        String sql = "UPDATE Inventory SET quantity = ? WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);  // Use the provided newQuantity value
            stmt.setInt(2, productID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete an inventory record by productID
    public boolean deleteInventory(int productID) {
        String sql = "DELETE FROM Inventory WHERE productID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, productID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
