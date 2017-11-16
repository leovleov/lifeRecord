package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import cmu.sv.lifeRecord.models.Album;
import cmu.sv.lifeRecord.models.Target;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

@Path("targets")
public class TargetInterface {
    private MongoCollection<Document> albumCollection;
    private MongoCollection<Document> targetCollection;
    private ObjectWriter ow;


    public TargetInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");

        this.albumCollection = database.getCollection("albums");
        this.targetCollection = database.getCollection("targets");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    }

    @GET
    @Path("{id}/albums")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOneAlbum(@Context HttpHeaders headers, @PathParam("id") String id) {
        ArrayList<Album> albumList = new ArrayList<>();
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            BasicDBObject query = new BasicDBObject();

            query.put("targetId", id);
            FindIterable<Document> results = albumCollection.find(query);
            if (results == null) {
                return new APPResponse(albumList);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Document item : results) {
                Album album = new Album(
                        item.getString("targetId"),
                        item.getString("albumName"),
                        sdf.format(item.getDate("albumDate"))
                );
                album.setId(item.getObjectId("_id").toString());
                albumList.add(album);
            }
            return new APPResponse(albumList);

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

    @POST
    @Path("{id}/albums")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {

        JSONObject json = null;
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("albumName"))
                throw new APPBadRequestException(55,"Missing album name.");
//            if (!json.has("targetId"))
//                throw new APPBadRequestException(55,"Missing target.");

            Document doc = new Document();
            doc.append("targetId", id);
            doc.append("albumName", json.getString("albumName"));
            doc.append("albumDate", new Date());
            albumCollection.insertOne(doc);
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

}
