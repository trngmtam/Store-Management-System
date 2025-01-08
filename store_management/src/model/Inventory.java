import java.sql.Timestamp;

public class Inventory {
    private int productID;
    private int quantity;
    private String location;
    private Timestamp lastUpdated;

    public Inventory(int productID, int quantity, String location, Timestamp lastUpdated) {
        this.productID = productID;
        this.quantity = quantity;
        this.location = location;
        this.lastUpdated = lastUpdated;
    }

    public int getProductID() {
        return productID;
    }
    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
