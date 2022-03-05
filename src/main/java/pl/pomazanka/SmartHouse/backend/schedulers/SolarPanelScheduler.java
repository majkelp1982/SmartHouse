package pl.pomazanka.SmartHouse.backend.schedulers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_SolarPanels;

@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@AllArgsConstructor
@Service
public class SolarPanelScheduler {
  @Autowired Module_SolarPanels solarPanels;

  @Scheduled(fixedDelay = 60000)
  public void solarPanelsScheduler() {
    solarPanels.saveWebPageData();
  }
}
