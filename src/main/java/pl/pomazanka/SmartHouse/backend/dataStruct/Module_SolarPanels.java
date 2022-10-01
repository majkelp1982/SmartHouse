package pl.pomazanka.SmartHouse.backend.dataStruct;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.common.Logger;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;

@Controller
@Getter
@Setter
public class Module_SolarPanels extends Module implements Cloneable {

  // Module heating type
  private static final byte MODULE_TYPE = 20;
  private static final int DELAY_TIME = 600;
  @Autowired transient Module_Heating module_heating;
  @Autowired transient MongoDBController mongoDBController;
  int forceCOBufferEnableLimit;
  int forceCOBufferResetLimit;
  int forceWaterSuperHeatEnableLimit;
  int forceWaterSuperHeatResetLimit;
  double reqHeatTempCO;
  private transient LocalDateTime stateChangeTime = LocalDateTime.now();
  private double webdata_now_p;
  private double webdata_today_e;
  private double webdata_total_e;
  private String webdata_alarm = "";
  private boolean forceCOBufferEnabled = true;
  private boolean forceWaterSuperHeatEnabled = true;
  private boolean forceCOBufferActive;
  private String cover_sta_rssi;

  private transient float oldReqTempBufferC0;

  Module_SolarPanels() throws Exception {
    super(MODULE_TYPE, "Fotowoltaika", "module_solarPanels");
  }

  @Override
  @PostConstruct
  public void postConstructor() {
    final Module_SolarPanels module_solarPanels =
        mongoDBController.getLastSolarPanelsValues(getModuleStructureName());
    try {
      forceCOBufferEnableLimit = module_solarPanels.getForceCOBufferEnableLimit();
      forceCOBufferResetLimit = module_solarPanels.getForceCOBufferResetLimit();
      forceWaterSuperHeatEnableLimit = module_solarPanels.getForceWaterSuperHeatEnableLimit();
      forceWaterSuperHeatResetLimit = module_solarPanels.getForceWaterSuperHeatResetLimit();
      reqHeatTempCO = module_solarPanels.getReqHeatTempCO();
    } catch (Exception e) {
      // TODO
    }
  }

  public void forceCOBuffer() {
    forceCOBufferEnabled = !forceCOBufferEnabled;
    if (!forceCOBufferEnabled) {
      resetForceCOBuffer();
    }
  }

  public void forceWaterSuperHeat() {
    forceWaterSuperHeatEnabled = !forceWaterSuperHeatEnabled;
    if (!forceWaterSuperHeatEnabled) {
      module_heating.setNVWaterSuperheat(false);
      module_heating.setReqUpdateValues(true);
    }
  }

  public void saveWebData() {
    isAllUpToDate();
    final StringBuilder source = new StringBuilder();
    try {
      Authenticator.setDefault(
          new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("admin", "admin".toCharArray());
            }
          });
      final URL url = new URL("http://192.168.0.220/status.html");
      final URLConnection connection = url.openConnection();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      final BufferedReader in =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      do {
        line = in.readLine();
        source.append(line);
      } while (line != null);
      webdata_now_p = Double.valueOf(extractValue("webdata_now_p", source.toString()));
      webdata_today_e = Double.valueOf(extractValue("webdata_today_e", source.toString()));
      webdata_total_e = Double.valueOf(extractValue("webdata_total_e", source.toString()));
      webdata_alarm = extractValue("webdata_alarm", source.toString());
      cover_sta_rssi = extractValue("cover_sta_rssi", source.toString());
      setFrameLastUpdate(getCurrentDate());
      setDiagnosticLastUpdate(getCurrentDate());
    } catch (final Exception e) {
      //      webdata_now_p = -100.00;
      Logger.error("Wyjątek przy pobraniu danych z fotowoltaiki: " + e);
    }
  }

  public boolean isAllUpToDate() {
    checkForceCOBuffer();
    checkWaterSuperHeat();
    setUpToDate(true);
    return isUpToDate();
  }

  private void resetForceCOBuffer() {
    module_heating.setNVCheapTariffOnly(true);
    module_heating.setNVReqTempBufferCO(oldReqTempBufferC0);
    module_heating.setReqUpdateValues(true);
    forceCOBufferActive = false;
  }

  private void checkWaterSuperHeat() {
    if (!forceWaterSuperHeatEnabled || forceWaterSuperHeatEnableLimit == 0) {
      return;
    }
    final long currentTimeMillis = System.currentTimeMillis();
    final long processStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
    final long upTimeMillis = currentTimeMillis - processStartTime;
    if (upTimeMillis < 30000) {
      return;
    }

    if (getWebdata_now_p() >= forceWaterSuperHeatEnableLimit) {
      module_heating.setNVWaterSuperheat(true);
      module_heating.setReqUpdateValues(true);
    }
    if (getWebdata_now_p() < forceWaterSuperHeatResetLimit) {
      module_heating.setNVWaterSuperheat(false);
      module_heating.setReqUpdateValues(true);
    }
  }

  private void checkForceCOBuffer() {
    if (!forceCOBufferActive) {
      oldReqTempBufferC0 = module_heating.getReqTempBufferCO();
      if (getWebdata_now_p() < forceCOBufferEnableLimit) {
        stateChangeTime = LocalDateTime.now().plusSeconds(DELAY_TIME);
      }
    } else if (getWebdata_now_p() > forceCOBufferResetLimit) {
      stateChangeTime = LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    if (!forceCOBufferEnabled) {
      return;
    }

    if (LocalDateTime.now().isAfter(stateChangeTime)) {
      if (getWebdata_now_p() >= forceCOBufferEnableLimit) {
        module_heating.setNVCheapTariffOnly(false);
        module_heating.setNVReqTempBufferCO(reqHeatTempCO);
        module_heating.setReqUpdateValues(true);
        forceCOBufferActive = true;
      }
      if (getWebdata_now_p() < forceCOBufferResetLimit) {
        forceCOBufferActive = false;
        resetForceCOBuffer();
      }
    }
  }

  @Override
  protected void assignNV(final Object object) throws Exception {}

  @Override
  void faultListInit() throws Exception {
    setFaultText(0, "Alarm falownika fotowoltaiki");
  }

  @Override
  void faultCheck() {
    // Clear previous faults status
    resetFaultPresent();

    setFaultPresent(0, getWebdata_alarm().length() > 0);

    updateGlobalFaultList();
  }

  // compare data : last save status with new set
  public boolean compare(final Module_SolarPanels module_solarPanels) {
    if (module_solarPanels == null) {
      return false;
    }
    boolean result = true;
    if (result) {
      result = cmp(module_solarPanels.webdata_now_p, webdata_now_p, 300);
    }
    if (result) {
      result = cmp(module_solarPanels.forceCOBufferEnableLimit, forceCOBufferEnableLimit, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.forceCOBufferResetLimit, forceCOBufferResetLimit, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.reqHeatTempCO, reqHeatTempCO, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.forceCOBufferEnabled, forceCOBufferEnabled);
    }
    if (result) {
      result = cmp(module_solarPanels.forceCOBufferActive, forceCOBufferActive);
    }
    if (result) {
      result =
          cmp(module_solarPanels.forceWaterSuperHeatEnableLimit, forceWaterSuperHeatEnableLimit, 0);
    }
    if (result) {
      result =
          cmp(module_solarPanels.forceWaterSuperHeatResetLimit, forceWaterSuperHeatResetLimit, 0);
    }
    if (isTooLongWithoutSave()) {
      result = false;
    }
    return result;
  }

  @Override
  public Module_SolarPanels clone() throws CloneNotSupportedException {
    final Module_SolarPanels module_sewage = (Module_SolarPanels) super.clone();
    return module_sewage;
  }

  private String extractValue(final String name, final String source) {
    final String key = name + " = \"";
    final String subSource = source.substring(source.indexOf(key) + key.length());
    return subSource.substring(0, subSource.indexOf("\""));
  }
}
