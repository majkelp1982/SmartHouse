package pl.pomazanka.SmartHouse;

import com.github.appreciated.apexcharts.helper.Coordinate;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class test {

    public static void main(String[] args) throws Exception {
        MongoDBController mongoDBController = new MongoDBController();
        //FIXME
        mongoDBController.getVariables("module_comfort");
    }
}
