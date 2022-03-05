package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.common.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

@Controller
public class Module_SolarPanels extends Module implements Cloneable {
	//Module heating type
	private static byte MODULE_TYPE = 99;

	// Values only to read
//	private String webdata_sn = "SF4ES006M7S394  ";
//	private String webdata_msvn = "G310";
//	private String webdata_ssvn = "";
//	private String webdata_pv_type = "SF4ES006";
//	private String webdata_rate_p = "";
	private String webdata_now_p = "740";
	private String webdata_today_e = "0.48";
	private String webdata_total_e = "50.0";
	private String webdata_alarm = "";
//	private String webdata_utime = "0";
//	private String cover_mid = "2300094760";
//	private String cover_ver = "LSW3_15_FFFF_1.0.57";
//	private String cover_wmode = "APSTA";
//	private String cover_ap_ssid = "AP_2300094760";
//	private String cover_ap_ip = "10.10.100.254";
//	private String cover_ap_mac = "30:EA:E7:EC:96:CA";
//	private String cover_sta_ssid = "Majkel";
	private String cover_sta_rssi = "49%";
//	private String cover_sta_ip = "192.168.0.220";
//	private String cover_sta_mac = "34:EA:E7:EC:96:CA";
//	private String status_a = "1";
//	private String status_b = "0";
//	private String status_c = "0";

	public Module_SolarPanels() throws Exception {
		super(MODULE_TYPE, "Fotowoltaika", "module_solarPanels");
		faultListInit();
	}

	public String getWebPageData() {
		StringBuilder source = new StringBuilder();
		try {
			Authenticator.setDefault (new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication ("admin", "admin".toCharArray());
				}
			});
			URL url = new URL("http://192.168.0.220/status.html");
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			do {
				line = in.readLine();
				source.append(line);
			} while (line != null);
			setFrameLastUpdate(getCurrentDate());
			setDiagnosticLastUpdate(getCurrentDate());

		} catch (Exception e) {
			Logger.error("Wyjątek przy pobraniu danych z fotowoltaiki: "+ e);
		}
		webdata_now_p = extractValue("webdata_now_p", source.toString());
		webdata_today_e = extractValue("webdata_today_e", source.toString());
		webdata_total_e = extractValue("webdata_total_e", source.toString());
		webdata_alarm = extractValue("webdata_alarm", source.toString());
		cover_sta_rssi = extractValue("cover_sta_rssi", source.toString());
		return source.toString();
	}


	public boolean isAllUpToDate() {
		setUpToDate(true);
		return isUpToDate();
	}

	//Parser for data package coming via UDP
	public void dataParser(int[] packetData) {
		super.dataParser(packetData);
	}

	@Override
	protected void assignNV(Object object) throws Exception {
	}

	@Override
	void faultListInit() throws Exception {
//		setFaultText(0, "Pompa wody przestała działać");
//		setFaultText(1, "Poziom wody przekroczony!!!");
//		setFaultText(2, "Sensor limitu poziomu wody aktywny!!!");
	}

	@Override
	void faultCheck() {
		//Clear previous faults status
		resetFaultPresent();

		//Fault check list
		//TODO
//		setFaultPresent(2, limitSensor);

		updateGlobalFaultList();
	}

	//compare data : last save status with new set
	public boolean compare(Module_SolarPanels module_sewage) {
		if (module_sewage == null)
			return false;
		boolean result = true;
//		if (result) result = cmp(module_sewage.airPump, airPump);
//		if (result) result = cmp(module_sewage.waterPump, waterPump);
//		if (result) result = cmp(module_sewage.limitSensor, limitSensor);
//
//		if (result) result = cmp(module_sewage.isWaterLevel, isWaterLevel, 1);
//		if (result) result = cmp(module_sewage.maxWaterLevel, maxWaterLevel, 0);
//		if (result) result = cmp(module_sewage.minWaterLevel, minWaterLevel, 0);
//		if (result) result = cmp(module_sewage.zeroRefWaterLevel, zeroRefWaterLevel, 0);
//		if (result) result = cmp(module_sewage.intervalAirPump, intervalAirPump, 0);
//		if (isTooLongWithoutSave())
//			result = false;
		return result;
	}

	@Override
	public Module_SolarPanels clone() throws CloneNotSupportedException {
		Module_SolarPanels module_sewage = (Module_SolarPanels) super.clone();
		return module_sewage;
	}

	private String extractValue(String name, String source) {
		String key = name + " = \"";
		String subSource = source.substring(source.indexOf(key)+key.length());
		return subSource.substring(0, subSource.indexOf("\""));
	}
}
