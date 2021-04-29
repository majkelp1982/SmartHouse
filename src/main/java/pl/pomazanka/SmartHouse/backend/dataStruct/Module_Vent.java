package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.BME280;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.Fan;

@Controller
public class Module_Vent extends Module implements Cloneable {
	//Module ventilation type
	private transient static byte MODULE_TYPE = 13;
	private transient final byte ID_CZERPNIA = 0;
	private transient final byte ID_WYRZUTNIA = 1;
	private transient final byte ID_NAWIEW = 2;
	private transient final byte ID_WYWIEW = 3;

	private boolean fanON;
	private boolean normalON;
	private boolean humidityAlert;
	private boolean bypassOpen;
	private boolean defrost;
	private transient int[] hour = new int[12];
	private transient int[] NVHour = new int[12];
	private BME280[] bme280 = new BME280[4];
	private Fan[] fan = new Fan[2];
	private int defrostTimeLeft;
	private int defrostTrigger;
	private int NVDefrostTrigger;
	private int humidityTimeLeft;
	private int humidityTrigger;
	private int NVHumidityTrigger;
	private int efficiency;

	public Module_Vent() {
		super(MODULE_TYPE, "Wentylacja", "module_vent");
		for (int i = 0; i < 4; i++)
			bme280[i] = new BME280();
		for (int i = 0; i < 2; i++)
			fan[i] = new Fan();
	}

	@Override
	protected boolean isUpToDate() {
		return super.isUpToDate();
	}

	public boolean isFanON() {
		return fanON;
	}

	public boolean isNormalON() {
		return normalON;
	}

	public boolean isHumidityAlert() {
		return humidityAlert;
	}

	public boolean isBypassOpen() {
		return bypassOpen;
	}

	public boolean isDefrost() {
		return defrost;
	}

	public int[] getHour() {
		return hour;
	}

	public int[] getNVHour() {
		return NVHour;
	}

	public int getDefrostTimeLeft() {
		return defrostTimeLeft;
	}

	public int getDefrostTrigger() {
		return defrostTrigger;
	}

	public int getNVDefrostTrigger() {
		return NVDefrostTrigger;
	}

	public void setNVDefrostTrigger(int NVDefrostTrigger) {
		this.NVDefrostTrigger = NVDefrostTrigger;
		setUpToDate(false);
	}

	public int getHumidityTimeLeft() {
		return humidityTimeLeft;
	}

	public int getHumidityTrigger() {
		return humidityTrigger;
	}

	public int getNVHumidityTrigger() {
		return NVHumidityTrigger;
	}

	public void setNVHumidityTrigger(int NVHumidityTrigger) {
		this.NVHumidityTrigger = NVHumidityTrigger;
		setUpToDate(false);
	}

	public int getEfficiency() {
		return efficiency;
	}

	// Check if values are Up to Date
	public void setNVHour01(int NVHour) {
		this.NVHour[0] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour23(int NVHour) {
		this.NVHour[1] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour45(int NVHour) {
		this.NVHour[2] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour67(int NVHour) {
		this.NVHour[3] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour89(int NVHour) {
		this.NVHour[4] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour1011(int NVHour) {
		this.NVHour[5] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour1213(int NVHour) {
		this.NVHour[6] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour1415(int NVHour) {
		this.NVHour[7] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour1617(int NVHour) {
		this.NVHour[8] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour1819(int NVHour) {
		this.NVHour[9] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour2021(int NVHour) {
		this.NVHour[10] = NVHour;
		setUpToDate(false);
	}

	public void setNVHour2223(int NVHour) {
		this.NVHour[11] = NVHour;
		setUpToDate(false);
	}

	public BME280[] getBme280() {
		return bme280;
	}

	public Fan[] getFan() {
		return fan;
	}

	public boolean isAllUpToDate() {
		setUpToDate(true);
		for (int i = 0; i <= 11; i++) {
			if (isUpToDate()) setUpToDate(hour[i] == NVHour[i]);
		}
		if (isUpToDate()) setUpToDate(defrostTrigger == NVDefrostTrigger);
		if (isUpToDate()) setUpToDate(humidityTrigger == NVHumidityTrigger);

		setReqUpdateValues(!isUpToDate());
		return isUpToDate();
	}

	//Parser for data package coming via UDP
	public void dataParser(int[] packetData) {
		int controllerFrameNumber = packetData[2];

		switch (controllerFrameNumber) {
			case 0: // standard frame 0
				fanON = bitStatus(packetData[3], 7);
				normalON = bitStatus(packetData[3], 6);
				humidityAlert = bitStatus(packetData[3], 5);
				bypassOpen = bitStatus(packetData[3], 4);
				defrost = bitStatus(packetData[3], 3);

				// copy relevant data received into array
				for (int i = 0; i < 12; i++)
					hour[i] = packetData[4 + i];
				for (int i = 0; i < 4; i++) {
					double tValue = packetData[16 + i * 4] + packetData[16 + i * 4 + 1] / 10.0;
					bme280[i].setTemp(getFloatValue(tValue));
					bme280[i].setHumidity(packetData[16 + i * 4 + 2]);
					bme280[i].setPressure(packetData[16 + i * 4 + 3] * 10);
				}

				fan[ID_CZERPNIA].setSpeed(packetData[32]);
				fan[ID_CZERPNIA].setRev(packetData[33] * 100);
				fan[ID_WYRZUTNIA].setSpeed(packetData[32]);
				fan[ID_WYRZUTNIA].setRev(packetData[34] * 100);

				defrostTimeLeft = packetData[35];
				defrostTrigger = packetData[36];
				humidityTimeLeft = packetData[37];
				humidityTrigger = packetData[38];
				efficiency = packetData[39];
				break;

			case 200: //diagnostic frame
				updateDiag(packetData);
				break;
		}
		super.dataParser(packetData);
	}

	@Override
	void faultListInit() throws Exception {

	}

	@Override
	void faultCheck() {
		updateGlobalFaultList();
	}

	@Override
	void assignNV() {
		NVHour = hour.clone();
		NVDefrostTrigger = defrostTrigger;
		NVHumidityTrigger = humidityTrigger;
	}

	public boolean compare(Module_Vent module_vent) {
		//return FALSE if compare data are different
		boolean result = true;
		if (result) result = cmp(module_vent.fanON, fanON);
		if (result) result = cmp(module_vent.normalON, normalON);
		if (result) result = cmp(module_vent.humidityAlert, humidityAlert);
		if (result) result = cmp(module_vent.bypassOpen, bypassOpen);
		if (result) result = cmp(module_vent.defrost, defrost);
		for (int i = 0; i < 12; i++)
			if (result) result = cmp(module_vent.hour[i], hour[i]);
		for (int i = 0; i < 4; i++) {
			if (result) result = cmp(module_vent.bme280[i].getTemp(), bme280[i].getTemp(), 1);
			if (result) result = cmp(module_vent.bme280[i].getPressure(), bme280[i].getPressure(), 5);
			if (result) result = cmp(module_vent.bme280[i].getHumidity(), bme280[i].getHumidity(), 3);
		}
		for (int i = 0; i < 2; i++) {
			if (result) result = cmp(module_vent.fan[i].getSpeed(), fan[i].getSpeed(), 10);
			if (result) result = cmp(module_vent.fan[i].getRev(), module_vent.fan[i].getRev(), 100);
		}
		if (result) result = cmp(module_vent.efficiency, efficiency);
		if (isTooLongWithoutSave())
			result = false;
		return result;
	}

	@Override
	public Module_Vent clone() throws CloneNotSupportedException {
		Module_Vent module_vent = (Module_Vent) super.clone();
		module_vent.hour = hour.clone();
		module_vent.NVHour = NVHour.clone();
		module_vent.bme280 = bme280.clone();
		module_vent.fan = fan.clone();
		return module_vent;
	}
}