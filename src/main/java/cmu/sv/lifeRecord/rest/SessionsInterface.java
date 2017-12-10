package cmu.sv.lifeRecord.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import cmu.sv.lifeRecord.exceptions.*;
import cmu.sv.lifeRecord.helpers.*;
import cmu.sv.lifeRecord.models.User;
import cmu.sv.lifeRecord.models.Token;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("sessions")

public class SessionsInterface {

    private MongoCollection<Document> userCollection;
    private ObjectWriter ow;


    public SessionsInterface() {
//        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
//        mongoLogger.setLevel(Level.SEVERE);
//        MongoClient mongoClient = new MongoClient();
//        MongoDatabase database = mongoClient.getDatabase("liferecord");
//
//        this.userCollection = database.getCollection("users");
        APPConnection appConnection = new APPConnection();
        this.userCollection = appConnection.userCollection;
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    }


    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create( Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("emailAddress"))
                throw new APPBadRequestException(55, "Missing emailAddress.");
            if (!json.has("password"))
                throw new APPBadRequestException(55, "Missing password.");
            BasicDBObject query = new BasicDBObject();

            query.put("emailAddress", json.getString("emailAddress"));
            query.put("password", APPCrypt.encrypt(json.getString("password")));
            Document item = userCollection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No user found matching credentials.");
            }
            User user = new User(
                    item.getString("firstName"),
                    item.getString("lastName"),
                    item.getString("nickName"),
                    item.getString("phoneNumber"),
                    item.getString("emailAddress"),
                    item.getBoolean("isAdmin")
            );
            user.setId(item.getObjectId("_id").toString());
            APPResponse r = new APPResponse(new Token(user));
            return r;
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        catch (APPBadRequestException e) {
            throw e;
        }
        catch (APPNotFoundException e) {
            throw e;
        }
        catch (Exception e) {
            throw new APPInternalServerException(0, e.getMessage());
        }
    }

}



