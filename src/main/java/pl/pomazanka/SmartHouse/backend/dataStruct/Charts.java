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

    public Coordinate[] getEntry () throws Exception {
        //FIXME
        ArrayList<Data> list;
        Coordinate[] serie = new Coordinate[100];

        list = mongoDBController.getEntry("module_heating", LocalDateTime.now(), LocalDateTime.now());
        int i = 0;
        for (Data temp : list) {

            serie[i] = new Coordinate<>(temp.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),temp.getValue());
            i++;

            //FIXME
            System.out.println(temp.getTimeStamp()+" Value:"+temp.getValue());
        }
        return serie;
    }

    public static class Data{
        private LocalDateTime timeStamp;
        private double value;

        public  Data(LocalDateTime timeStamp, double value) {
            this.timeStamp = timeStamp;
            this.value = value;
        }

        public LocalDateTime getTimeStamp() {
            return timeStamp;
        }

        public double getValue() {
            return value;
        }
    }

}
