import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ReviewDAO {
    private MongoCollection<Document> reviewCollection;

    public ReviewDAO() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        reviewCollection = database.getCollection("review");
    }

    // Create a new review
    public void addReview(int customerID, int productID, int star, String feedback) {
        Document doc = new Document("customerID", customerID)
                .append("productID", productID)
                .append("star", star)
                .append("feedback", feedback);
        reviewCollection.insertOne(doc);
    }

    // Retrieve all reviews for a specific product
    public List<Review> getReviewsByProductID(int productID) {
        List<Review> reviews = new ArrayList<>();
        for (Document doc : reviewCollection.find(eq("productID", productID))) {
            reviews.add(new Review(
                    doc.getInteger("customerID"),
                    doc.getInteger("productID"),
                    doc.getInteger("star"),
                    doc.getString("feedback")
            ));
        }
        return reviews;
    }

    // Update a review
    public boolean updateReview1(int customerID, int productID, int star, String feedback) {
        var updateResult = reviewCollection.updateOne(
                Filters.and(eq("customerID", customerID), eq("productID", productID)),
                combine(
                        set("star", star),
                        set("feedback", feedback)
                )
        );
        return updateResult.getModifiedCount() > 0;
    }

    public void updateReview(int customerID, int productID, int newStars, String newFeedback) {
        Bson filter = combine(
                eq("customerID", customerID),
                eq("productID", productID)
        );
        Bson updates = combine(
                set("stars", newStars),
                set("feedback", newFeedback)
        );
        reviewCollection.updateOne(filter, updates);
    }

    public boolean deleteReview(int customerID, int productID) {
        long deletedCount = reviewCollection.deleteOne(
                Filters.and(
                        Filters.eq("customerID", customerID),
                        Filters.eq("productID", productID)
                )
        ).getDeletedCount();
        return deletedCount > 0;
    }


    public List<Review> getReviewsByCustomerID(int customerID) {
        List<Review> reviews = new ArrayList<>();
        for (Document doc : reviewCollection.find(eq("customerID", customerID))) {
            reviews.add(new Review(
                    doc.getInteger("customerID"),
                    doc.getInteger("productID"),
                    doc.getInteger("star"),
                    doc.getString("feedback")
            ));
        }
        return reviews;
    }
}
