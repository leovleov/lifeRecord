package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.Record;
import cmu.sv.lifeRecord.helpers.*;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
//11111
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
    public APPResponse getAll() {

        ArrayList<Record> recordList = new ArrayList<Record>();

        try {
            FindIterable<Document> results = collection.find();
            if (results == null) {
                return new APPResponse(recordList);
            }
            for (Document item : results) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Record record = new Record(
                        item.getString("recordName"),
                        item.getString("recordInfo"),
                        item.getString("albumId"),
                        item.getString("targetId"),
                        item.getString("userId"),
                        sdf.format(item.getDate("createDate")),
                        sdf.format(item.getDate("updateDate"))
                );
                record.setId(item.getObjectId("_id").toString());
                recordList.add(record);
            }
            return new APPResponse(recordList);
        } catch(Exception e) {
            System.out.println("Get data EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOne(@Context HttpHeaders headers, @PathParam("id") String id) {
        BasicDBObject query = new BasicDBObject();
        try {
            query.put("_id", new ObjectId(id));
            Document item = collection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such record.");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Record record = new Record(
                    item.getString("recordName"),
                    item.getString("recordInfo"),
                    item.getString("albumId"),
                    item.getString("targetId"),
                    item.getString("userId"),
                    sdf.format(item.getDate("createDate")),
                    sdf.format(item.getDate("updateDate"))
            );
            record.setId(item.getObjectId("_id").toString());
            AuthCheck.checkWatcherAuthentication(headers,item.getString("targetId"));
            return new APPResponse(record);
        } catch(APPNotFoundException e) {
            throw e;
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch(IllegalArgumentException e) {
            throw new APPBadRequestException(45,"Unacceptable ID.");
        }  catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }

    @GET
    @Path("{id}/pictures")
    @Produces({MediaType.APPLICATION_JSON})
    public APPListResponse getPicturesForRecord(@Context HttpHeaders headers, @PathParam("id") String id,
                                                @DefaultValue("_id") @QueryParam("sort") String sortArg,
                                                @DefaultValue("20") @QueryParam("count") int count,
                                                @DefaultValue("0") @QueryParam("offset") int offset) {

        ArrayList<Picture> picList = new ArrayList<Picture>();

        try {
            //checkAuthentication(headers,id);
            BasicDBObject sortParams = new BasicDBObject();
            List<String> sortList = Arrays.asList(sortArg.split(","));
            sortList.forEach(sortItem -> {
                sortParams.put(sortItem,1);
            });

            BasicDBObject query = new BasicDBObject();
            query.put("recordId", id);

            long resultCount = picCollection.count(query);
            FindIterable<Document> results = picCollection.find(query).sort(sortParams).skip(offset).limit(count);
            for (Document item : results) {
                Picture pic = new Picture(
                        item.getString("url"),
                        item.getString("recordId")
                );
                pic.setId(item.getObjectId("_id").toString());
                picList.add(pic);

            }
            return new APPListResponse(picList,resultCount,offset, picList.size());

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
    public APPResponse update(@PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
        }
        catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        }

        try {
            if (json.has("targetId"))
                //doc.append("targetId",json.getString("targetId"));
                throw new APPBadRequestException(33, "Target can't be updated.");
            if (json.has("viewId"))
                //doc.append("viewId",json.getString("viewId"));
                throw new APPBadRequestException(33, "View History can't be updated.");
            if (json.has("likeId"))
                //doc.append("likeId",json.getString("likeId"));
                throw new APPBadRequestException(33, "Like list can't be updated.");
            if (json.has("editorId"))
                //doc.append("editorId",json.getString("editorId"));
                throw new APPBadRequestException(33, "Editor can't be updated.");

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            Document doc = new Document();
            if (json.has("recordName"))
                doc.append("recordName",json.getString("recordName"));
            if (json.has("recordInfo"))
                doc.append("recordInfo",json.getString("recordInfo"));
            if (json.has("albumId"))
                doc.append("albumId",json.getString("albumId"));

            Document set = new Document("$set", doc);
            collection.updateOne(query,set);

        } catch(APPBadRequestException e){
            throw e;
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to patch a document.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
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
            throw new APPNotFoundException(66,"Could not delete the record.");

        return new APPResponse(new JSONObject());
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
        if (!json.has("recordName"))
            throw new APPBadRequestException(55,"missing name");
        if (!json.has("recordInfo"))
            throw new APPBadRequestException(55,"missing info");
        if (!json.has("targetId"))
            throw new APPBadRequestException(55,"missing target");
        if (!json.has("userId"))
            throw new APPBadRequestException(55,"missing user");
        try {
            Document doc = new Document();
            doc.append("recordName", json.getString("recordName"));
            doc.append("recordInfo", json.getString("recordInfo"));
            doc.append("targetId", json.getString("targetId"));
            doc.append("userId", json.getString("userId"));
            if (json.has("albumId"))
                doc.append("albumId", json.getString("albumId"));
            doc.append("createDate", new Date());
            doc.append("updateDate", new Date());

            collection.insertOne(doc);
            return new APPResponse(request);
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to post a document.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }

    @POST
    @Path("{id}/pictures")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createPic(@PathParam("id") String id, Object request) {
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
            return new APPResponse(request);
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to post a document.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }

    void checkAuthentication(HttpHeaders headers,String id) throws Exception{
        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70,"No Authorization Headers");
        String token = authHeaders.get(0);
        String clearToken = APPCrypt.decrypt(token);
        if (id.compareTo(clearToken) != 0) {
            throw new APPUnauthorizedException(71,"Invalid token. Please try getting a new token");
        }
    }
}
