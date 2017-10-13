package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.*;
import cmu.sv.lifeRecord.helpers.*;
import cmu.sv.lifeRecord.models.WatcherList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("watcherlists")
public class WatcherListsInterface {
    private MongoCollection<Document> collection = null;
    private ObjectWriter ow;

    public WatcherListsInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("watcherlists");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getAll() {

        ArrayList<WatcherList> watcherLists = new ArrayList<WatcherList>();

        try {
            FindIterable<Document> results = collection.find();
            for (Document item : results) {
                WatcherList watcherList = new WatcherList(
                        (ArrayList<String>) item.get("usersId"),
                        item.getString("targetId")
                );
                watcherList.setId(item.getObjectId("_id").toString());
                watcherLists.add(watcherList);
            }
            return new APPResponse(watcherLists);

        } catch(Exception e) {
            System.out.println("Get Data EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }

    }

    @GET
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getOne(@PathParam("id") String id) {


        BasicDBObject query = new BasicDBObject();

        try {
            query.put("_id", new ObjectId(id));
            Document item = collection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such watcher list, my friend.");
            }
            WatcherList watcherList = new WatcherList(
                    (ArrayList<String>) item.get("usersId"),
                    item.getString("targetId")
            );
            watcherList.setId(item.getObjectId("_id").toString());
            return new APPResponse(watcherList);

        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(0,"No such watcher list.");
        } catch(IllegalArgumentException e) {
            throw new APPBadRequestException(45,"Unacceptable ID.");
        }  catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }


    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create(Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        if (!json.has("usersId"))
            throw new APPBadRequestException(55,"missing users");
        if (!json.has("targetId"))
            throw new APPBadRequestException(55,"missing target");
        try {
            Document doc = new Document();
            doc.append("usersId", json.get("usersId"));
            doc.append("targetId", json.getString("targetId"));

            collection.insertOne(doc);
            return new APPResponse(request);
        } catch(JSONException e) {
            //System.out.println("Failed to patch a document");
            throw new APPBadRequestException(33,"Failed to post a document");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse update(@PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }

        try {

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));
            if (json.has("targetId"))
                throw new APPBadRequestException(33, "Target can't be updated.");

            Document doc = new Document();
            if (json.has("usersId"))
                doc.append("usersId",json.get("usersId"));

            Document set = new Document("$set", doc);
            collection.updateOne(query,set);

        }  catch(APPBadRequestException e){
            throw new APPBadRequestException(33, "Target can't be updated.");
        } catch(JSONException e) {
            System.out.println("Failed to patch a document");

        }
        return new APPResponse(request);
    }

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@PathParam("id") String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));

        DeleteResult deleteResult = collection.deleteOne(query);
        if (deleteResult.getDeletedCount() < 1)
            throw new APPNotFoundException(66,"Could not delete");

        return new APPResponse(new JSONObject());
    }
}
