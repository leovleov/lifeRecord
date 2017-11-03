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

    public static void checkAnyAuthentication(HttpHeaders headers, String id) throws Exception{
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
}
