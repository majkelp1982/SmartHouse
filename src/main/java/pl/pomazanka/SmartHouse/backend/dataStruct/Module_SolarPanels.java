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
  private static final int DELAY_TIME = 300;
  @Autowired transient Module_Heating module_heating;
  @Autowired transient MongoDBController mongoDBController;
  int powerEnableLimit;
  int powerResetLimit;
  double reqHeatTempCO;
  private transient LocalDateTime stateChangeTime = LocalDateTime.now();
  private double webdata_now_p;
  private double webdata_today_e;
  private double webdata_total_e;
  private String webdata_alarm = "";
  private boolean autoConsumptionEnabled;
  private boolean autoConsumptionActive;
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
    powerEnableLimit = module_solarPanels.getPowerEnableLimit();
    powerResetLimit = module_solarPanels.getPowerResetLimit();
    reqHeatTempCO = module_solarPanels.getReqHeatTempCO();
  }

  public void autoConsumption() {
    autoConsumptionEnabled = !autoConsumptionEnabled;
    if (!autoConsumptionEnabled) {
      resetAutoConsumption();
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
      webdata_now_p = -100.00;
      Logger.error("Wyjątek przy pobraniu danych z fotowoltaiki: " + e);
    }
  }

  public boolean isAllUpToDate() {
    checkAutoConsumption();
    setUpToDate(true);
    return isUpToDate();
  }

  private void resetAutoConsumption() {
    module_heating.setNVCheapTariffOnly(true);
    module_heating.setNVReqTempBufferCO(oldReqTempBufferC0);
    module_heating.setReqUpdateValues(true);
    autoConsumptionActive = false;
  }

  private void checkAutoConsumption() {
    if (!autoConsumptionActive) {
      oldReqTempBufferC0 = module_heating.getReqTempBufferCO();
      if (getWebdata_now_p() < powerEnableLimit) {
        stateChangeTime = LocalDateTime.now().plusSeconds(DELAY_TIME);
      }
    } else if (getWebdata_now_p() > powerResetLimit) {
      stateChangeTime = LocalDateTime.now().plusSeconds(DELAY_TIME);
    }

    if (!autoConsumptionEnabled) {
      return;
    }

    if (LocalDateTime.now().isAfter(stateChangeTime)) {
      if (getWebdata_now_p() >= powerEnableLimit) {
        module_heating.setNVCheapTariffOnly(false);
        module_heating.setNVReqTempBufferCO(reqHeatTempCO);
        module_heating.setReqUpdateValues(true);
        autoConsumptionActive = true;
      }
      if (getWebdata_now_p() < powerResetLimit) {
        autoConsumptionActive = false;
        resetAutoConsumption();
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
      result = cmp(module_solarPanels.powerEnableLimit, powerEnableLimit, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.powerResetLimit, powerResetLimit, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.reqHeatTempCO, reqHeatTempCO, 0);
    }
    if (result) {
      result = cmp(module_solarPanels.autoConsumptionEnabled, autoConsumptionEnabled);
    }
    if (result) {
      result = cmp(module_solarPanels.autoConsumptionActive, autoConsumptionActive);
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
