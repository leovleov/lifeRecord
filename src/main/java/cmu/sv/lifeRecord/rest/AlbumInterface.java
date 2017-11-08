package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import cmu.sv.lifeRecord.helpers.PATCH;
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
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@Path("albums")
public class AlbumInterface {
    private MongoCollection<Document> collection = null;
    private MongoCollection<Document> albumCollection;
    private ObjectWriter ow;

    public AlbumInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        albumCollection = database.getCollection("albums");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }


    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOne(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            BasicDBObject query = new BasicDBObject();

            query.put("_id", new ObjectId(id));
            Document item = albumCollection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such album.");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Album album = new Album(
                        item.getString("targetId"),
                        item.getString("albumName"),
                        sdf.format(item.getDate("albumDate"))
                );
            album.setId(item.getObjectId("_id").toString());

            AuthCheck.checkEditorOrWatcherAuthentication(headers, album.getTargetId());
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

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse update(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {
        JSONObject json = null;

        try {
            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), true);
            json = new JSONObject(ow.writeValueAsString(request));

//            if (json.has("emailAddress"))
//                throw new APPBadRequestException(33, "Email address can't be updated.");

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            Document doc = new Document();
            if (json.has("targetId"))
                doc.append("targetId",json.getString("targetId"));
            if (json.has("albumName"))
                doc.append("albumName",json.getString("albumName"));
            if (json.has("albumDate"))
                doc.append("albumDate",json.getString("albumDate"));

            Document set = new Document("$set", doc);
            albumCollection.updateOne(query,set);
            return new APPResponse(request);

        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch(APPBadRequestException e){
            throw e;
        } catch(JSONException e) {
            throw new APPBadRequestException(33,"Failed to patch a document.");
        } catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }

    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers,  Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));


            if (!json.has("targetId"))
                throw new APPBadRequestException(55,"Missing target.");

            AuthCheck.checkEditorAuthentication(headers, json.getString("targetId"), true);
            BasicDBObject query = new BasicDBObject();
            query.put("targetId", json.getString("targetId"));
            query.put("albumName", json.getString("albumName"));
            query.put("albumDate", json.getString("albumDate"));
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
