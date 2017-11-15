package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("watchers")
public class WatcherInterface {
    private MongoCollection<Document> collection = null;
    private ObjectWriter ow;

    public WatcherInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("watchers");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create(@Context HttpHeaders headers, Object request) {

        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, "Failed to parse the data.");
        }
        if (!json.has("userId"))
            throw new APPBadRequestException(55,"Missing user.");
        if (!json.has("targetId"))
            throw new APPBadRequestException(55,"Missing target.");

        try {
            AuthCheck.checkWatcherAuthentication(headers, json.getString("targetId"));
            Document doc = new Document();
            doc.append("userId", json.getString("userId"));
            doc.append("targetId", json.getString("targetId"));
            collection.insertOne(doc);
            return new APPResponse(request);
        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, "Failed to parse the data.");
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers,  Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));

            if (!json.has("userId"))
                throw new APPBadRequestException(55,"Missing user.");
            if (!json.has("targetId"))
                throw new APPBadRequestException(55,"Missing target.");

            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"),false);
            BasicDBObject query = new BasicDBObject();
            query.put("targetId", json.getString("targetId"));
            query.put("userId", json.getString("userId"));
            DeleteResult deleteResult = collection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete watcher.");
            return new APPResponse(new JSONObject());

        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, "Failed to parse the data.");
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }
}
