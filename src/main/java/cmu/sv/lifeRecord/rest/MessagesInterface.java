package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import cmu.sv.lifeRecord.helpers.PATCH;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("messages")
public class MessagesInterface {
    private MongoCollection<Document> collection = null;
    private ObjectWriter ow;

    public MessagesInterface() {
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE);
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("messages");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    //get? put?

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse update(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            AuthCheck.checkAdminAuthentication(headers);
            json = new JSONObject(ow.writeValueAsString(request));
            if (json.has("recordId"))
                //doc.append("recordId",json.getString("recordId"));
                throw new APPBadRequestException(33, "Record can't be updated.");

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            Document doc = new Document();
            if (json.has("messageInfo"))
                doc.append("messageInfo",json.getString("messageInfo"));

            Document set = new Document("$set", doc);
            collection.updateOne(query,set);

        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(APPBadRequestException e){
            throw e;
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to patch a document.");
        } catch (Exception e ){
            throw new APPInternalServerException(99,"Unexpected error!");
        }
        return new APPResponse(request);
    }


    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            AuthCheck.checkAdminAuthentication(headers);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            DeleteResult deleteResult = collection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete");

            return new APPResponse(new JSONObject());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPNotFoundException e) {
            throw e;
        } catch (Exception e){
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }
}
