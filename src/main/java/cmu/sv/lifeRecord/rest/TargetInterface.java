package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPCrypt;
import cmu.sv.lifeRecord.helpers.APPResponse;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import cmu.sv.lifeRecord.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Path("targets")
public class TargetInterface {
    private MongoCollection<Document> albumCollection;
    private MongoCollection<Document> targetCollection;
    private MongoCollection<Document> editorCollection;
    private MongoCollection<Document> watcherCollection;
    private MongoCollection<Document> userCollection;
    private ObjectWriter ow;


    public TargetInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");

        this.albumCollection = database.getCollection("albums");
        this.targetCollection = database.getCollection("targets");
        this.editorCollection = database.getCollection("editors");
        this.watcherCollection = database.getCollection("watchers");
        this.userCollection = database.getCollection("users");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOne(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            AuthCheck.checkEditorOrWatcherAuthentication(headers,id);
            BasicDBObject query = new BasicDBObject();

            query.put("_id", new ObjectId(id));
            Document item = targetCollection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such target.");
            }
            Target target = new Target(
                    item.getString("targetName"),
                    item.getString("targetInfo"),
                    item.getString("creatorId")
            );
            target.setId(item.getObjectId("_id").toString());
            return new APPResponse(target);

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

    @GET
    @Path("{id}/albums")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getTargetAlbums(@Context HttpHeaders headers, @PathParam("id") String id) {
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

    @GET
    @Path("{id}/editors")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getTargetEditors(@Context HttpHeaders headers, @PathParam("id") String id) {
        ArrayList<User> userList = new ArrayList<>();
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            BasicDBObject query = new BasicDBObject();

            query.put("targetId", id);
            FindIterable<Document> results = editorCollection.find(query);
            if (results == null) {
                return new APPResponse(userList);
            }

            for (Document item : results) {
                BasicDBObject query2 = new BasicDBObject();

                query2.put("_id", new ObjectId(item.getString("userId")));
                Document item2 = userCollection.find(query2).first();
                if (item2 == null) {
                    throw new APPNotFoundException(0, "No such user.");
                }
                User user = new User(
                        item2.getString("firstName"),
                        item2.getString("lastName"),
                        item2.getString("nickName"),
                        item2.getString("phoneNumber"),
                        item2.getString("emailAddress"),
                        item2.getBoolean("isAdmin")
                );
                user.setId(item2.getObjectId("_id").toString());
                userList.add(user);
            }
            return new APPResponse(userList);

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

    @GET
    @Path("{id}/watchers")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getTargetWatchers(@Context HttpHeaders headers, @PathParam("id") String id) {
        ArrayList<User> userList = new ArrayList<>();
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            BasicDBObject query = new BasicDBObject();

            query.put("targetId", id);
            FindIterable<Document> results = watcherCollection.find(query);
            if (results == null) {
                return new APPResponse(userList);
            }

            for (Document item : results) {
                BasicDBObject query2 = new BasicDBObject();

                query2.put("_id", new ObjectId(item.getString("userId")));
                Document item2 = userCollection.find(query2).first();
                if (item2 == null) {
                    throw new APPNotFoundException(0, "No such user.");
                }
                User user = new User(
                        item2.getString("firstName"),
                        item2.getString("lastName"),
                        item2.getString("nickName"),
                        item2.getString("phoneNumber"),
                        item2.getString("emailAddress"),
                        item2.getBoolean("isAdmin")
                );
                user.setId(item2.getObjectId("_id").toString());
                userList.add(user);
            }
            return new APPResponse(userList);

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
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse create(@Context HttpHeaders headers, Object request) {

        JSONObject json = null;
        try {
            AuthCheck.checkAnyAuthentication(headers);
            List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null)
                throw new APPUnauthorizedException(70, "No Authorization Headers");
            String token = authHeaders.get(0);
            String userId = APPCrypt.decrypt(token);

            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("targetName"))
                throw new APPBadRequestException(55, "Missing target name.");
            if (!json.has("targetInfo"))
                throw new APPBadRequestException(55, "Missing target Info.");
            BasicDBObject query = new BasicDBObject();

            Document doc = new Document("targetName", json.getString("targetName"))
                    .append("targetInfo", json.getString("targetInfo"))
                    .append("creatorId", userId);
            targetCollection.insertOne(doc);

            BasicDBObject query2 = new BasicDBObject();
            query2.put("targetName", json.getString("targetName"));
            query2.put("creatorId", userId);
            Document item2 = targetCollection.find(query2).first();
            if (item2 == null) {
                throw new APPNotFoundException(0, "No such target.");
            }
            Target target = new Target(
                    item2.getString("targetName"),
                    item2.getString("targetInfo"),
                    item2.getString("creatorId")
            );
            target.setId(item2.getObjectId("_id").toString());

            Document doc2 = new Document();
            doc2.append("userId", userId);
            doc2.append("targetId", item2.getObjectId("_id").toString());
            doc2.append("isCreator", true);
            editorCollection.insertOne(doc2);

            Document doc3 = new Document();
            doc3.append("userId", userId);
            doc3.append("targetId", item2.getObjectId("_id").toString());
            watcherCollection.insertOne(doc3);

            return new APPResponse(target);

        } catch (JsonProcessingException e) {
            throw new APPBadRequestException(33, e.getMessage());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch (APPBadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
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
