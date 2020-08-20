package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

import java.time.LocalDateTime;

@Controller
public class Charts {

    //Wired classes
    @Autowired
    MongoDBController mongoDBController;

    public Charts () {
    }

    public void getEntry () throws Exception {
        //FIXME
        Module_Heating module_heating = new Module_Heating();
        module_heating = mongoDBController.getEntry("module_heating", LocalDateTime.now(), LocalDateTime.now());

        System.out.println(module_heating.toString());
    }

}
