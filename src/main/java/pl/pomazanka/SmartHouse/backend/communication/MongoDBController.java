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
import pl.pomazanka.SmartHouse.backend.dataStruct.*;
import pl.pomazanka.SmartHouse.backend.security.UserInstance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

@Service
public class MongoDBController {

	@Autowired
	Module_Heating module_heating;
	@Autowired
	Module_Comfort module_comfort;
	@Autowired
	Module_Vent module_vent;
	@Autowired
	Module_Weather module_weather;
	@Autowired
	Module_Sewage module_sewage;
	@Autowired
	Module_ExtLights module_extLights;
	@Autowired
	Diagnostic diagnostic;
	//MongoDB
	private MongoClient mongoClient = new MongoClient("localhost", 27017);
	private MongoDatabase mongoDatabase = mongoClient.getDatabase("house");

	private Module_Comfort module_comfortLastSaved;
	private Module_Weather module_weatherLastSaved;
	private Module_Sewage module_sewageLastSaved;
	private Module_Vent module_ventLastSaved;
	private Module_Heating module_heatingLastSaved;
	private Module_ExtLights module_extLightsLastSaved;

	public MongoDBController() throws CloneNotSupportedException {
	}

	public void saveUDPFrame(int[] packetData) throws CloneNotSupportedException {
		int moduleType = packetData[0];             // Get basic data from UDP frame

		switch (moduleType) {
			case 10: {
				module_comfort.dataParser(packetData);
				if (!module_comfort.compare(module_comfortLastSaved)) {
					saveNewEntry("module_comfort", module_comfort);             // if data has been changed add new entry in DB
					module_comfort.setLastSaveDateTime(LocalDateTime.now());
					module_comfortLastSaved = module_comfort.clone();
				} else updateLastEntry("module_comfort", module_comfort);         // else update last entry

			}
			break;
			case 11: {
				module_weather.dataParser(packetData);
				if (!module_weather.compare(module_weatherLastSaved)) {
					saveNewEntry("module_weather", module_weather);             // if data has been changed add new entry in DB
					module_weather.setLastSaveDateTime(LocalDateTime.now());
					module_weatherLastSaved = module_weather.clone();
					} else updateLastEntry("module_weather", module_weather);         // else update last entry

			}
			break;
			case 12: {
				module_sewage.dataParser(packetData);
				if (!module_sewage.compare(module_sewageLastSaved)) {
					saveNewEntry("module_sewage", module_sewage);             // if data has been changed add new entry in DB
					module_sewage.setLastSaveDateTime(LocalDateTime.now());
					module_sewageLastSaved = module_sewage.clone();
				} else updateLastEntry("module_weather", module_weather);         // else update last entry

			}
			break;
			case 13: {
				module_vent.dataParser(packetData);
				if (!module_vent.compare(module_ventLastSaved)) {
					saveNewEntry("module_vent", module_vent);                    // if data has been changed add new entry in DB
					module_vent.setLastSaveDateTime(LocalDateTime.now());
					module_ventLastSaved = module_vent.clone();
				} else updateLastEntry("module_vent", module_vent);                // else update last entry

			}
			break;
			case 14: {
				module_heating.dataParser(packetData);
				if (!module_heating.compare(module_heatingLastSaved)) {
					saveNewEntry("module_heating", module_heating);             // if data has been changed add new entry in DB
					module_heating.setLastSaveDateTime(LocalDateTime.now());
					module_heatingLastSaved = module_heating.clone();
				} else updateLastEntry("module_heating", module_heating);         // else update last entry
			}
			break;
			case 16: {
				module_extLights.dataParser(packetData);
				if (!module_extLights.compare(module_extLightsLastSaved)) {
					saveNewEntry("module_extLights", module_extLights);             // if data has been changed add new entry in DB
					module_extLights.setLastSaveDateTime(LocalDateTime.now());
					module_extLightsLastSaved = module_extLights.clone();
				} else updateLastEntry("module_extLights", module_extLights);         // else update last entry
			}
			break;
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

	public void dropCollection(String collectionName) {
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
		Document documentToUpdate = (Document) mongoCollection.find().limit(1).sort(new Document("_id", -1)).first();
		if (documentToUpdate != null) mongoCollection.deleteOne(documentToUpdate);
		mongoCollection.insertOne(documentNew);
	}

	public ArrayList<Charts.Data> getValues(String collectionName, String variableName, LocalDateTime from, LocalDateTime to) throws Exception {
		ArrayList<Charts.Data> list = new ArrayList<>();
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
		BasicDBObject gtQuery = new BasicDBObject();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		Long fromString = Long.valueOf(from.format(formatter));
		Long toString = Long.valueOf(to.format(formatter));
		gtQuery.put("localDateTimeLong", new BasicDBObject("$gte", fromString).append("$lte", toString));
		Gson gson = new Gson();
		for (Document doc : mongoCollection.find(gtQuery)) {
			String jsonDoc = gson.toJson(doc);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode nodeDoc = mapper.readTree(jsonDoc);
			JsonNode valueNode = null;
			int arrayIndex = variableName.indexOf("[");
			if (arrayIndex != -1) {
				String varName = variableName.substring(0, arrayIndex);
				String arrayNumber = variableName.substring(arrayIndex + 1, arrayIndex + 2);
				String variable = variableName.substring(arrayIndex + 4);
				JsonNode varNameNode = nodeDoc.get(varName);
				JsonNode arrayNumberNode = varNameNode.get(Integer.valueOf(arrayNumber));
				JsonNode variableNode = arrayNumberNode.get(variable);
				valueNode = variableNode;
			} else if (variableName.contains(".")) {
				JsonNode subNode = nodeDoc.get(variableName.substring(0, variableName.indexOf(".")));
				valueNode = subNode.get(variableName.substring(variableName.indexOf(".") + 1));
			} else {
				valueNode = nodeDoc.get(variableName);
			}

			String value = valueNode.toString();
			LocalDateTime dateTime = getDateTimeFromJson(nodeDoc);

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
			MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(moduleStructureName);
//            Document document = (Document) mongoCollection.find().limit(1).sort(new Document("_id", -1));
			for (Document document : mongoCollection.find().limit(1).sort(new Document("_id", -1))) {
				try {
					variablesListByModule.addAll(getFields(moduleStructureName, document));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
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
			int endIndex = jsonDoc.indexOf(",", startIndex);
			String variableName = jsonDoc.substring(startIndex + 13, endIndex);

			startIndex = jsonDoc.indexOf("enabled=");
			endIndex = jsonDoc.indexOf("}", startIndex);
			String value = jsonDoc.substring(startIndex + 8, endIndex);

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

	private ArrayList<String> getFields(String collectionName, Document mongoDoc) throws JsonProcessingException {
		ArrayList<String> variableList = new ArrayList<>();
		Gson gson = new Gson();
		String jsonDoc = gson.toJson(mongoDoc);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(jsonDoc);
		Iterator<Map.Entry<String, JsonNode>> rootIterator = root.fields();
		while (rootIterator.hasNext()) {
			String variable = collectionName;
			Map.Entry<String, JsonNode> field = rootIterator.next();
			getVariable(variableList, field, variable);
		}
		return variableList;
	}

	private void getVariable(ArrayList<String> variableList, Map.Entry<String, JsonNode> field, String name) {
		String key = field.getKey();
		if (key.toUpperCase().contains("_ID")
				|| (key.toUpperCase().contains("UPDATE"))
				|| (key.toUpperCase().contains("DATETIME"))
				|| (key.toUpperCase().contains("MODULE")))
			return;
		String variable = name + "." + key;
		Iterator<Map.Entry<String, JsonNode>> childIterator = field.getValue().fields();
		if (childIterator.hasNext()) {
			while (childIterator.hasNext()) {
				Map.Entry<String, JsonNode> child = childIterator.next();
				getVariable(variableList, child, variable);
			}
		} else {
			Iterator<JsonNode> iterElem = field.getValue().elements();
			if (iterElem.hasNext()) {
				int i = 0;
				while (iterElem.hasNext()) {
					JsonNode node = iterElem.next();
					Iterator<String> namesIterator = node.fieldNames();
					while (namesIterator.hasNext()) {
						variableList.add(variable + "[" + i + "]." + namesIterator.next());
					}
					i++;
				}
			} else variableList.add(variable);
		}
	}

	public UserDetails getUser() {
		MongoCollection mongoCollection = mongoDatabase.getCollection("Users");
		Document doc = (Document) mongoCollection.find().first();
		UserInstance userInstance = new Gson().fromJson(doc.toJson(), UserInstance.class);

		return userInstance;
	}

	private LocalDateTime getDateTimeFromJson(JsonNode nodeDoc) {
		JsonNode node = nodeDoc.get("frameLastUpdate");
		JsonNode dateNode = node.get("date");
		JsonNode timeNode = node.get("time");
		try {
			int year = Integer.valueOf((dateNode.get("year").toString()));
			int month = Integer.valueOf(dateNode.get("month").toString());
			int day = Integer.valueOf(dateNode.get("day").toString());
			int hour = Integer.valueOf(timeNode.get("hour").toString());
			int minute = Integer.valueOf(timeNode.get("minute").toString());
			int second = Integer.valueOf(timeNode.get("second").toString());
			LocalDateTime temp = LocalDateTime.of(year, month, day, hour, minute, second);
			LocalDateTime temp1 = temp.plusHours(1);
			return temp1;
		} catch (Exception e) {
			throw e;
		}
	}
}
