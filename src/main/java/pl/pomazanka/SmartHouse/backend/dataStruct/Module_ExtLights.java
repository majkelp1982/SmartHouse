package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.LightDimmer;

import java.time.LocalTime;

@Controller
public class Module_ExtLights extends Module implements Cloneable {
  // Module ventilation type
  private static final transient byte MODULE_TYPE = 16;
  private final transient byte ID_ENTRANCE = 0;
  private final transient byte ID_DRIVEWAY = 1;
  private final transient byte ID_CARPORT = 2;
  private final transient byte ID_FENCE = 3;
  private final transient LightDimmer[] NVLightDimmer = new LightDimmer[4];
  private LightDimmer[] lightDimmer = new LightDimmer[4];
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

  public void setNVstartLightLevel(final int NVstartLightLevel) {
    this.NVstartLightLevel = NVstartLightLevel;
  }

  public LocalTime getNVoffTime() {
    return NVoffTime;
  }

  public void setNVoffTime(final LocalTime NVoffTime) {
    this.NVoffTime = NVoffTime;
  }

  public boolean isAllUpToDate() {
    setUpToDate(true);
    for (int i = 0; i < 4; i++) {
      if (isUpToDate()) {
        setUpToDate(lightDimmer[i].isForceMax() == NVLightDimmer[i].isForceMax());
      }
      if (isUpToDate()) {
        setUpToDate(lightDimmer[i].isForce0() == NVLightDimmer[i].isForce0());
      }
      if (isUpToDate()) {
        setUpToDate(lightDimmer[i].getStandByIntens() == NVLightDimmer[i].getStandByIntens());
      }
      if (isUpToDate()) {
        setUpToDate(lightDimmer[i].getMaxIntens() == NVLightDimmer[i].getMaxIntens());
      }
    }
    if (isUpToDate()) {
      setUpToDate(startLightLevel == NVstartLightLevel);
    }
    if (isUpToDate()) {
      setUpToDate(offTime.getHour() == NVoffTime.getHour());
    }
    if (isUpToDate()) {
      setUpToDate(offTime.getMinute() == NVoffTime.getMinute());
    }

    setReqUpdateValues(!isUpToDate());
    return isUpToDate();
  }

  // Parser for data package coming via UDP
  @Override
  public void dataParser(final int[] packetData) {
    final int controllerFrameNumber = packetData[2];

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

        if ((packetData[13] > 23) || (packetData[14] > 59)) {
          offTime = LocalTime.of(0, 0);
        } else {
          offTime = LocalTime.of(packetData[13], packetData[14]);
        }

        lightDimmer[ID_ENTRANCE].setMaxIntens(packetData[15]);
        lightDimmer[ID_DRIVEWAY].setMaxIntens(packetData[16]);
        lightDimmer[ID_CARPORT].setMaxIntens(packetData[17]);
        lightDimmer[ID_FENCE].setMaxIntens(packetData[18]);

        break;

      case 200: // diagnostic frame
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
    // Clear previous faults status
    resetFaultPresent();

    // TODO fault list to extend
    updateGlobalFaultList();
  }

  @Override
  protected void assignNV(final Object object) throws Exception {
    for (int i = 0; i < 4; i++) {
      try {
        NVLightDimmer[i] = (LightDimmer) lightDimmer[i].clone();
      } catch (final CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }
    NVstartLightLevel = startLightLevel;
    NVoffTime = offTime;
  }

  public boolean compare(final Module_ExtLights module_extLights) {
    if (module_extLights == null) {
      return false;
    }
    // return FALSE if compare data are different
    boolean result = true;
    for (int i = 0; i < 4; i++) {
      if (result) {
        result = cmp(module_extLights.lightDimmer[i].getIntens(), lightDimmer[i].getIntens(), 1);
      }
      if (result) {
        result =
            cmp(
                module_extLights.lightDimmer[i].getStandByIntens(),
                lightDimmer[i].getStandByIntens(),
                1);
      }
      if (result) {
        result =
            cmp(module_extLights.lightDimmer[i].getMaxIntens(), lightDimmer[i].getMaxIntens(), 1);
      }
      if (result) {
        result = cmp(module_extLights.lightDimmer[i].isForce0(), lightDimmer[i].isForce0());
      }
      if (result) {
        result = cmp(module_extLights.lightDimmer[i].isForceMax(), lightDimmer[i].isForceMax());
      }
    }
    if (result) {
      result =
          cmp(
              (module_extLights.getOffTime().getHour() * 100
                  + module_extLights.getOffTime().getMinute()),
              (getOffTime().getHour() * 100 + getOffTime().getMinute()));
    }
    if (isTooLongWithoutSave()) {
      result = false;
    }
    return result;
  }

  @Override
  public Module_ExtLights clone() throws CloneNotSupportedException {
    final Module_ExtLights module_extLights = (Module_ExtLights) super.clone();
    module_extLights.lightDimmer = lightDimmer.clone();
    for (int i = 0; i < 4; i++) {
      try {
        module_extLights.lightDimmer[i] = (LightDimmer) lightDimmer[i].clone();
      } catch (final CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }
    return module_extLights;
  }
}
