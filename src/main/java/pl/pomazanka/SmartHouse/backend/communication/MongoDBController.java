package pl.pomazanka.SmartHouse.backend.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.common.Logger;
import pl.pomazanka.SmartHouse.backend.dataStruct.*;
import pl.pomazanka.SmartHouse.backend.security.UserInstance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Service
public class MongoDBController {

  // MongoDB
  private final MongoClient mongoClient = new MongoClient("192.168.0.200", 27017);
  private final MongoDatabase mongoDatabase = mongoClient.getDatabase("house");
  @Autowired Module_Heating module_heating;
  @Autowired Module_Comfort module_comfort;
  @Autowired Module_Vent module_vent;
  @Autowired Module_Weather module_weather;
  @Autowired Module_Sewage module_sewage;
  @Autowired Module_ExtLights module_extLights;
  @Autowired Diagnostic diagnostic;
  private Module_Comfort module_comfortLastSaved;
  private Module_Weather module_weatherLastSaved;
  private Module_Sewage module_sewageLastSaved;
  private Module_Vent module_ventLastSaved;
  private Module_Heating module_heatingLastSaved;
  private Module_ExtLights module_extLightsLastSaved;

  public MongoDBController() throws CloneNotSupportedException {}

  public void saveUDPFrame(final int[] packetData) throws CloneNotSupportedException {
    final int moduleType = packetData[0]; // Get basic data from UDP frame

    Logger.debug("Obsługa komunikatu UDP");
    switch (moduleType) {
      case 10:
        {
          module_comfort.dataParser(packetData);
          if (!module_comfort.compare(module_comfortLastSaved)) {
            saveNewEntry(
                "module_comfort", module_comfort); // if data has been changed add new entry in DB
            module_comfort.setLastSaveDateTime(LocalDateTime.now());
            module_comfortLastSaved = module_comfort.clone();
          } else {
            updateLastEntry("module_comfort", module_comfort); // else update last entry
          }
        }
        break;
      case 11:
        {
          module_weather.dataParser(packetData);
          if (!module_weather.compare(module_weatherLastSaved)) {
            saveNewEntry(
                "module_weather", module_weather); // if data has been changed add new entry in DB
            module_weather.setLastSaveDateTime(LocalDateTime.now());
            module_weatherLastSaved = module_weather.clone();
          } else {
            updateLastEntry("module_weather", module_weather); // else update last entry
          }
        }
        break;
      case 12:
        {
          module_sewage.dataParser(packetData);
          if (!module_sewage.compare(module_sewageLastSaved)) {
            saveNewEntry(
                "module_sewage", module_sewage); // if data has been changed add new entry in DB
            module_sewage.setLastSaveDateTime(LocalDateTime.now());
            module_sewageLastSaved = module_sewage.clone();
          } else {
            updateLastEntry("module_weather", module_weather); // else update last entry
          }
        }
        break;
      case 13:
        {
          module_vent.dataParser(packetData);
          if (!module_vent.compare(module_ventLastSaved)) {
            saveNewEntry(
                "module_vent", module_vent); // if data has been changed add new entry in DB
            module_vent.setLastSaveDateTime(LocalDateTime.now());
            module_ventLastSaved = module_vent.clone();
          } else {
            updateLastEntry("module_vent", module_vent); // else update last entry
          }
        }
        break;

      case 14:
        {
          module_heating.dataParser(packetData);
          if (!module_heating.compare(module_heatingLastSaved)) {
            saveNewEntry(
                "module_heating", module_heating); // if data has been changed add new entry in DB
            module_heating.setLastSaveDateTime(LocalDateTime.now());
            module_heatingLastSaved = module_heating.clone();
          } else {
            updateLastEntry("module_heating", module_heating); // else update last entry
          }
        }
        break;
      case 16:
        {
          module_extLights.dataParser(packetData);
          if (!module_extLights.compare(module_extLightsLastSaved)) {
            saveNewEntry(
                "module_extLights",
                module_extLights); // if data has been changed add new entry in DB
            module_extLights.setLastSaveDateTime(LocalDateTime.now());
            module_extLightsLastSaved = module_extLights.clone();
          } else {
            updateLastEntry("module_extLights", module_extLights); // else update last entry
          }
        }
        break;
    }
    Logger.debug("Obsługa komunikatu UDP zakończona");
  }

  public void saveNotice(final String source, final String text) {
    // FIXME check if working
    final Document document = new Document();
    document.put(source, text);
    final MongoCollection mongoCollection = mongoDatabase.getCollection("Messages");
    mongoCollection.insertOne(document);
  }

  public void saveError(final String source, final String text) {
    // FIXME check if working
    final Document document = new Document();
    document.put(source, text);
    final MongoCollection mongoCollection = mongoDatabase.getCollection("Error");
    mongoCollection.insertOne(document);
  }

  public void saveNewEntry(final String collectionName, final Object object) {
    Logger.debug("Zapis kolekcji do bazy");
    // Help variables
    final Gson gson = new Gson();
    final String json;

    // Parsing class data to JSON
    json = gson.toJson(object);

    // create BSON document
    final Document documentActual = Document.parse(json);
    final MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
    mongoCollection.insertOne(documentActual);
    Logger.debug("Zapis kolekcji do bazy zakończone");
  }

  public void dropCollection(final String collectionName) {
    final MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
    mongoCollection.drop();
  }

  private void updateLastEntry(final String collectionName, final Object object) {
    Logger.debug("Aktualizacja kolekcji w bazie");
    // Help variables
    final Gson gson = new Gson();
    final String json;

    // Parsing class data to JSON
    json = gson.toJson(object);

    // create BSON document
    final Document documentNew = Document.parse(json);
    final MongoCollection mongoCollection = mongoDatabase.getCollection(collectionName);
    final Document documentToUpdate =
        (Document) mongoCollection.find().limit(1).sort(new Document("_id", -1)).first();
    if (documentToUpdate != null) {
      mongoCollection.deleteOne(documentToUpdate);
    }
    mongoCollection.insertOne(documentNew);
    Logger.debug("Aktualizacja kolekcji w bazie zakończona");
  }

  public ArrayList<Charts.Data> getValues(
      final String collectionName,
      final String variableName,
      final LocalDateTime from,
      final LocalDateTime to)
      throws Exception {
    final ArrayList<Charts.Data> list = new ArrayList<>();
    final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
    final BasicDBObject gtQuery = new BasicDBObject();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    final Long fromString = Long.valueOf(from.format(formatter));
    final Long toString = Long.valueOf(to.format(formatter));
    gtQuery.put(
        "localDateTimeLong", new BasicDBObject("$gte", fromString).append("$lte", toString));
    final Gson gson = new Gson();
    for (final Document doc : mongoCollection.find(gtQuery)) {
      final String jsonDoc = gson.toJson(doc);
      final ObjectMapper mapper = new ObjectMapper();
      final JsonNode nodeDoc = mapper.readTree(jsonDoc);
      JsonNode valueNode = null;
      final int arrayIndex = variableName.indexOf("[");
      if (arrayIndex != -1) {
        final String varName = variableName.substring(0, arrayIndex);
        final String arrayNumber = variableName.substring(arrayIndex + 1, arrayIndex + 2);
        final String variable = variableName.substring(arrayIndex + 4);
        final JsonNode varNameNode = nodeDoc.get(varName);
        final JsonNode arrayNumberNode = varNameNode.get(Integer.valueOf(arrayNumber));
        final JsonNode variableNode = arrayNumberNode.get(variable);
        valueNode = variableNode;
      } else if (variableName.contains(".")) {
        final JsonNode subNode = nodeDoc.get(variableName.substring(0, variableName.indexOf(".")));
        valueNode = subNode.get(variableName.substring(variableName.indexOf(".") + 1));
      } else {
        valueNode = nodeDoc.get(variableName);
      }

      final String value = valueNode.toString();
      final LocalDateTime dateTime = getDateTimeFromJson(nodeDoc);

      if (!value.equals("0.0") && !value.equals("10.0") && (!value.equals("100.0"))) {
        list.add(new Charts.Data(dateTime, value));
      }
    }
    return list;
  }

  public ArrayList<Charts.VariableList> refreshVariables() {
    final ArrayList<Charts.VariableList> variableList = new ArrayList<>();
    final ArrayList<String> variablesListByModule = new ArrayList<>();

    diagnostic
        .getModules()
        .forEach(
            moduleDiagInfo -> {
              final String moduleStructureName = moduleDiagInfo.getModuleStructureName();
              final MongoCollection<Document> mongoCollection =
                  mongoDatabase.getCollection(moduleStructureName);
              //            Document document = (Document) mongoCollection.find().limit(1).sort(new
              // Document("_id", -1));
              for (final Document document :
                  mongoCollection.find().limit(1).sort(new Document("_id", -1))) {
                try {
                  variablesListByModule.addAll(getFields(moduleStructureName, document));
                } catch (final JsonProcessingException e) {
                  e.printStackTrace();
                }
              }
            });

    variablesListByModule.forEach(
        item -> {
          variableList.add(new Charts.VariableList(item.toString(), false));
        });

    // Get last VariableList
    final MongoCollection mongoCollection = mongoDatabase.getCollection("chart_variable_list");
    final FindIterable<Document> iterable = mongoCollection.find();
    for (final Document doc : iterable) {
      final String jsonDoc = doc.toString();
      int startIndex = jsonDoc.indexOf("variableName=");
      int endIndex = jsonDoc.indexOf(",", startIndex);
      final String variableName = jsonDoc.substring(startIndex + 13, endIndex);

      startIndex = jsonDoc.indexOf("enabled=");
      endIndex = jsonDoc.indexOf("}", startIndex);
      final String value = jsonDoc.substring(startIndex + 8, endIndex);

      final Iterator iterator = variableList.iterator();
      while (iterator.hasNext()) {
        final Charts.VariableList variable = (Charts.VariableList) iterator.next();
        if (variable.getVariableName().equals(variableName)) {
          variable.setEnabled((value.equals("true") ? true : false));
        }
      }
    }
    return variableList;
  }

  private ArrayList<String> getFields(final String collectionName, final Document mongoDoc)
      throws JsonProcessingException {
    final ArrayList<String> variableList = new ArrayList<>();
    final Gson gson = new Gson();
    final String jsonDoc = gson.toJson(mongoDoc);
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode root = mapper.readTree(jsonDoc);
    final Iterator<Map.Entry<String, JsonNode>> rootIterator = root.fields();
    while (rootIterator.hasNext()) {
      final String variable = collectionName;
      final Map.Entry<String, JsonNode> field = rootIterator.next();
      getVariable(variableList, field, variable);
    }
    return variableList;
  }

  private void getVariable(
      final ArrayList<String> variableList,
      final Map.Entry<String, JsonNode> field,
      final String name) {
    final String key = field.getKey();
    if (key.toUpperCase().contains("_ID")
        || (key.toUpperCase().contains("UPDATE"))
        || (key.toUpperCase().contains("DATETIME"))
        || (key.toUpperCase().contains("MODULE"))) {
      return;
    }
    final String variable = name + "." + key;
    final Iterator<Map.Entry<String, JsonNode>> childIterator = field.getValue().fields();
    if (childIterator.hasNext()) {
      while (childIterator.hasNext()) {
        final Map.Entry<String, JsonNode> child = childIterator.next();
        getVariable(variableList, child, variable);
      }
    } else {
      final Iterator<JsonNode> iterElem = field.getValue().elements();
      if (iterElem.hasNext()) {
        int i = 0;
        while (iterElem.hasNext()) {
          final JsonNode node = iterElem.next();
          final Iterator<String> namesIterator = node.fieldNames();
          while (namesIterator.hasNext()) {
            variableList.add(variable + "[" + i + "]." + namesIterator.next());
          }
          i++;
        }
      } else {
        variableList.add(variable);
      }
    }
  }

  public UserDetails getUser() {
    final MongoCollection mongoCollection = mongoDatabase.getCollection("Users");
    final Document doc = (Document) mongoCollection.find().first();
    final UserInstance userInstance = new Gson().fromJson(doc.toJson(), UserInstance.class);

    return userInstance;
  }

  private LocalDateTime getDateTimeFromJson(final JsonNode nodeDoc) {
    final JsonNode node = nodeDoc.get("frameLastUpdate");
    final JsonNode dateNode = node.get("date");
    final JsonNode timeNode = node.get("time");
    try {
      final int year = Integer.valueOf((dateNode.get("year").toString()));
      final int month = Integer.valueOf(dateNode.get("month").toString());
      final int day = Integer.valueOf(dateNode.get("day").toString());
      final int hour = Integer.valueOf(timeNode.get("hour").toString());
      final int minute = Integer.valueOf(timeNode.get("minute").toString());
      final int second = Integer.valueOf(timeNode.get("second").toString());
      final LocalDateTime temp = LocalDateTime.of(year, month, day, hour, minute, second);
      final LocalDateTime temp1 = temp.plusHours(1);
      return temp1;
    } catch (final Exception e) {
      throw e;
    }
  }
}
