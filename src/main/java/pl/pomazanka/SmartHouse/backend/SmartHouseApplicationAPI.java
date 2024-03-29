package pl.pomazanka.SmartHouse.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;

@RestController
public class SmartHouseApplicationAPI {

  @Autowired private Module_Heating module_heating;
  @Autowired private Module_Comfort module_comfort;

  @GetMapping("/module_heating")
  private Module_Heating getModule_heating() {
    return module_heating;
  }

  @GetMapping("/module_comfort")
  private Module_Comfort getModule_comfort() {
    return module_comfort;
  }
}
