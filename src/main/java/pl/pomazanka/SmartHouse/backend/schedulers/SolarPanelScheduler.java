package pl.pomazanka.SmartHouse.backend.schedulers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_SolarPanels;

import java.time.LocalDateTime;

@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@AllArgsConstructor
@Service
@Slf4j
public class SolarPanelScheduler {
  @Autowired Module_SolarPanels solarPanels;
  @Autowired MongoDBController mongoDBController;
  private Module_SolarPanels module_solarPanelsLastSaved;

  @Scheduled(fixedDelay = 60000)
  public void solarPanelsScheduler() throws CloneNotSupportedException {
    log.info("Scheduler triggered");
    solarPanels.saveWebData();
    if (!solarPanels.compare(module_solarPanelsLastSaved)) {
      log.info("New data will be saved for module: solarPanels");
      module_solarPanelsLastSaved = solarPanels.clone();
      mongoDBController.saveNewEntry(solarPanels.getModuleStructureName(), solarPanels);
      solarPanels.setLastSaveDateTime(LocalDateTime.now());
    }
  }
}
