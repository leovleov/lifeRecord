package cmu.sv.lifeRecord.helpers;

import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.ws.rs.core.HttpHeaders;
import java.util.List;

public class AuthCheck {

    // Only Admin can pass
    public static void checkAdminAuthentication(HttpHeaders headers) throws Exception{
        MongoCollection<Document> adminCollection;
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        adminCollection = database.getCollection("admins");

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);

        BasicDBObject query = new BasicDBObject();
        query.put("userId", clearToken);
        Document item = adminCollection.find(query).first();
        if (item == null) {     //This user is an administrator
            throw new APPUnauthorizedException(71, "Authenticaion Failed.");
        }

    }

    //The user and admin can pass
    public static void checkOwnAuthentication(HttpHeaders headers, String id) throws Exception{
        MongoCollection<Document> adminCollection;
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        adminCollection = database.getCollection("admins");

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);

        BasicDBObject query = new BasicDBObject();
        query.put("userId", clearToken);
        Document item = adminCollection.find(query).first();
        if (item != null) {     //This user is an administrator
            return;
        }
        else if(id == null){
            throw new APPUnauthorizedException(71, "Authenticaion Failed.");
        }
        if (id.compareTo(clearToken) != 0) {
            throw new APPUnauthorizedException(71, "Invalid token. Please try getting a new token.");
        }

    }

    //Any login person can pass
    public static void checkAnyAuthentication(HttpHeaders headers) throws Exception{
        MongoCollection<Document> userCollection;
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        userCollection = database.getCollection("users");

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(clearToken));
        Document item = userCollection.find(query).first();
        if (item == null) {     //This user does not exist
            throw new APPUnauthorizedException(71, "Invalid token. Please try getting a new token.");
        }

    }

    //The user in the editor list and admin can pass, isCreator specifies whether the user need to be the creator
    public static void checkEditorAuthentication(HttpHeaders headers, String targetId, Boolean isCreator) throws Exception{
        MongoCollection<Document> adminCollection;
        MongoCollection<Document> editorCollection;
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        adminCollection = database.getCollection("admins");
        editorCollection = database.getCollection("editors");

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);

        BasicDBObject query = new BasicDBObject();
        query.put("userId", clearToken);
        Document item = adminCollection.find(query).first();
        if (item != null) {     //This user is an administrator
            return;
        }
        BasicDBObject query2 = new BasicDBObject();
        query2.put("userId", clearToken);
        query2.put("targetId", targetId);
        if(isCreator == true)
            query2.put("isCreator", true);
        Document item2 = editorCollection.find(query2).first();
        if (item2 != null){
            return;
        }
        else{
            throw new APPUnauthorizedException(71, "Authorization fail!");
        }

    }

    //The user in the watcher list and admin can pass
    public static void checkWatcherAuthentication(HttpHeaders headers, String targetId) throws Exception{
        MongoCollection<Document> adminCollection;
        MongoCollection<Document> editorCollection;
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        adminCollection = database.getCollection("admins");
        editorCollection = database.getCollection("watchers");

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);

        BasicDBObject query = new BasicDBObject();
        query.put("userId", clearToken);
        Document item = adminCollection.find(query).first();
        if (item != null) {     //This user is an administrator
            return;
        }
        BasicDBObject query2 = new BasicDBObject();
        query2.put("userId", clearToken);
        query2.put("targetId", targetId);
        Document item2 = editorCollection.find(query2).first();
        if (item2 != null){
            return;
        }
        else{
            throw new APPUnauthorizedException(71, "Authorization fail!");
        }

    }
}
