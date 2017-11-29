package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.SimpleUser;
import cmu.sv.lifeRecord.models.Target;
import cmu.sv.lifeRecord.models.Token;
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
import cmu.sv.lifeRecord.helpers.*;
import cmu.sv.lifeRecord.exceptions.*;
import cmu.sv.lifeRecord.models.User;
import cmu.sv.lifeRecord.helpers.AuthCheck;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@Path("users")
public class UsersInterface {
    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> targetCollection;
    private MongoCollection<Document> watcherCollection;
    private MongoCollection<Document> editorCollection;
    private ObjectWriter ow;


    public UsersInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");

        this.userCollection = database.getCollection("users");
        this.targetCollection = database.getCollection("targets");
        this.watcherCollection = database.getCollection("watchers");
        this.editorCollection = database.getCollection("editors");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getAll(@Context HttpHeaders headers) {
        ArrayList<User> userList = new ArrayList<User>();
        try {
            //AuthCheck.checkAdminAuthentication(headers);
            FindIterable<Document> results = userCollection.find();
            if (results == null) {
                return new APPResponse(userList);
            }
            for (Document item : results) {
                User user = new User(
                        item.getString("firstName"),
                        item.getString("lastName"),
                        item.getString("nickName"),
                        item.getString("phoneNumber"),
                        item.getString("emailAddress"),
                        item.getBoolean("isAdmin")
                );
                user.setId(item.getObjectId("_id").toString());
                userList.add(user);
            }
            return new APPResponse(userList);
        }
        catch(APPUnauthorizedException e) {
            throw e;
        }
        catch(Exception e) {
            throw new APPInternalServerException(99,"Unexpected error!");
        }
    }



    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOne(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            AuthCheck.checkOwnAuthentication(headers,id);
            BasicDBObject query = new BasicDBObject();

            query.put("_id", new ObjectId(id));
            Document item = userCollection.find(query).first();
            if (item == null) {
                throw new APPNotFoundException(0, "No such user.");
            }
            User user = new User(
                    item.getString("firstName"),
                    item.getString("lastName"),
                    item.getString("nickName"),
                    item.getString("phoneNumber"),
                    item.getString("emailAddress"),
                    item.getBoolean("isAdmin")
            );
            user.setId(item.getObjectId("_id").toString());
            return new APPResponse(user);

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
    @Path("{id}/targets")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse getOnesTargets(@Context HttpHeaders headers, @PathParam("id") String id) {
        ArrayList<Target> targetList = new ArrayList<>();
        try {
            AuthCheck.checkOwnAuthentication(headers,id);
            BasicDBObject query = new BasicDBObject();

            query.put("creatorId", id);
            FindIterable<Document> results = targetCollection.find(query);
            if (results == null) {
                return new APPResponse(targetList);
            }
            for (Document item : results) {
                Target target = new Target(
                        item.getString("targetName"),
                        item.getString("targetInfo"),
                        item.getString("creatorId")
                );
                target.setId(item.getObjectId("_id").toString());
                targetList.add(target);
            }
            return new APPResponse(targetList);

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
    @Path("targetsWatch")
    @Produces({MediaType.APPLICATION_JSON})
    public APPListResponse getOnesTargetsWatch(@Context HttpHeaders headers,
                                           @DefaultValue("20") @QueryParam("count") int count,
                                           @DefaultValue("0") @QueryParam("offset") int offset) {
        ArrayList<Target> targetList = new ArrayList<>();
        try {
            //AuthCheck.checkOwnAuthentication(headers,id);
            String id = AuthCheck.checkAnyAuthentication(headers);

            BasicDBObject query = new BasicDBObject();
            ArrayList<String> targetIds = new ArrayList<>();
            query.put("userId", id);

            FindIterable<Document> results = editorCollection.find(query);
            if (results != null) {
                for (Document item : results) {
                    targetIds.add(item.getString("targetId"));
                }
            }
            results = watcherCollection.find(query);
            if (results != null) {
                for (Document item : results) {
                    if(!targetIds.contains(item.getString("targetId")))
                        targetIds.add(item.getString("targetId"));
                }
            }
            if(targetIds.isEmpty())
                return new APPListResponse(targetList,targetList.size(),offset,0);
//                return new APPResponse(targetList);
            for(int i = offset; (i < offset+count) && (i < targetIds.size()); i++){
//            Iterator<String> iter = targetIds.iterator();
//            while(iter.hasNext()){
//                String targetId = iter.next();
                String targetId = targetIds.get(i);
                BasicDBObject query1 = new BasicDBObject();
                query1.put("_id", new ObjectId(targetId));
                Document item = targetCollection.find(query1).first();
                Target target = new Target(
                        item.getString("targetName"),
                        item.getString("targetInfo"),
                        item.getString("creatorId")
                );
                target.setId(item.getObjectId("_id").toString());
                targetList.add(target);
            }

            //return new APPResponse(targetList);
            return new APPListResponse(targetList,targetIds.size(),offset,targetList.size());

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

    private BasicDBObject getLikeStr(String findStr) {
        Pattern pattern = Pattern.compile("^.*" + findStr + ".*$", Pattern.CASE_INSENSITIVE);
        return new BasicDBObject("$regex", pattern);
    }

    @GET
    @Path("findUsers/{name}")
    @Produces({MediaType.APPLICATION_JSON})
    public APPResponse findUsers(@Context HttpHeaders headers, @PathParam("name") String name) {
        ArrayList<SimpleUser> userList = new ArrayList();
        try {
            AuthCheck.checkAnyAuthentication(headers);
            BasicDBObject query = new BasicDBObject();

            query.put("firstName", getLikeStr(name));
            FindIterable<Document> results = userCollection.find(query).limit(10);
            if (results == null) {
                return new APPResponse(userList);
            }
            for (Document item : results) {
                SimpleUser user = new SimpleUser(
                        item.getString("firstName"),
                        item.getString("lastName"),
                        item.getString("nickName")
                );
                user.setId(item.getObjectId("_id").toString());
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
    public APPResponse create( Object request) {
        JSONObject json = null;
        try {
            json = new JSONObject(ow.writeValueAsString(request));
            if (!json.has("firstName"))
                throw new APPBadRequestException(55, "Missing firstName.");
            if (!json.has("lastName"))
                throw new APPBadRequestException(55, "Missing lastName.");
            if (!json.has("emailAddress"))
                throw new APPBadRequestException(55, "Missing emailAddress.");
            if (!json.has("password"))
                throw new APPBadRequestException(55, "Missing password.");
            BasicDBObject query = new BasicDBObject();

            query.put("emailAddress",json.getString("emailAddress"));
            Document item = userCollection.find(query).first();
            if (item != null) {
                throw new APPBadRequestException(56, "The emailAddress has existed.");
            }

            Document doc = new Document("firstName", json.getString("firstName"))
                    .append("lastName", json.getString("lastName"))
                    .append("emailAddress", json.getString("emailAddress"))
                    .append("password", APPCrypt.encrypt(json.getString("password")));
            if(json.has("nickName"))
                doc.append("nickName", json.getString("nickName"));
            if(json.has("phoneNumber"))
                doc.append("phoneNumber", json.getString("phoneNumber"));
            doc.append("idAdmin", false);
            userCollection.insertOne(doc);

            BasicDBObject query2 = new BasicDBObject();

            query2.put("emailAddress", json.getString("emailAddress"));
            query2.put("password", APPCrypt.encrypt(json.getString("password")));
            Document item2 = userCollection.find(query2).first();
            if (item2 == null) {
                throw new APPNotFoundException(0, "No user found matching credentials.");
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
            APPResponse r = new APPResponse(new Token(user));
            return r;

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

    @PATCH
    @Path("{id}")
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse update(@Context HttpHeaders headers, @PathParam("id") String id, Object request) {
        JSONObject json = null;

        try {
            AuthCheck.checkOwnAuthentication(headers,id);
            json = new JSONObject(ow.writeValueAsString(request));

            if (json.has("emailAddress"))
                throw new APPBadRequestException(33, "Email address can't be updated.");

            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            Document doc = new Document();
            if (json.has("firstName"))
                doc.append("firstName",json.getString("firstName"));
            if (json.has("lastName"))
                doc.append("lastName",json.getString("lastName"));
            if (json.has("password"))
                doc.append("password",json.getString("password"));
            if (json.has("nickName"))
                doc.append("nickName",json.getString("nickName"));
            if (json.has("phoneNumber"))
                doc.append("phoneNumber",json.getString("phoneNumber"));

            Document set = new Document("$set", doc);
            userCollection.updateOne(query,set);
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
    @Path("{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse delete(@Context HttpHeaders headers, @PathParam("id") String id) {
        try {
            AuthCheck.checkAdminAuthentication(headers);
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(id));

            DeleteResult deleteResult = userCollection.deleteOne(query);
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
}
