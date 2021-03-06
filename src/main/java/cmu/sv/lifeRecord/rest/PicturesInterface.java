package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.Picture;
import cmu.sv.lifeRecord.helpers.*;
import cmu.sv.lifeRecord.exceptions.*;
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

@Path("pictures")
public class PicturesInterface {
    private MongoCollection<Document> collection = null;
    private ObjectWriter ow;

    public PicturesInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("pictures");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getAll() {

        ArrayList<Picture> picList = new ArrayList<Picture>();

        try {
            FindIterable<Document> results = collection.find();
            for (Document item : results) {
                Picture pic = new Picture(
                        item.getString("url"),
                        item.getString("recordId")
                );
                pic.setId(item.getObjectId("_id").toString());
                picList.add(pic);
            }
            return new APPResponse(picList);

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
                throw new APPNotFoundException(0, "No such picture.");
            }
            Picture pic = new Picture(
                    item.getString("url"),
                    item.getString("recordId")
            );
            pic.setId(item.getObjectId("_id").toString());
            return new APPResponse(pic);

        } catch(APPNotFoundException e) {
            throw e;
        } catch(IllegalArgumentException e) {
            throw new APPBadRequestException(45,"Unacceptable ID.");
        }  catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
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

            if (json.has("recordId"))
                //doc.append("recordId",json.getString("recordId"));
                throw new APPBadRequestException(33, "Record can't be updated.");

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            Document doc = new Document();
            if (json.has("url"))
                doc.append("url",json.getString("url"));

            Document set = new Document("$set", doc);
            collection.updateOne(query,set);

        }  catch(APPBadRequestException e){
            throw e;
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
