package cmu.sv.lifeRecord.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.util.List;

@Path("users")
public class UsersInterface {
    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> adminCollection;
    private ObjectWriter ow;


    public UsersInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");

        this.userCollection = database.getCollection("users");
        this.adminCollection = database.getCollection("admins");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public APPResponse getAll(@Context HttpHeaders headers) {
        ArrayList<User> userList = new ArrayList<User>();
        try {
            AuthCheck.checkAdminAuthentication(headers);
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
                        item.getString("emailAddress")
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
                    item.getString("emailAddress")
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
            userCollection.insertOne(doc);
            return new APPResponse(request);
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


}
