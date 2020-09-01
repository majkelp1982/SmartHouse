package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
public class Charts {

    //Wired classes
    @Autowired
    MongoDBController mongoDBController;

    public Charts () {
    }

    public void getEntry () throws Exception {
        //FIXME
        ArrayList<Data> data = new ArrayList<>();

        Module_Heating module_heating = new Module_Heating();
        module_heating = mongoDBController.getEntry("module_heating", LocalDateTime.now(), LocalDateTime.now());

    }

    private class Data{
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
