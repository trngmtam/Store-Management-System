public class Account {
    private int customerID;
    private String password;

    public Account(int customerID, String password) {
        this.customerID = customerID;
        this.password = password;
    }

    public int getCustomerID() {
        return customerID;
    }
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
