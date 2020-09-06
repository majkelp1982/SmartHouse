package pl.pomazanka.SmartHouse.backend.dataStruct;

import com.github.appreciated.apexcharts.helper.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
public class Charts {

    //Wired classes
    @Autowired
    MongoDBController mongoDBController;

    public Charts () {
    }

    public Coordinate[] getSerie (String collectionName, String variableName, LocalDateTime from, LocalDateTime to) throws Exception {
        //FIXME
        ArrayList<Data> list;
        list = mongoDBController.getEntry(collectionName, variableName, from, to);
        Coordinate[] serie = new Coordinate[list.size()];

        int i = 0;
        for (Data temp : list) {
            if (temp.isNumber) serie[i] = new Coordinate<>(temp.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), temp.getDouble());
            else serie[i] = new Coordinate<>(temp.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), temp.getBoolean());
            i++;
        }
        return serie;
    }

    public ArrayList<String> getVariables (String collectionName) {
        return mongoDBController.getVariables(collectionName);
    }

    public static class Data{
        private LocalDateTime timeStamp;
        private String value;
        private boolean isNumber;

        public  Data(LocalDateTime timeStamp, String value) throws Exception {
            this.timeStamp = timeStamp;
            this.value = value;
            if (tryParseDouble(value))
                isNumber = true;
            else
                if (tryParseBoolean(value))
                    isNumber = false;
                else throw new Exception("Value not a number and not a boolean");
        }

        public LocalDateTime getTimeStamp() {
            return timeStamp;
        }

        public double getDouble() throws Exception {
            if (isNumber) return Double.parseDouble(value);
            else throw new Exception("Value not a Double format");
        }

        public boolean getBoolean() throws Exception {
            if (!isNumber) return Boolean.parseBoolean(value);
            else throw new Exception("Value not a Boolean format");
        }

        public boolean isNumber() {
            return isNumber;
        }

        private boolean tryParseDouble(String value) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private boolean tryParseBoolean(String value) {
            try {
                Boolean.parseBoolean(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

}
