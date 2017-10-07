package cmu.sv.lifeRecord.rest;

import cmu.sv.lifeRecord.models.Picture;
import cmu.sv.lifeRecord.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("pictures")
public class PicturesInterface {
    private MongoCollection<Document> collection = null;
    private ObjectWriter ow;

    public PicturesInterface() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("liferecord");
        collection = database.getCollection("pictures");
        ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public ArrayList<Picture> getAll() {

        ArrayList<Picture> carList = new ArrayList<Picture>();

        try {
            FindIterable<Document> results = collection.find();
            for (Document item : results) {
                //String make = item.getString("make");
                Picture pic = new Picture(
                        item.getString("url"),
                        item.getString("recordId"),
                        item.getDate("date")
                );
                pic.setId(item.getObjectId("_id").toString());
                carList.add(pic);
            }
            return carList;

        } catch(Exception e) {
            System.out.println("GetAll EXCEPTION!!!!");
            e.printStackTrace();
            throw new APPInternalServerException(99,e.getMessage());
        }

    }
}
