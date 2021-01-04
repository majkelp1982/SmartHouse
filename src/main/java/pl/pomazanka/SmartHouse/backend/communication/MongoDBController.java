package pl.pomazanka.SmartHouse.backend.communication;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.dataStruct.*;
import pl.pomazanka.SmartHouse.backend.security.UserInstance;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Autowired
    Module_Vent2 module_vent2;
    @Autowired
    Diagnostic diagnostic;

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
            case 13 : {
                Module_Vent2 module_vent2LastStatus = module_vent2.clone();
                module_vent2.dataParser(packetData);
                if (!module_vent2.compare(module_vent2LastStatus))
                    saveNewEntry("module_vent2", module_vent2);                    // if data has been changed add new entry in DB
                else updateLastEntry("module_vent2", module_vent2);                // else update last entry

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

    public void saveNewEntry(String collectionName, Object object) {
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

    public void dropCollection (String collectionName) {
        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        mongoCollection.drop();
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

    public ArrayList<Charts.Data> getValues(String collectionName, String variableName, LocalDateTime from, LocalDateTime to) throws Exception {
        ArrayList<Charts.Data> list = new ArrayList<>();

        MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
        BasicDBObject gtQuery = new BasicDBObject();
        gtQuery.put("frameLastUpdate.date.day", new BasicDBObject("$gte", from.getDayOfMonth()).append("$lte", to.getDayOfMonth()));

        FindIterable iterable =  mongoCollection.find(gtQuery);
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            String jsonDoc = iterator.next().toString();
            LocalDateTime dateTime = getDateTimeFromJson(jsonDoc);
            String value = getValue(jsonDoc, variableName);

            if (!value.equals("0.0") && !value.equals("10.0") && (!value.equals("100.0")))
                list.add(new Charts.Data(dateTime, value));
        }
        return list;
    }

    public ArrayList<Charts.VariableList> refreshVariables() {
        ArrayList<Charts.VariableList> variableList = new ArrayList<>();
        ArrayList<String> variablesListByModule = new ArrayList<>();

        diagnostic.getModules().forEach(moduleDiagInfo -> {
            String moduleStructureName = moduleDiagInfo.getModuleStructureName();
            MongoCollection mongoCollection = mongoDatabase.getCollection(moduleStructureName);
            FindIterable iterable =  mongoCollection.find().limit(1).sort(new Document("_id",-1));
            Iterator iterator = iterable.iterator();
            if (iterator.hasNext()) {
                String jsonDoc = iterator.next().toString();
                variablesListByModule.addAll(jsonDocAnalyse(moduleStructureName, jsonDoc));
            }
        });

        variablesListByModule.forEach(item -> {
            variableList.add(new Charts.VariableList(item.toString(), false));
        });

        //Get last VariableList
        MongoCollection mongoCollection = mongoDatabase.getCollection("chart_variable_list");
        FindIterable<Document> iterable = mongoCollection.find();
        for (Document doc : iterable) {
            String jsonDoc = doc.toString();
            int startIndex = jsonDoc.indexOf("variableName=");
            int endIndex = jsonDoc.indexOf(",",startIndex);
            String variableName = jsonDoc.substring(startIndex+13,endIndex);

            startIndex = jsonDoc.indexOf("enabled=");
            endIndex = jsonDoc.indexOf("}",startIndex);
            String value = jsonDoc.substring(startIndex+8,endIndex);

            Iterator iterator = variableList.iterator();
            while (iterator.hasNext()) {
                Charts.VariableList variable = (Charts.VariableList) iterator.next();
                if (variable.getVariableName().equals(variableName)) {
                    variable.setEnabled((value.equals("true") ? true : false));
                }
            }
        }
            return variableList;
    }

    private ArrayList<String> jsonDocAnalyse(String collectionName, String jsonDoc) {
        ArrayList<String> list = new ArrayList<>();
        int cursor = 0;
        int tempIndex = 0;
        String variableName;
        String subString;
        int variableArrayNo=0;
        //skip 'Document{{'
        cursor += 10;
        int length = jsonDoc.length();

        while ((cursor<length) && (cursor != 1)){
                subString = "";
            variableArrayNo = 0;
            //find cursor index variable end
            tempIndex = jsonDoc.indexOf("=", cursor);
            subString = jsonDoc.substring(tempIndex + 1, tempIndex + 2);
            if (subString.equals("[")) {
                variableName = jsonDoc.substring(cursor, tempIndex);
                cursor = tempIndex + 1;
                subString = jsonDoc.substring(cursor+1, cursor + 11);

                if (subString.equals("Document{{")) {
                    int endIndex = jsonDoc.indexOf("]",cursor);
                    while (cursor < endIndex) {
                        ArrayList<String> subList = new ArrayList<>();
                        tempIndex = jsonDoc.indexOf("}}", cursor);
                        subString = jsonDoc.substring(cursor + 1, tempIndex);
                        subList = jsonDocAnalyse("",subString);
                        cursor = tempIndex+2;
                        final String prefixName = collectionName+"."+variableName + "[" + variableArrayNo + "]";

                        subList.forEach(action -> {
                            list.add(prefixName + action.toString());
                        });
                        variableArrayNo++;
                        cursor +=1;
                    }
                    cursor++;
                } else {
                    int endIndexInnerArray = jsonDoc.indexOf("]", cursor);
                    cursor++;
                    while (cursor < endIndexInnerArray) {
                        list.add(collectionName+"."+variableName + "[" + variableArrayNo + "]");
                        variableArrayNo++;
                        cursor = jsonDoc.indexOf(",", cursor+1);
                    }
                    cursor ++;
                }
            } else {
                subString = jsonDoc.substring(cursor, tempIndex);
                if (subString.equals("moduleType")) break;

                if (!subString.equals("_id")) list.add(collectionName+"."+subString);
                cursor = jsonDoc.indexOf(", ",tempIndex)+1;
            }
            cursor++;
        }
        return list;
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
            LocalDateTime temp = LocalDateTime.of(year, month, day, hour, minute, second);

            //FIXME offset 2 hours needed
            LocalDateTime temp1 = temp.plusHours(2);
            return temp1;
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
