import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnection {

    private static final String CONNECTION_STRING = "mongodb+srv://trngmtam:trngmtam@cluster0.nubwt.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private static final String DATABASE_NAME = "RetailStore";
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Static initializer to configure and establish the connection
    static {
        try {
            ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                    .serverApi(serverApi)
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DATABASE_NAME);

            // Ping to verify connection
            database.runCommand(new Document("ping", 1));
            System.out.println("Pinged your deployment. Successfully connected to MongoDB!");

        } catch (MongoException e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    // Method to retrieve the database instance
    public static MongoDatabase getDatabase() {
        return database;
    }

    // Close the connection when the application shuts down
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}
