package pl.pomazanka.SmartHouse.backend.communication;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.backend.security.UserInstance;

@Service
public class MongoDBController {

    //MongoDB
    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("house");

    @Autowired
    Module_Heating module_heating;
    @Autowired
    Module_Comfort module_comfort;
    @Autowired
    Module_Vent module_vent;

    public void saveUDPFrame(int[] packetData) throws CloneNotSupportedException {
        int moduleType = packetData[0];             // Get basic data from UDP frame


        switch (moduleType) {
            case 3 : {
                Module_Vent module_ventLastStatus = module_vent.clone();
                module_vent.dataParser(packetData);
                if (!module_vent.compare(module_ventLastStatus))
                    saveNewEntry("module_vent", module_vent);                    // if data has been changed add new entry in DB
                else updateLastEntry("module_vent", module_vent);                // else update last entry

            } break;
            case 10 : {
                Module_Comfort module_comfortLastStatus = module_comfort.clone();
                module_comfort.dataParser(packetData);
                if (!module_comfort.compare(module_comfortLastStatus))
                    saveNewEntry("module_comfort", module_comfort);             // if data has been changed add new entry in DB
                else updateLastEntry("module_comfort", module_comfort);         // else update last entry

            } break;
            case 14 : {
                Module_Heating module_heatingLastStatus = module_heating.clone();
                module_heating.dataParser(packetData);
                if (!module_heating.compare(module_heatingLastStatus))
                    saveNewEntry("module_heating", module_heating);             // if data has been changed add new entry in DB
                else updateLastEntry("module_heating", module_heating);         // else update last entry
            } break;
        }
    }

    public void saveNotice(String source, String text) {
        //FIXME check if working
        Document document = new Document();
        document.put(source, text);
        MongoCollection mongoCollection = mongoDatabase.getCollection("Messages");
        mongoCollection.insertOne(document);
    }

    public void saveError(String source, String text) {
        //FIXME check if working
        Document document = new Document();
        document.put(source, text);
        MongoCollection mongoCollection = mongoDatabase.getCollection("Error");
        mongoCollection.insertOne(document);
    }

    private void saveNewEntry(String collectionName, Object object) {
        // Help variables
        Gson gson = new Gson();
        String json;

        //Parsing class data to JSON
        json = gson.toJson(object);

        //create BSON document
        Document documentActual = Document.parse(json);
        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        mongoCollection.insertOne(documentActual);
    }

    private void updateLastEntry(String collectionName, Object object) {
        // Help variables
        Gson gson = new Gson();
        String json;

        //Parsing class data to JSON
        json = gson.toJson(object);

        //create BSON document
        Document documentNew = Document.parse(json);
        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        Document documentToUpdate = (Document) mongoCollection.find().limit(1).sort(new Document("_id",-1)).first();
        if (documentToUpdate != null) mongoCollection.deleteOne(documentToUpdate);
        mongoCollection.insertOne(documentNew);;
    }

    public UserDetails getUser ()  {
        MongoCollection mongoCollection = mongoDatabase.getCollection("Users");
        Document doc = (Document) mongoCollection.find().first();
        UserInstance userInstance = new Gson().fromJson(doc.toJson(), UserInstance.class);

        return userInstance;
    }
}
