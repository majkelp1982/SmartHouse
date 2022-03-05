package pl.pomazanka.SmartHouse.backend.dataStruct;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.common.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

@Controller
@Getter
@Setter
public class Module_SolarPanels extends Module implements Cloneable {

  // Module heating type
  private static final byte MODULE_TYPE = 99;
  @Autowired Module_Heating module_heating;
  int powerEnableLimit;
  int powerResetLimit;
  double reqHeatTempCO;
  // Values only to read
  //	private String webdata_sn = "SF4ES006M7S394  ";
  //	private String webdata_msvn = "G310";
  //	private String webdata_ssvn = "";
  //	private String webdata_pv_type = "SF4ES006";
  //	private String webdata_rate_p = "";
  private double webdata_now_p; // = "740";
  private double webdata_today_e; // = "0.48";
  private double webdata_total_e; // = "50.0";
  private String webdata_alarm = "";
  private boolean isAutoconsumption;
  //	private String cover_sta_ip = "192.168.0.220";
  //	private String cover_sta_mac = "34:EA:E7:EC:96:CA";
  //	private String status_a = "1";
  //	private String status_b = "0";
  //	private String status_c = "0";
  //	private String webdata_utime = "0";
  //	private String cover_mid = "2300094760";
  //	private String cover_ver = "LSW3_15_FFFF_1.0.57";
  //	private String cover_wmode = "APSTA";
  //	private String cover_ap_ssid = "AP_2300094760";
  //	private String cover_ap_ip = "10.10.100.254";
  //	private String cover_ap_mac = "30:EA:E7:EC:96:CA";
  //	private String cover_sta_ssid = "Majkel";
  private String cover_sta_rssi;
  // = "49%";

  private transient float oldReqTempBufferC0;

  Module_SolarPanels() throws Exception {
    super(MODULE_TYPE, "Fotowoltaika", "module_solarPanels");
  }

  public void autoConsumption() {
    isAutoconsumption = !isAutoconsumption;
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
    if (!isAutoconsumption) {
      oldReqTempBufferC0 = module_heating.getReqTempBufferCO();
    }

    if (isAutoconsumption && (getWebdata_now_p() >= powerEnableLimit)) {
      module_heating.setNVCheapTariffOnly(false);
      module_heating.setNVReqTempBufferCO(reqHeatTempCO);
      module_heating.setReqUpdateValues(true);
    } else if (!isAutoconsumption || getWebdata_now_p() < powerResetLimit) {
      module_heating.setNVCheapTariffOnly(true);
      module_heating.setNVReqTempBufferCO(oldReqTempBufferC0);
      module_heating.setReqUpdateValues(true);
    }

    setUpToDate(true);
    return isUpToDate();
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
