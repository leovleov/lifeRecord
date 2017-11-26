package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.Message;
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
    private MongoCollection<Document> msgCollection;
    private MongoCollection<Document> likeCollection;
    private ObjectWriter ow;


    public RecordsInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("records");
        picCollection = database.getCollection("pictures");
        msgCollection = database.getCollection("messages");
        likeCollection = database.getCollection("likes");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }



//    @GET
//    @Produces({ MediaType.APPLICATION_JSON})
//    public APPResponse getAll() {
//
//        ArrayList<Record> recordList = new ArrayList<Record>();
//
//        try {
//            FindIterable<Document> results = collection.find();
//            if (results == null) {
//                return new APPResponse(recordList);
//            }
//            for (Document item : results) {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Record record = new Record(
//                        item.getString("recordName"),
//                        item.getString("recordInfo"),
//                        item.getString("albumId"),
//                        item.getString("targetId"),
//                        item.getString("userId"),
//                        sdf.format(item.getDate("createDate")),
//                        sdf.format(item.getDate("updateDate"))
//                );
//                record.setId(item.getObjectId("_id").toString());
//                recordList.add(record);
//            }
//            return new APPResponse(recordList);
//        } catch(Exception e) {
//            System.out.println("Get data EXCEPTION!!!!");
//            e.printStackTrace();
//            throw new APPInternalServerException(99,e.getMessage());
//        }
//    }

    public String recordCheckEditor(HttpHeaders headers, String recordId) throws Exception {
        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");

        BasicDBObject query = new BasicDBObject();

        query.put("_id", new ObjectId(recordId));
        Document item = collection.find(query).first();
        if (item == null) {
            throw new APPNotFoundException(0, "No such record.");
        }
        AuthCheck.checkEditorAuthentication(headers,item.getString("targetId"),false);
        return item.getString("targetId");
    }

    public String recordCheckWatcher(HttpHeaders headers, String recordId) throws Exception {
        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        BasicDBObject query = new BasicDBObject();

        query.put("_id", new ObjectId(recordId));
        Document item = collection.find(query).first();
        if (item == null) {
            throw new APPNotFoundException(0, "No such record.");
        }
        AuthCheck.checkWatcherAuthentication(headers,item.getString("targetId"));
        return item.getString("targetId");
    }

    public String recordCheckOwn(HttpHeaders headers, String recordId) throws Exception {
        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        BasicDBObject query = new BasicDBObject();

        query.put("_id", new ObjectId(recordId));
        Document item = collection.find(query).first();
        if (item == null) {
            throw new APPNotFoundException(0, "No such record.");
        }
        AuthCheck.checkOwnAuthentication(headers,item.getString("userId"));
        return item.getString("targetId");
    }

    public String likeCheckOwn(HttpHeaders headers, String recordId) throws Exception {
        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null)
            throw new APPUnauthorizedException(70, "No Authorization Headers");
        String token = authHeaders.get(0);
        String userId = APPCrypt.decrypt(token);
        BasicDBObject query = new BasicDBObject();

        query.put("recordId", recordId);
        query.put("userId", userId);
        Document item = likeCollection.find(query).first();
        if (item == null) {
            throw new APPNotFoundException(0, "No such record.");
        }
        if(!item.getString("userId").equals(userId))
            throw new APPUnauthorizedException(71, "Authorization fail!");
        return item.getObjectId("_id").toString();
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
    @Path("{id}/likeNumber")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getLikeNumber(@Context HttpHeaders headers, @PathParam("id") String id) {
        BasicDBObject query = new BasicDBObject();
        try {
            recordCheckWatcher(headers,id);
            query.put("recordId", id);
            long resultCount = likeCollection.count(query);

            return new APPResponse(resultCount);
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
            recordCheckWatcher(headers, id);
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
                        item.getString("recordId"),
                        item.getString("targetId")
                );
                pic.setId(item.getObjectId("_id").toString());
                picList.add(pic);

            }
            return new APPListResponse(picList,resultCount,offset, picList.size());

        } catch(APPUnauthorizedException e) {
            throw e;
        } catch(Exception e) {
            System.out.println("Get data EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }
    }

    @GET
    @Path("{id}/messages")
    @Produces({MediaType.APPLICATION_JSON})
    public APPListResponse getMessagesForRecord(@Context HttpHeaders headers, @PathParam("id") String id,
                                                @DefaultValue("_id") @QueryParam("sort") String sortArg,
                                                @DefaultValue("20") @QueryParam("count") int count,
                                                @DefaultValue("0") @QueryParam("offset") int offset) {

        ArrayList<Message> msgList = new ArrayList<>();

        try {
            recordCheckWatcher(headers, id);
            BasicDBObject sortParams = new BasicDBObject();
            List<String> sortList = Arrays.asList(sortArg.split(","));
            sortList.forEach(sortItem -> {
                sortParams.put(sortItem,1);
            });

            BasicDBObject query = new BasicDBObject();
            query.put("recordId", id);

            long resultCount = msgCollection.count(query);
            FindIterable<Document> results = msgCollection.find(query).sort(sortParams).skip(offset).limit(count);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Document item : results) {
                Message msg = new Message(
                        item.getString("userId"),
                        item.getString("recordId"),
                        item.getString("messageInfo"),
                        sdf.format(item.getDate("createDate"))
                );
                msg.setId(item.getObjectId("_id").toString());
                msgList.add(msg);

            }
            return new APPListResponse(msgList,resultCount,offset, msgList.size());

        } catch(APPUnauthorizedException e) {
            throw e;
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
    public APPResponse update(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {

        JSONObject json = null;

        try {
            recordCheckEditor(headers, id);
            json = new JSONObject(ow.writeValueAsString(request));
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
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
        return new APPResponse(request);
    }


    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            recordCheckOwn(headers, id);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            DeleteResult deleteResult = collection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete the record.");

            return new APPResponse(new JSONObject());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }

    @DELETE
    @Path("{id}/likes")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse deleteLike(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            String likeId = likeCheckOwn(headers, id);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(likeId));

            DeleteResult deleteResult = likeCollection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete the like.");

            return new APPResponse(new JSONObject());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }

//    @POST
//    @Consumes({ MediaType.APPLICATION_JSON})
//    @Produces({ MediaType.APPLICATION_JSON})
//    public APPResponse create(Object request) {
//        JSONObject json = null;
//        try {
//            json = new JSONObject(ow.writeValueAsString(request));
//        }
//        catch (JsonProcessingException e) {
//            throw new APPBadRequestException(33, e.getMessage());
//        }
//        if (!json.has("recordName"))
//            throw new APPBadRequestException(55,"missing name");
//        if (!json.has("recordInfo"))
//            throw new APPBadRequestException(55,"missing info");
//        if (!json.has("targetId"))
//            throw new APPBadRequestException(55,"missing target");
//        if (!json.has("userId"))
//            throw new APPBadRequestException(55,"missing user");
//        try {
//            Document doc = new Document();
//            doc.append("recordName", json.getString("recordName"));
//            doc.append("recordInfo", json.getString("recordInfo"));
//            doc.append("targetId", json.getString("targetId"));
//            doc.append("userId", json.getString("userId"));
//            if (json.has("albumId"))
//                doc.append("albumId", json.getString("albumId"));
//            else
//                doc.append("albumId", "");
//            doc.append("createDate", new Date());
//            doc.append("updateDate", new Date());
//
//            collection.insertOne(doc);
//            return new APPResponse(request);
//        } catch(JSONException e) {
//            throw new APPBadRequestException(33,"Failed to post a document.");
//        } catch(APPUnauthorizedException e) {
//            throw e;
//        } catch(Exception e) {
//            throw new APPInternalServerException(99,"Unexpected error!");
//        }
//    }

    @POST
    @Path("{id}/pictures")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createPic(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
             String targetId = recordCheckEditor(headers, id);
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("url"))
                throw new APPBadRequestException(55,"missing url.");
            Document doc = new Document("url", json.getString("url"))
                    .append("recordId", id)
                    .append("targetId", targetId);
            picCollection.insertOne(doc);
            return new APPResponse(request);
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to post a document.");
        } catch(BadRequestException e) {
            throw e;
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }

    @POST
    @Path("{id}/messages")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createMessage(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {
        JSONObject json = null;
        try {
            String targetId = recordCheckEditor(headers, id);

            List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null)
                throw new APPUnauthorizedException(70, "No Authorization Headers");
            String token = authHeaders.get(0);
            String userId = APPCrypt.decrypt(token);

            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("messageInfo"))
                throw new APPBadRequestException(55,"missing message information.");
            Document doc = new Document("messageInfo", json.getString("messageInfo"))
                    .append("userId", userId)
                    .append("recordId", id)
                    .append("createDate", new Date());
            msgCollection.insertOne(doc);
            return new APPResponse(request);
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to post a document.");
        } catch(BadRequestException e) {
            throw e;
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }

    @POST
    @Path("{id}/likes")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createLike(@Context HttpHeaders headers, @PathParam("id") String id) {
        JSONObject json = null;
        try {
            String targetId = recordCheckWatcher(headers, id);

            List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null)
                throw new APPUnauthorizedException(70, "No Authorization Headers");
            String token = authHeaders.get(0);
            String userId = APPCrypt.decrypt(token);

            Document doc = new Document("userId", userId)
                    .append("recordId", id)
                    .append("createDate", new Date());
            likeCollection.insertOne(doc);
            return new APPResponse(doc);
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to post a document.");
        } catch(BadRequestException e) {
            throw e;
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }


}
