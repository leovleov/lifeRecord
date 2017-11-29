package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.exceptions.APPBadRequestException;
import cmu.sv.lifeRecord.exceptions.APPInternalServerException;
import cmu.sv.lifeRecord.exceptions.APPNotFoundException;
import cmu.sv.lifeRecord.exceptions.APPUnauthorizedException;
import cmu.sv.lifeRecord.helpers.APPCrypt;
import cmu.sv.lifeRecord.helpers.APPListResponse;
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
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
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
import java.util.regex.Pattern;

import static java.lang.System.err;

@Path("targets")
public class TargetInterface {
    private MongoCollection<Document> albumCollection;
    private MongoCollection<Document> targetCollection;
    private MongoCollection<Document> editorCollection;
    private MongoCollection<Document> watcherCollection;
    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> recordCollection;
    private MongoCollection<Document> picCollection;
    private ObjectWriter ow;


    public TargetInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");

        this.albumCollection = database.getCollection("albums");
        this.targetCollection = database.getCollection("targets");
        this.editorCollection = database.getCollection("editors");
        this.watcherCollection = database.getCollection("watchers");
        this.userCollection = database.getCollection("users");
        this.recordCollection = database.getCollection("records");
        this.picCollection = database.getCollection("pictures");
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
    public APPListResponse getTargetAlbums(@Context HttpHeaders headers, @PathParam("id") String id,
                                       @DefaultValue("4") @QueryParam("count") int count,
                                       @DefaultValue("0") @QueryParam("offset") int offset) {
        ArrayList<Album> albumList = new ArrayList<>();
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            BasicDBObject query = new BasicDBObject();

            query.put("targetId", id);
            long resultCount = albumCollection.count(query);
            FindIterable<Document> results = albumCollection.find(query).skip(offset).limit(count);
            if (results == null) {
//                return new APPResponse(albumList);
                return new APPListResponse(albumList,resultCount,offset, albumList.size());
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
            //return new APPResponse(albumList);
            return new APPListResponse(albumList,resultCount,offset, albumList.size());

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
    @Path("{id}/records")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getTargetRecords(@Context HttpHeaders headers, @PathParam("id") String id) {
        ArrayList<Record> recordList = new ArrayList<>();
        try {
            AuthCheck.checkEditorAuthentication(headers,id,false);
            BasicDBObject query = new BasicDBObject();

            query.put("targetId", id);
            FindIterable<Document> results = recordCollection.find(query);
            if (results == null) {
                return new APPResponse(recordList);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Document item : results) {
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
    @Path("{id}/pictures")
    @Produces({MediaType.APPLICATION_JSON})
    public APPListResponse getPicturesForTarget(@Context HttpHeaders headers, @PathParam("id") String id,
                                                @DefaultValue("_id") @QueryParam("sort") String sortArg,
                                                @DefaultValue("20") @QueryParam("count") int count,
                                                @DefaultValue("0") @QueryParam("offset") int offset) {

        ArrayList<Picture> picList = new ArrayList<Picture>();

        try {
            AuthCheck.checkWatcherAuthentication(headers,id);
            BasicDBObject sortParams = new BasicDBObject();
            List<String> sortList = Arrays.asList(sortArg.split(","));
            sortList.forEach(sortItem -> {
                sortParams.put(sortItem,1);
            });

            BasicDBObject query = new BasicDBObject();
            query.put("targetId", id);

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

            BasicDBObject queryCheck = new BasicDBObject();
            queryCheck.put("targetName", json.getString("targetName"));
            queryCheck.put("creatorId", userId);
            Document itemCheck = targetCollection.find(queryCheck).first();
            if (itemCheck != null) {
                throw new APPBadRequestException(56, "The target name already exists.");
            }

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

    @DELETE
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            AuthCheck.checkEditorAuthentication(headers, id, true);
            BasicDBObject queryCheckNum = new BasicDBObject();
            queryCheckNum.put("targetId", id);
            long resultCount = recordCollection.count(queryCheckNum);
            if(resultCount != 0)
                throw new APPBadRequestException(67, "Please clean the record first.");

            DeleteResult deleteResultEditor = editorCollection.deleteMany(queryCheckNum);
            DeleteResult deleteResultWatcher = watcherCollection.deleteMany(queryCheckNum);

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            DeleteResult deleteResult = targetCollection.deleteOne(query);
            if (deleteResult.getDeletedCount() < 1)
                throw new APPNotFoundException(66, "Could not delete the record.");

            return new APPResponse(new JSONObject());
        } catch(APPUnauthorizedException e) {
            throw e;
        } catch(APPBadRequestException e) {
            throw e;
        } catch (APPNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new APPInternalServerException(99, "Unexpected error!");
        }
    }

    @POST
    @Path("{id}/albums")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createAlbums(@Context HttpHeaders headers, @PathParam("id") String targetId, Object request) {

        JSONObject json = null;
        try {
            AuthCheck.checkEditorAuthentication(headers,targetId,false);
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("albumName"))
                throw new APPBadRequestException(55,"Missing album name.");
//            if (!json.has("targetId"))
//                throw new APPBadRequestException(55,"Missing target.");

            Document doc = new Document();
            doc.append("targetId", targetId);
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

    @POST
    @Path("{id}/records")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse createRecords(@Context HttpHeaders headers, @PathParam("id") String targetId, Object request) {

        JSONObject json = null;
        try {
            String userId = AuthCheck.checkEditorAuthentication(headers,targetId,false);
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("recordName"))
                throw new APPBadRequestException(55,"Missing record name.");
            if (!json.has("recordInfo"))
                throw new APPBadRequestException(55,"Missing Info.");

            Document doc = new Document();
            doc.append("userId", userId);
            doc.append("recordName", json.getString("recordName"));
            doc.append("recordInfo", json.getString("recordInfo"));
            doc.append("targetId", targetId);
            doc.append("createDate", new Date());
            doc.append("updateDate", new Date());
            if(json.has("albumId"))
                doc.append("albumId", json.getString("albumId"));
            else
                doc.append("albumId","");
            recordCollection.insertOne(doc);
            ObjectId recordId = (ObjectId)doc.get( "_id" );
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Record record = new Record(
                    (String)doc.get( "recordName" ),
                    (String)doc.get( "recordInfo" ),
                    (String)doc.get( "albumId" ),
                    (String)doc.get( "targetId" ),
                    (String)doc.get( "userId" ),
                    sdf.format((Date) doc.get( "createDate" )),
                    sdf.format((Date) doc.get( "updateDate" ))
            );
            record.setId(recordId.toString());
            return new APPResponse(record);
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
