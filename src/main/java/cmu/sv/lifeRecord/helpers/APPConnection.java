package cmu.sv.lifeRecord.helpers;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

public class APPConnection {
    public static MongoCollection<Document> albumCollection;
    public static MongoCollection<Document> targetCollection;
    public static MongoCollection<Document> editorCollection;
    public static MongoCollection<Document> watcherCollection;
    public static MongoCollection<Document> userCollection;
    public static MongoCollection<Document> recordCollection;
    public static MongoCollection<Document> picCollection;
    public static MongoCollection<Document> msgCollection;
    public static MongoCollection<Document> likeCollection;

    static {
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE);
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        albumCollection = database.getCollection("albums");
        targetCollection = database.getCollection("targets");
        editorCollection = database.getCollection("editors");
        watcherCollection = database.getCollection("watchers");
        userCollection = database.getCollection("users");
        recordCollection = database.getCollection("records");
        picCollection = database.getCollection("pictures");
        msgCollection = database.getCollection("messages");
        likeCollection = database.getCollection("likes");
    }
    public APPConnection()
    {

    }
}
