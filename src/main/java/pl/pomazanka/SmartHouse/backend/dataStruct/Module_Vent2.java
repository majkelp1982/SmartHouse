package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.communication.UDPController;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.BME280;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.Fan;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.ControlValue;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.Mode;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.VentZones;

@Controller
public class Module_Vent2 extends Module implements Cloneable {
	//Module ventilation type
	private transient static int MODULE_TYPE = 130;
	private transient final byte ID_CZERPNIA = 0;
	private transient final byte ID_WYRZUTNIA = 1;
	private transient final byte ID_NAWIEW = 2;
	private transient final byte ID_WYWIEW = 3;

	private transient final byte FAN_CZERPNIA = 0;
	private transient final byte FAN_WYWIEW = 1;

	private transient final byte ID_WATER_INLET = 0;
	private transient final byte ID_WATER_OUTLET = 1;
	private transient final byte ID_AIR_INLET = 2;
	private transient final byte ID_AIR_OUTLET = 3;

	// byte 0
	private boolean humidityAlert = false;
	private boolean bypassOpen = false;
	private boolean circuitPump = false;
	private boolean reqPumpColdWater = false;
	private boolean reqPumpHotWater = false;
	private boolean defrostActive;
	private ControlValue reqAutoDiagnosis = new ControlValue(false);

	// byte 1
	private boolean normalOn = false;
	private ControlValue activeCooling = new ControlValue(false);
	private ControlValue activeHeating = new ControlValue(false);
	private ControlValue reqLazDol = new ControlValue(false);
	private ControlValue reqLazGora = new ControlValue(false);
	private ControlValue reqKuchnia = new ControlValue(false);

	//byte 2-17
	private BME280[] bme280 = new BME280[4];

	//byte 18-21
	private Fan[] fan = new Fan[2];

	//byte 22-25
	private Float heatExchanger[] = new Float[4];

	//byte 30
	private Mode normalMode;                        // normal mode structure

	//byte 31-32
	private Mode humidityAlertMode;                    // humidity mode structure

	//byte 33-34
	private Mode defrostMode;                        // defrost mode structure

	//byte 35-58
	private VentZones activeTempRegByHours[] = new VentZones[24];        // active cooling/heating according to hours

	//byte 59
	private ControlValue minTemp = new ControlValue(0);                            // min temp to trigger active cooling in zones. Priority is normal heating. Only when normal heating is to weak then trigger vent heating system

	//byte 60-83
	private VentZones normalOnByHours[] = new VentZones[24];                // active cooling/heating according to hours

	public Module_Vent2() {
		super(MODULE_TYPE, "Wentylacja2", "module_vent2");
		for (int i = 0; i < 4; i++)
			bme280[i] = new BME280();
		for (int i = 0; i < 2; i++)
			fan[i] = new Fan();
		for (int i = 0; i < 4; i++)
			heatExchanger[i] = new Float(0f);
		normalMode = new Mode();
		humidityAlertMode = new Mode();
		defrostMode = new Mode();
		for (int i = 0; i < 24; i++)
			activeTempRegByHours[i] = new VentZones();

		for (int i = 0; i < 24; i++)
			normalOnByHours[i] = new VentZones();
	}

	@Override
	protected boolean isUpToDate() {
		return super.isUpToDate();
	}

	public boolean isHumidityAlert() {
		return humidityAlert;
	}

	public void setHumidityAlert(boolean humidityAlert) {
		this.humidityAlert = humidityAlert;
	}

	public boolean isBypassOpen() {
		return bypassOpen;
	}

	public void setBypassOpen(boolean bypassOpen) {
		this.bypassOpen = bypassOpen;
	}

	public boolean isCircuitPump() {
		return circuitPump;
	}

	public void setCircuitPump(boolean circuitPump) {
		this.circuitPump = circuitPump;
	}

	public boolean isReqPumpColdWater() {
		return reqPumpColdWater;
	}

	public void setReqPumpColdWater(boolean reqPumpColdWater) {
		this.reqPumpColdWater = reqPumpColdWater;
	}

	public boolean isReqPumpHotWater() {
		return reqPumpHotWater;
	}

	public void setReqPumpHotWater(boolean reqPumpHotWater) {
		this.reqPumpHotWater = reqPumpHotWater;
	}

	public boolean isDefrostActive() {
		return defrostActive;
	}

	public void setDefrostActive(boolean defrostActive) {
		this.defrostActive = defrostActive;
	}

	public ControlValue getReqAutoDiagnosis() {
		return reqAutoDiagnosis;
	}

	public void setReqAutoDiagnosis(ControlValue reqAutoDiagnosis) {
		this.reqAutoDiagnosis = reqAutoDiagnosis;
	}

	public boolean isNormalOn() {
		return normalOn;
	}

	public void setNormalOn(boolean normalOn) {
		this.normalOn = normalOn;
	}

	public ControlValue getActiveCooling() {
		return activeCooling;
	}

	public void setActiveCooling(ControlValue activeCooling) {
		this.activeCooling = activeCooling;
	}

	public ControlValue getActiveHeating() {
		return activeHeating;
	}

	public void setActiveHeating(ControlValue activeHeating) {
		this.activeHeating = activeHeating;
	}

	public ControlValue getReqLazDol() {
		return reqLazDol;
	}

	public void setReqLazDol(ControlValue reqLazDol) {
		this.reqLazDol = reqLazDol;
	}

	public ControlValue getReqLazGora() {
		return reqLazGora;
	}

	public void setReqLazGora(ControlValue reqLazGora) {
		this.reqLazGora = reqLazGora;
	}

	public ControlValue getReqKuchnia() {
		return reqKuchnia;
	}

	public void setReqKuchnia(ControlValue reqKuchnia) {
		this.reqKuchnia = reqKuchnia;
	}

	public BME280[] getBme280() {
		return bme280;
	}

	public void setBme280(BME280[] bme280) {
		this.bme280 = bme280;
	}

	public Fan[] getFan() {
		return fan;
	}

	public void setFan(Fan[] fan) {
		this.fan = fan;
	}

	public Float[] getHeatExchanger() {
		return heatExchanger;
	}

	public void setHeatExchanger(Float[] heatExchanger) {
		this.heatExchanger = heatExchanger;
	}

	public Mode getNormalMode() {
		return normalMode;
	}

	public void setNormalMode(Mode normalMode) {
		this.normalMode = normalMode;
	}

	public Mode getHumidityAlertMode() {
		return humidityAlertMode;
	}

	public void setHumidityAlertMode(Mode humidityAlertMode) {
		this.humidityAlertMode = humidityAlertMode;
	}

	public Mode getDefrostMode() {
		return defrostMode;
	}

	public void setDefrostMode(Mode defrostMode) {
		this.defrostMode = defrostMode;
	}

	public VentZones[] getActiveTempRegByHours() {
		return activeTempRegByHours;
	}

	public void setActiveTempRegByHours(VentZones[] activeTempRegByHours) {
		this.activeTempRegByHours = activeTempRegByHours;
	}

	public ControlValue getMinTemp() {
		return minTemp;
	}

	public void setMinTemp(ControlValue minTemp) {
		this.minTemp = minTemp;
	}

	public VentZones[] getNormalOnByHours() {
		return normalOnByHours;
	}

	public void setNormalOnByHours(VentZones[] normalOnByHours) {
		this.normalOnByHours = normalOnByHours;
	}

	public boolean isAllUpToDate() {
		setUpToDate(true);
		if (isUpToDate()) setUpToDate(reqAutoDiagnosis.isUpToDate());
		if (isUpToDate()) setUpToDate(activeCooling.isUpToDate());
		if (isUpToDate()) setUpToDate(activeHeating.isUpToDate());
		if (isUpToDate()) setUpToDate(reqLazDol.isUpToDate());
		if (isUpToDate()) setUpToDate(reqLazGora.isUpToDate());
		if (isUpToDate()) setUpToDate(reqKuchnia.isUpToDate());

		if (isUpToDate()) setUpToDate(normalMode.getTriggerInt().isUpToDate());
		if (isUpToDate()) setUpToDate(humidityAlertMode.getTriggerInt().isUpToDate());
		if (isUpToDate()) setUpToDate(humidityAlertMode.getDelayTime().isUpToDate());
		if (isUpToDate()) setUpToDate(defrostMode.getTriggerInt().isUpToDate());
		if (isUpToDate()) setUpToDate(defrostMode.getDelayTime().isUpToDate());

		for (int i = 0; i < 24; i++) {
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getSalon().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getPralnia().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getLazDol().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getRodzice().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getNatalia().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getKarolina().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(activeTempRegByHours[i].getLazGora().getRequest().isUpToDate());
		}

		if (isUpToDate()) setUpToDate(minTemp.isUpToDate());

		for (int i = 0; i < 24; i++) {
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getSalon().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getPralnia().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getLazDol().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getRodzice().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getNatalia().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getKarolina().getRequest().isUpToDate());
			if (isUpToDate()) setUpToDate(normalOnByHours[i].getLazGora().getRequest().isUpToDate());
		}

		setReqUpdateValues(!isUpToDate());
		return isUpToDate();
	}

	//Parser for data package coming via UDP
	public void dataParser(int[] packetData) {
		int controllerFrameNumber = packetData[2];

		//CUT FIRST 3 BYTES FROM PACKET DATA. FIRST 3 BYTE IS MODULE ID's
		int[] data = new int[UDPController.PACKET_SIZE_MODULE_130 - 3];
		for (int i = 0; i < (UDPController.PACKET_SIZE_MODULE_130 - 3); i++)
			data[i] = packetData[i + 3];

		switch (controllerFrameNumber) {
			case 0: // standard frame 0
				//byte 0
				humidityAlert = bitStatus(data[0], 7);
				bypassOpen = bitStatus(data[0], 6);
				circuitPump = bitStatus(data[0], 5);
				reqPumpColdWater = bitStatus(data[0], 4);
				reqPumpHotWater = bitStatus(data[0], 3);
				defrostActive = bitStatus(data[0], 2);
				reqAutoDiagnosis.setIsValue(bitStatus(data[0], 0));

				//byte 1
				normalOn = bitStatus(data[1], 7);
				activeCooling.setIsValue(bitStatus(data[1], 6));
				activeHeating.setIsValue(bitStatus(data[1], 5));
				reqLazDol.setIsValue(bitStatus(data[1], 4));
				reqLazGora.setIsValue(bitStatus(data[1], 3));
				reqKuchnia.setIsValue(bitStatus(data[1], 2));

				//byte 2-17
				for (int i = 0; i < 4; i++) {
					double tValue = data[2 + i * 4] + packetData[2 + i * 4 + 1] / 10.0;
					bme280[i].setTemp(getFloatValue(tValue));
					bme280[i].setHumidity(data[2 + i * 4 + 2]);
					bme280[i].setPressure(data[2 + i * 4 + 3] * 10);
				}

				//byte 18-21

				fan[ID_CZERPNIA].setSpeed(data[18]);
				fan[ID_CZERPNIA].setRev(data[19] * 100);
				fan[ID_WYRZUTNIA].setSpeed(data[20]);
				fan[ID_WYRZUTNIA].setRev(data[21] * 100);

				//byte 22-29
				for (int i = 0; i < 4; i++)
					heatExchanger[0] = (float) (data[22 + i * 2] * 10 + (data[22 + i * 2 + 1] / 10.0));

				//byte 30
				normalMode.getDelayTime().setIsValue(data[30]);

				//byte 31
				humidityAlertMode.getTriggerInt().setIsValue(data[31]);
				//byte 32
				humidityAlertMode.getDelayTime().setIsValue(data[32]);

				//byte 33
				defrostMode.getTriggerInt().setIsValue(data[33]);
				//byte 34
				defrostMode.getDelayTime().setIsValue(data[34]);

				//byte 35-58
				for (int i = 0; i < 24; i++) {
					activeTempRegByHours[i].getSalon().getRequest().setIsValue(bitStatus(data[35 + i], 7));
					activeTempRegByHours[i].getPralnia().getRequest().setIsValue(bitStatus(data[35 + i], 6));
					activeTempRegByHours[i].getLazDol().getRequest().setIsValue(bitStatus(data[35 + i], 5));
					activeTempRegByHours[i].getRodzice().getRequest().setIsValue(bitStatus(data[35 + i], 4));
					activeTempRegByHours[i].getNatalia().getRequest().setIsValue(bitStatus(data[35 + i], 3));
					activeTempRegByHours[i].getKarolina().getRequest().setIsValue(bitStatus(data[35 + i], 2));
					activeTempRegByHours[i].getLazGora().getRequest().setIsValue(bitStatus(data[35 + i], 1));
				}

				//byte 59
				minTemp.setIsValue(data[59]);

				//byte 60-83
				for (int i = 0; i < 24; i++) {
					normalOnByHours[i].getSalon().getRequest().setIsValue(bitStatus(data[60 + i], 7));
					normalOnByHours[i].getPralnia().getRequest().setIsValue(bitStatus(data[60 + i], 6));
					normalOnByHours[i].getLazDol().getRequest().setIsValue(bitStatus(data[60 + i], 5));
					normalOnByHours[i].getRodzice().getRequest().setIsValue(bitStatus(data[60 + i], 4));
					normalOnByHours[i].getNatalia().getRequest().setIsValue(bitStatus(data[60 + i], 3));
					normalOnByHours[i].getKarolina().getRequest().setIsValue(bitStatus(data[60 + i], 2));
					normalOnByHours[i].getLazGora().getRequest().setIsValue(bitStatus(data[60 + i], 1));
				}

				//byte 84
				normalMode.setTimeLeft(data[84]);
				//byte 85
				humidityAlertMode.setTimeLeft(data[85]);
				//byte 86
				defrostMode.setTimeLeft(data[86]);
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

	public boolean compare(Module_Vent2 module_vent) {
		if (module_vent == null)
			return false;
		//return FALSE if compare data are different
		boolean result = true;
		if (result) result = cmp(module_vent.humidityAlert, humidityAlert);
		if (result) result = cmp(module_vent.bypassOpen, bypassOpen);
		if (result) result = cmp(module_vent.circuitPump, circuitPump);
		if (result) result = cmp(module_vent.reqPumpColdWater, reqPumpColdWater);
		if (result) result = cmp(module_vent.reqPumpHotWater, reqPumpHotWater);
		if (result) result = cmp(module_vent.defrostActive, defrostActive);
		if (result) result = cmp(module_vent.defrostActive, reqPumpHotWater);
		if (result) result = cmp(module_vent.reqAutoDiagnosis, reqAutoDiagnosis);

		if (result) result = cmp(module_vent.normalOn, normalOn);
		if (result) result = cmp(module_vent.activeCooling, activeCooling);
		if (result) result = cmp(module_vent.activeHeating, activeHeating);
		if (result) result = cmp(module_vent.reqLazDol, reqLazDol);
		if (result) result = cmp(module_vent.reqLazGora, reqLazGora);
		if (result) result = cmp(module_vent.reqKuchnia, reqKuchnia);

		for (int i = 0; i < 4; i++) {
			if (result) result = cmp(module_vent.bme280[i].getTemp(), bme280[i].getTemp(), 1);
			if (result) result = cmp(module_vent.bme280[i].getPressure(), bme280[i].getPressure(), 5);
			if (result) result = cmp(module_vent.bme280[i].getHumidity(), bme280[i].getHumidity(), 3);
		}

		for (int i = 0; i < 2; i++) {
			if (result) result = cmp(module_vent.fan[i].getSpeed(), fan[i].getSpeed(), 10);
			if (result) result = cmp(module_vent.fan[i].getRev(), module_vent.fan[i].getRev(), 100);
		}

		for (int i = 0; i < 4; i++)
			if (result) result = cmp(module_vent.heatExchanger[i], heatExchanger[i], 0.2);

		//byte 30
		if (result) result = cmp(module_vent.normalMode.getTrigger(), normalMode.getTrigger());
		if (result) result = cmp(module_vent.normalMode.getTriggerInt(), normalMode.getTriggerInt());
		if (result) result = cmp(module_vent.normalMode.getDelayTime(), normalMode.getDelayTime());
		if (result) result = cmp(module_vent.normalMode.getTimeLeft(), normalMode.getTimeLeft());

		if (result) result = cmp(module_vent.humidityAlertMode.getTrigger(), humidityAlertMode.getTrigger());
		if (result) result = cmp(module_vent.humidityAlertMode.getTriggerInt(), humidityAlertMode.getTriggerInt());
		if (result) result = cmp(module_vent.humidityAlertMode.getDelayTime(), humidityAlertMode.getDelayTime());
		if (result) result = cmp(module_vent.humidityAlertMode.getTimeLeft(), humidityAlertMode.getTimeLeft());

		if (result) result = cmp(module_vent.defrostMode.getTrigger(), defrostMode.getTrigger());
		if (result) result = cmp(module_vent.defrostMode.getTriggerInt(), defrostMode.getTriggerInt());
		if (result) result = cmp(module_vent.defrostMode.getDelayTime(), defrostMode.getDelayTime());
		if (result) result = cmp(module_vent.defrostMode.getTimeLeft(), defrostMode.getTimeLeft());

		for (int i = 0; i < 4; i++) {
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getSalon().getRequest(), activeTempRegByHours[i].getSalon().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getPralnia().getRequest(), activeTempRegByHours[i].getPralnia().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getLazDol().getRequest(), activeTempRegByHours[i].getLazDol().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getRodzice().getRequest(), activeTempRegByHours[i].getRodzice().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getNatalia().getRequest(), activeTempRegByHours[i].getNatalia().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getKarolina().getRequest(), activeTempRegByHours[i].getKarolina().getRequest());
			if (result)
				result = cmp(module_vent.activeTempRegByHours[i].getLazGora().getRequest(), activeTempRegByHours[i].getLazGora().getRequest());
		}

		if (result) result = cmp(module_vent.minTemp, minTemp);

		for (int i = 0; i < 4; i++) {
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getSalon().getRequest(), normalOnByHours[i].getSalon().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getPralnia().getRequest(), normalOnByHours[i].getPralnia().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getLazDol().getRequest(), normalOnByHours[i].getLazDol().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getRodzice().getRequest(), normalOnByHours[i].getRodzice().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getNatalia().getRequest(), normalOnByHours[i].getNatalia().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getKarolina().getRequest(), normalOnByHours[i].getKarolina().getRequest());
			if (result)
				result = cmp(module_vent.normalOnByHours[i].getLazGora().getRequest(), normalOnByHours[i].getLazGora().getRequest());
		}

		return result;
	}

	@Override
	public Module_Vent2 clone() throws CloneNotSupportedException {
		Module_Vent2 module_vent2 = (Module_Vent2) super.clone();
//		module_vent.hour = hour.clone();
//		module_vent.NVHour = NVHour.clone();
//		module_vent.bme280 = bme280.clone();
//		module_vent.fan = fan.clone();
		return module_vent2;
	}
}