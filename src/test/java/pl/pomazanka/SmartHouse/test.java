package pl.pomazanka.SmartHouse;

import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;
import java.util.ArrayList;

public class test {

    public static void main(String[] args) throws Exception {
        MongoDBController mongoDBController = new MongoDBController();
        //FIXME
        mongoDBController.refreshVariables();
    }
}
