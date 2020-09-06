package pl.pomazanka.SmartHouse.backend.communication;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.atmosphere.interceptor.AtmosphereResourceStateRecovery;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.backend.security.UserInstance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

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
        mongoCollection.insertOne(documentNew);
    }

    public ArrayList<Charts.Data> getEntry(String collectionName, String variableName, LocalDateTime from, LocalDateTime to) throws Exception {
        ArrayList<Charts.Data> list = new ArrayList<>();

        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        BasicDBObject gtQuery = new BasicDBObject();
        //FIXME temporary 0<day<31. Replace with nesesery date in the future
        gtQuery.put("frameLastUpdate.date.day", new BasicDBObject("$gt", 0).append("$lt", 31));

        FindIterable iterable =  mongoCollection.find(gtQuery);
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            String jsonDoc = iterator.next().toString();
            LocalDateTime dateTime = getDateTimeFromJson(jsonDoc);
            String value = getValue(jsonDoc, variableName);
            list.add(new Charts.Data(dateTime, value));
        }
        return list;
    }

    public ArrayList<String> getVariables(String collectionName) {
        ArrayList<String> list = new ArrayList<>();
        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        FindIterable iterable =  mongoCollection.find().limit(1).sort(new Document("_id",-1));
        Iterator iterator = iterable.iterator();
        if (iterator.hasNext()) {

            //FIXME
            System.out.println(iterator.next().toString());
            return list;
        }
        else return null;
    }


        public UserDetails getUser ()  {
        MongoCollection mongoCollection = mongoDatabase.getCollection("Users");
        Document doc = (Document) mongoCollection.find().first();
        UserInstance userInstance = new Gson().fromJson(doc.toJson(), UserInstance.class);

        return userInstance;
    }

    private LocalDateTime getDateTimeFromJson (String jsonDoc) {

        //Get Date
        int startIndex = jsonDoc.indexOf("frameLastUpdate=Document{{date=Document{{");
        int yearIndex = jsonDoc.indexOf("year=", startIndex);
        int comaIndex = jsonDoc.indexOf(",", yearIndex);
        String yearSubstring = jsonDoc.substring(yearIndex + 5, comaIndex);

        int monthIndex = jsonDoc.indexOf("month=", startIndex);
        comaIndex = jsonDoc.indexOf(",", monthIndex);
        String monthSubstring = jsonDoc.substring(monthIndex + 6, comaIndex);

        int dayIndex = jsonDoc.indexOf("day=", startIndex);
        comaIndex = jsonDoc.indexOf("}", dayIndex);
        String daySubstring = jsonDoc.substring(dayIndex + 4, comaIndex);

        int hourIndex = jsonDoc.indexOf("hour=", startIndex);
        comaIndex = jsonDoc.indexOf(",", hourIndex);
        String hourSubstring = jsonDoc.substring(hourIndex + 5, comaIndex);

        int minuteIndex = jsonDoc.indexOf("minute=", startIndex);
        comaIndex = jsonDoc.indexOf(",", minuteIndex);
        String minuteSubstring = jsonDoc.substring(minuteIndex + 7, comaIndex);

        int secondIndex = jsonDoc.indexOf("second=", startIndex);
        comaIndex = jsonDoc.indexOf(",", secondIndex);
        String secondSubstring = jsonDoc.substring(secondIndex + 7, comaIndex);

        try {
            int year = Integer.valueOf(yearSubstring);
            int month = Integer.valueOf(monthSubstring);
            int day = Integer.valueOf(daySubstring);
            int hour = Integer.valueOf(hourSubstring);
            int minute = Integer.valueOf(minuteSubstring);
            int second = Integer.valueOf(secondSubstring);
            return LocalDateTime.of(year, month, day, hour, minute, second);
        }
        catch (NumberFormatException e) {
            System.out.print(jsonDoc.substring(jsonDoc.indexOf("frameLastUpdate=Document{{date=Document{{")));
            System.out.printf("year[%s] month[%s] day[%s] hour[%s] minute[%s] second[%s]\n",
                    yearSubstring,monthSubstring,daySubstring,hourSubstring,minuteSubstring,secondSubstring);
            new Exception(e);
            return null;
        }

    }

    private String getValue (String jsonDoc, String variableName) {
        //Get value
        int firstIndex = jsonDoc.indexOf(variableName)+1;
        int variableNameLength = variableName.length();
        int valueIndexStartsFrom = firstIndex+variableNameLength;
        int comaIndex = jsonDoc.indexOf(",",valueIndexStartsFrom);
        return jsonDoc.substring(valueIndexStartsFrom,comaIndex);
    }

}
