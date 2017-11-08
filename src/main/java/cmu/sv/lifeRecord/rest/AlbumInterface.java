package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import cmu.sv.lifeRecord.models.Album;
import cmu.sv.lifeRecord.models.User;
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
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("albums")
public class AlbumInterface {
    private MongoCollection<Document> collection = null;
    private MongoCollection<Document> albumCollection;
    private ObjectWriter ow;

    public AlbumInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("albums");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

//    @GET
//    @Produces({ MediaType.APPLICATION_JSON})
//    public APPResponse getAll(@Context HttpHeaders headers) {
//        ArrayList<Album> albumList = new ArrayList<Album>();
//        try {
//            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), true);
//            FindIterable<Document> results = albumCollection.find();
//            if (results == null) {
//                return new APPResponse(albumList);
//            }
//            for (Document item : results) {
//                Album album = new Album(
//                        item.getString("targetId"),
//                        item.getString("albumName"),
//                        item.getString("albumDate")
////                        item.getString("phoneNumber"),
////                        item.getString("emailAddress")
//                );
//                album.setId(item.getObjectId("_id").toString());
//                albumList.add(album);
//            }
//            return new APPResponse(albumList);
//        }
//        catch(APPUnauthorizedException e) {
//            throw e;
//        }
//        catch(Exception e) {
//            throw new APPInternalServerException(99,"Unexpected error!");
//        }
//    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOne(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            //AuthCheck.checkOwnAuthentication(headers,id);
            //AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), true);
            BasicDBObject query = new BasicDBObject();

            query.put("_id", new ObjectId(id));
            Document item = albumCollection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such user.");
            }
            Album album = new Album(
                        item.getString("targetId"),
                        item.getString("albumName"),
                        item.getString("albumDate")
                );
            album.setId(item.getObjectId("_id").toString());

            AuthCheck.checkEditorAuthentication(headers, album.getTargetId(), false);
            return new APPResponse(album);

        }
        catch(APPNotFoundException e) {
            throw e;
        }
        catch(APPUnauthorizedException e) {
            throw e;
        }
        catch(IllegalArgumentException e) {
            throw new APPBadRequestException(45,"Unacceptable ID.");
        }
        catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }
/*
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
//        if (!json.has("userId"))
//            throw new APPBadRequestException(55,"Missing user.");
        if (!json.has("targetId"))
            throw new APPBadRequestException(55,"Missing target.");

        try {
            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), false);
            Document doc = new Document();
            //doc.append("userId", json.getString("userId"));
            doc.append("targetId", json.getString("targetId"));
            //Mia added these two lines
            doc.append("albumName", json.getString("albumName"));
            doc.append("albumDate", json.getString("albumDate"));
            //doc.append("isCreator", false);
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
*/
    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers,  Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));

//            if (!json.has("userId"))
//                throw new APPBadRequestException(55,"Missing user.");
            if (!json.has("targetId"))
                throw new APPBadRequestException(55,"Missing target.");

            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), true);
            BasicDBObject query = new BasicDBObject();
            query.put("targetId", json.getString("targetId"));
            //Mia added these two lines
            query.put("albumName", json.getString("albumName"));
            query.put("albumDate", json.getString("albumDate"));
//            query.put("userId", json.getString("userId"));
            DeleteResult deleteResult = collection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete editor.");
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
