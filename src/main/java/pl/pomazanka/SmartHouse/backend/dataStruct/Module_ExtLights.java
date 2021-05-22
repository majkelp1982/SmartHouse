package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.LightDimmer;

import java.time.LocalTime;

@Controller
public class Module_ExtLights extends Module implements Cloneable {
	//Module ventilation type
	private transient static byte MODULE_TYPE = 16;
	private transient final byte ID_ENTRANCE = 0;
	private transient final byte ID_DRIVEWAY = 1;
	private transient final byte ID_CARPORT = 2;
	private transient final byte ID_FENCE = 3;

	private LightDimmer[] lightDimmer = new LightDimmer[4];
	private transient LightDimmer[] NVLightDimmer = new LightDimmer[4];
	private int startLightLevel;
	private transient int NVstartLightLevel;
	private LocalTime offTime = LocalTime.now();
	private transient LocalTime NVoffTime;

	public Module_ExtLights() throws Exception {
		super(MODULE_TYPE, "Oświetlenie", "module_extLights");
		for (int i = 0; i < 4; i++) {
			lightDimmer[i] = new LightDimmer();
			NVLightDimmer[i] = new LightDimmer();
		}
		faultListInit();
	}

	public int getStartLightLevel() {
		return startLightLevel;
	}

	public LocalTime getOffTime() {
		return offTime;
	}

	public LightDimmer[] getLightDimmer() {
		return lightDimmer;
	}

	public LightDimmer[] getNVLightDimmer() {
		return NVLightDimmer;
	}

	public int getNVstartLightLevel() {
		return NVstartLightLevel;
	}

	public LocalTime getNVoffTime() {
		return NVoffTime;
	}

	public void setNVstartLightLevel(int NVstartLightLevel) {
		this.NVstartLightLevel = NVstartLightLevel;
	}

	public void setNVoffTime(LocalTime NVoffTime) {
		this.NVoffTime = NVoffTime;
	}

	public boolean isAllUpToDate() {
		setUpToDate(true);
		for (int i = 0; i < 4; i++) {
			if (isUpToDate()) setUpToDate(lightDimmer[i].isForceMax() == NVLightDimmer[i].isForceMax());
			if (isUpToDate()) setUpToDate(lightDimmer[i].isForce0() == NVLightDimmer[i].isForce0());
			if (isUpToDate()) setUpToDate(lightDimmer[i].getStandByIntens() == NVLightDimmer[i].getStandByIntens());
			if (isUpToDate()) setUpToDate(lightDimmer[i].getMaxIntens() == NVLightDimmer[i].getMaxIntens());
		}
		if (isUpToDate()) setUpToDate(startLightLevel == NVstartLightLevel);
		if (isUpToDate()) setUpToDate(offTime.getHour() == NVoffTime.getHour());
		if (isUpToDate()) setUpToDate(offTime.getMinute() == NVoffTime.getMinute());

		setReqUpdateValues(!isUpToDate());
		return isUpToDate();
	}

	//Parser for data package coming via UDP
	public void dataParser(int[] packetData) {
		int controllerFrameNumber = packetData[2];

		switch (controllerFrameNumber) {
			case 0: // standard frame 0
				lightDimmer[ID_ENTRANCE].setForceMax(bitStatus(packetData[3], 7));
				lightDimmer[ID_DRIVEWAY].setForceMax(bitStatus(packetData[3], 6));
				lightDimmer[ID_CARPORT].setForceMax(bitStatus(packetData[3], 5));
				lightDimmer[ID_FENCE].setForceMax(bitStatus(packetData[3], 4));

				lightDimmer[ID_ENTRANCE].setForce0(bitStatus(packetData[3], 3));
				lightDimmer[ID_DRIVEWAY].setForce0(bitStatus(packetData[3], 2));
				lightDimmer[ID_CARPORT].setForce0(bitStatus(packetData[3], 1));
				lightDimmer[ID_FENCE].setForce0(bitStatus(packetData[3], 0));

				lightDimmer[ID_ENTRANCE].setIntens(packetData[4]);
				lightDimmer[ID_DRIVEWAY].setIntens(packetData[5]);
				lightDimmer[ID_CARPORT].setIntens(packetData[6]);
				lightDimmer[ID_FENCE].setIntens(packetData[7]);

				startLightLevel = packetData[8];

				lightDimmer[ID_ENTRANCE].setStandByIntens(packetData[9]);
				lightDimmer[ID_DRIVEWAY].setStandByIntens(packetData[10]);
				lightDimmer[ID_CARPORT].setStandByIntens(packetData[11]);
				lightDimmer[ID_FENCE].setStandByIntens(packetData[12]);

				if ((packetData[13]>23) || (packetData[14]>59))
					offTime = LocalTime.of(0,0);
					else offTime = LocalTime.of(packetData[13], packetData[14]);

				lightDimmer[ID_ENTRANCE].setMaxIntens(packetData[15]);
				lightDimmer[ID_DRIVEWAY].setMaxIntens(packetData[16]);
				lightDimmer[ID_CARPORT].setMaxIntens(packetData[17]);
				lightDimmer[ID_FENCE].setMaxIntens(packetData[18]);

				break;

			case 200: //diagnostic frame
				updateDiag(packetData);
				break;
		}
		super.dataParser(packetData);
	}

	@Override
	void faultListInit() throws Exception {
//		setFaultText(0, "Test");
	}

	@Override
	void faultCheck() {
		//Clear previous faults status
		resetFaultPresent();

		//TODO fault list to extend
		updateGlobalFaultList();
	}

	@Override
	void assignNV() {
		for (int i=0; i<4; i++) {
			try {
				NVLightDimmer[i] = (LightDimmer) lightDimmer[i].clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		NVstartLightLevel = startLightLevel;
		NVoffTime = offTime;
	}

	public boolean compare(Module_ExtLights module_extLights) {
		if (module_extLights == null)
			return false;
		//return FALSE if compare data are different
		boolean result = true;
		for (int i = 0; i < 4; i++) {
			if (result) result = cmp(module_extLights.lightDimmer[i].getIntens(), lightDimmer[i].getIntens(), 1);
			if (result) result = cmp(module_extLights.lightDimmer[i].getStandByIntens(), lightDimmer[i].getStandByIntens(), 1);
			if (result) result = cmp(module_extLights.lightDimmer[i].getMaxIntens(), lightDimmer[i].getMaxIntens(), 1);
			if (result) result = cmp(module_extLights.lightDimmer[i].isForce0(), lightDimmer[i].isForce0());
			if (result) result = cmp(module_extLights.lightDimmer[i].isForceMax(), lightDimmer[i].isForceMax());
		}
		if (result) result = cmp((module_extLights.getOffTime().getHour() * 100 + module_extLights.getOffTime().getMinute()), (getOffTime().getHour() * 100 + getOffTime().getMinute()));
		if (isTooLongWithoutSave())
			result = false;
		return result;
	}

	@Override
	public Module_ExtLights clone() throws CloneNotSupportedException {
		Module_ExtLights module_extLights = (Module_ExtLights) super.clone();
		module_extLights.lightDimmer = lightDimmer.clone();
		for (int i=0; i<4; i++) {
			try {
				module_extLights.lightDimmer[i] = (LightDimmer) lightDimmer[i].clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return module_extLights;
	}
}