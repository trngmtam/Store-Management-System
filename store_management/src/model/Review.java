public class Review {
    private int customerID;
    private int productID;
    private int star;
    private String feedback;

    public Review(int customerID, int productID, int star, String feedback) {
        this.customerID = customerID;
        this.productID = productID;
        this.star = star;
        this.feedback = feedback;
    }

    public int getCustomerID() { return customerID; }
    public void setCustomerID(int customerID) { this.customerID = customerID; }

    public int getProductID() { return productID; }
    public void setProductID(int productID) { this.productID = productID; }

    public int getStar() { return star; }
    public void setStar(int star) { this.star = star; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}

