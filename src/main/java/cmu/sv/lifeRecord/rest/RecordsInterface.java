package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.Record;
import cmu.sv.lifeRecord.helpers.PATCH;
import cmu.sv.lifeRecord.exceptions.*;
import cmu.sv.lifeRecord.models.Picture;
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

@Path("records")
public class RecordsInterface {
    private MongoCollection<Document> collection;
    private MongoCollection<Document> picCollection;
    private ObjectWriter ow;


    public RecordsInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("records");
        picCollection = database.getCollection("pictures");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public ArrayList<Record> getAll() {

        ArrayList<Record> recordList = new ArrayList<Record>();

        try {
            FindIterable<Document> results = collection.find();
            if (results == null) {
                return recordList;
            }
            for (Document item : results) {
                Record record = new Record(
                        item.getString("recordName"),
                        item.getString("recordInfo"),
                        item.getString("albumId"),
                        item.getString("targetId"),
                        item.getString("viewId"),
                        item.getString("likeId"),
                        item.getString("editorId")
                );
                record.setId(item.getObjectId("_id").toString());
                recordList.add(record);
            }
            return recordList;
        } catch(Exception e) {
            System.out.println("Get data EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Record getOne(@PathParam("id") String id) {
        BasicDBObject query = new BasicDBObject();
        try {
            query.put("_id", new ObjectId(id));
            Document item = collection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such record");
            }
            Record record = new Record(
                    item.getString("recordName"),
                    item.getString("recordInfo"),
                    item.getString("albumId"),
                    item.getString("targetId"),
                    item.getString("viewId"),
                    item.getString("likeId"),
                    item.getString("editorId")
            );
            record.setId(item.getObjectId("_id").toString());
            return record;
        } catch(APPNotFoundException e) {
            throw new APPNotFoundException(0,"No such record.");
        } catch(IllegalArgumentException e) {
            throw new APPBadRequestException(45,"Unacceptable ID.");
        }  catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @GET
    @Path("{id}/pictures")
    @Produces({MediaType.APPLICATION_JSON})
    public ArrayList<Picture> getPicturesForRecord(@PathParam("id") String id) {

        ArrayList<Picture> picList = new ArrayList<Picture>();

        try {
            BasicDBObject query = new BasicDBObject();
            query.put("recordId", id);

            FindIterable<Document> results = picCollection.find(query);
            for (Document item : results) {
                Picture pic = new Picture(
                        item.getString("url"),
                        item.getString("recordId")
                );
                pic.setId(item.getObjectId("_id").toString());
                picList.add(pic);
            }
            return picList;

        } catch(Exception e) {
            System.out.println("Get data EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }
    }

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public Object update(@PathParam("id") String id, Object request) {
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

            Document doc = new Document();
            if (json.has("recordName"))
                doc.append("recordName",json.getString("recordName"));
            if (json.has("recordInfo"))
                doc.append("recordInfo",json.getString("recordInfo"));
            if (json.has("albumId"))
                doc.append("albumId",json.getString("albumId"));
            if (json.has("targetId"))
                doc.append("targetId",json.getString("targetId"));
            if (json.has("viewId"))
                doc.append("viewId",json.getString("viewId"));
            if (json.has("likeId"))
                doc.append("likeId",json.getString("likeId"));
            if (json.has("editorId"))
                doc.append("editorId",json.getString("editorId"));
            Document set = new Document("$set", doc);
            collection.updateOne(query,set);

        } catch(JSONException e) {
            //System.out.println("Failed to patch a document");
            throw new APPBadRequestException(33,"Failed to patch a document");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
        return request;
    }


    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public Object delete(@PathParam("id") String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(id));

        DeleteResult deleteResult = collection.deleteOne(query);
        if (deleteResult.getDeletedCount() < 1)
            throw new APPNotFoundException(66,"Could not delete");

        return new JSONObject();
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public Object create(Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        if (!json.has("recordName"))
            throw new APPBadRequestException(55,"missing name");
        if (!json.has("recordInfo"))
            throw new APPBadRequestException(55,"missing info");
        if (!json.has("targetId"))
            throw new APPBadRequestException(55,"missing target");
        if (!json.has("editorId"))
            throw new APPBadRequestException(55,"missing editor");
        try {
            Document doc = new Document();
            doc.append("recordName", json.getString("recordName"));
            doc.append("recordInfo", json.getString("recordInfo"));
            doc.append("targetId", json.getString("targetId"));
            doc.append("editorId", json.getString("editorId"));
            if (json.has("albumId"))
                doc.append("albumId", json.getString("albumId"));
            if (json.has("viewId"))
                doc.append("viewId", json.getString("viewId"));
            if (json.has("likeId"))
                doc.append("likeId", json.getString("likeId"));

            collection.insertOne(doc);
            return request;
        } catch(JSONException e) {
            //System.out.println("Failed to patch a document");
            throw new APPBadRequestException(33,"Failed to post a document");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }

    @POST
    @Path("{id}/pictures")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public Object createPic(@PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }
        if (!json.has("url"))
            throw new APPBadRequestException(55,"missing url");
        if (!json.has("recordId"))
            throw new APPBadRequestException(55,"missing related record");
        try {
            Document doc = new Document("url", json.getString("url"))
                    .append("recordId", json.getString("recordId"));
            picCollection.insertOne(doc);
            return request;
        } catch(JSONException e) {
            //System.out.println("Failed to patch a document");
            throw new APPBadRequestException(33,"Failed to post a document");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Something happened at server side!");
        }
    }
}
