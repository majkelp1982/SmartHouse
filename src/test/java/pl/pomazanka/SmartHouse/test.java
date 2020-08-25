package pl.pomazanka.SmartHouse;

import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;

import java.time.LocalDateTime;

public class test {

    public static void main(String[] args) throws Exception {
        Module_Heating module_heating = new Module_Heating();
        MongoDBController mongoDBController = new MongoDBController();
        module_heating = mongoDBController.getEntry("module_heating", LocalDateTime.now(), LocalDateTime.now());
    }
}
