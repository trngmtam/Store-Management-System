import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

public class CustomerDAO {
    private MongoCollection<Document> customersCollection;

    public CustomerDAO() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        customersCollection = database.getCollection("customer");
    }

    // Method to add a customer
    public boolean addCustomer(Customer customer) {
        try {
            // Create a BSON document from the Customer object
            Document customerDocument = new Document("customerID", customer.getCustomerID())
                    .append("customerName", customer.getCustomerName())
                    .append("customerPhone", customer.getCustomerPhone())
                    .append("customerAddress", customer.getCustomerAddress());
            // Insert the document into the collection
            customersCollection.insertOne(customerDocument);
            return true; // Insertion successful
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return false; // Insertion failed
        }
    }

    // Method to view customers by ID
    public Customer getCustomerByID(int customerID) {
        Document query = customersCollection.find(Filters.eq("customerID", customerID)).first();
        if (query != null) {
            return new Customer(
                    query.getInteger("customerID"),
                    query.getString("customerName"),
                    query.getString("customerPhone"),
                    query.getString("customerAddress")
            );
        }
        return null; // Customer not found
    }

    // Method to get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (MongoCursor<Document> cursor = customersCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                customers.add(new Customer(
                        doc.getInteger("customerID"),
                        doc.getString("customerName"),
                        doc.getString("customerPhone"),
                        doc.getString("customerAddress")
                ));
            }
        }
        return customers;
    }

    // Method to update an existing customer
    public boolean updateCustomer(Customer customer) {
        Bson filter = Filters.eq("customerID", customer.getCustomerID());
        Document updatedDocument = new Document()
                .append("customerName", customer.getCustomerName())
                .append("customerPhone", customer.getCustomerPhone())
                .append("customerAddress", customer.getCustomerAddress());

        Document update = new Document("$set", updatedDocument);

        try {
            return customersCollection.updateOne(filter, update).getModifiedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to delete a customer by ID
    public boolean deleteCustomer(int customerID) {
        Bson filter = Filters.eq("customerID", customerID);
        try {
            return customersCollection.deleteOne(filter).getDeletedCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to search for customers by name
    public List<Customer> searchCustomerByName(String customerName) {
        List<Customer> customers = new ArrayList<>();
        Bson filter = Filters.regex("customerName", ".*" + customerName + ".*", "i");

        try (MongoCursor<Document> cursor = customersCollection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                customers.add(new Customer(
                        doc.getInteger("customerID"),
                        doc.getString("customerName"),
                        doc.getString("customerPhone"),
                        doc.getString("customerAddress")
                ));
            }
        }
        return customers;
    }

    // Method to generate a new customer ID
    public int generateCustomerID() {
        Document lastCustomer = customersCollection.find().sort(new Document("customerID", -1)).first();
        if (lastCustomer != null) {
            return lastCustomer.getInteger("customerID") + 1;
        }
        return 1; // Default to 1 if no customers exist
    }
}
