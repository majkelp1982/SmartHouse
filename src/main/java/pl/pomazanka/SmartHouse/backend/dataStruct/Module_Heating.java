package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
public class Module_Heating extends Module implements Cloneable {
  // Module heating type
  private static final byte MODULE_TYPE = 14;
  private LocalDateTime reqPCStartTime = null;

  // Values only to read
  private int heatSourceActive;
  private boolean pump_InHouse;
  private boolean pump_UnderGround;
  private boolean reqHeatingPumpOn;
  private boolean cheapTariffOnly;
  private boolean heatingActivated;
  private boolean waterSuperheat;
  private int valve_3way;
  private int valve_bypass;
  private boolean[] zone = new boolean[7];
  private float reqTempBufferCO;
  private float reqTempBufferCWU;
  private float tBufferCOLow;
  private float tBufferCOMid;
  private float tBufferCOHigh;
  private float tBufferCWULow;
  private float tBufferCWUMid;
  private float tBufferCWUHigh;
  private float tSupply;
  private float tReturn;
  private float tGroundSource;
  private float tFirePlace;
  private float tManifold;
  private float tReturnGroundFloor;
  private float tReturnLoft;
  private int heatPumpAlarmTemp;

  // New values of variables which can be updated
  private transient boolean NVCheapTariffOnly;
  private transient boolean NVHeatingActivated;
  private transient boolean NVWaterSuperheat;
  private transient double NVReqTempBufferCO;
  private transient double NVReqTempBufferCWU;
  private transient int NVHeatPumpAlarmTemp;

  public Module_Heating() throws Exception {
    super(MODULE_TYPE, "Ogrzewanie", "module_heating");
  }

  public int getHeatSourceActive() {
    return heatSourceActive;
  }

  public boolean isPump_InHouse() {
    return pump_InHouse;
  }

  public boolean isPump_UnderGround() {
    return pump_UnderGround;
  }

  public boolean isReqHeatingPumpOn() {
    return reqHeatingPumpOn;
  }

  public boolean isCheapTariffOnly() {
    return cheapTariffOnly;
  }

  public boolean isHeatingActivated() {
    return heatingActivated;
  }

  public boolean isWaterSuperheat() {
    return waterSuperheat;
  }

  public int getValve_3way() {
    return valve_3way;
  }

  public int getValve_bypass() {
    return valve_bypass;
  }

  public boolean[] getZone() {
    return zone;
  }

  public float getReqTempBufferCO() {
    return reqTempBufferCO;
  }

  public float getReqTempBufferCWU() {
    return reqTempBufferCWU;
  }

  public float getHeatPumpAlarmTemp() {
    return heatPumpAlarmTemp;
  }

  public float gettBufferCOLow() {
    return tBufferCOLow;
  }

  public float gettBufferCOMid() {
    return tBufferCOMid;
  }

  public float gettBufferCOHigh() {
    return tBufferCOHigh;
  }

  public float gettBufferCWULow() {
    return tBufferCWULow;
  }

  public float gettBufferCWUMid() {
    return tBufferCWUMid;
  }

  public float gettBufferCWUHigh() {
    return tBufferCWUHigh;
  }

  public float gettSupply() {
    return tSupply;
  }

  public float gettReturn() {
    return tReturn;
  }

  public float gettGroundSource() {
    return tGroundSource;
  }

  public float gettFirePlace() {
    return tFirePlace;
  }

  public float gettManifold() {
    return tManifold;
  }

  public float gettReturnGroundFloor() {
    return tReturnGroundFloor;
  }

  public float gettReturnLoft() {
    return tReturnLoft;
  }

  public boolean isNVCheapTariffOnly() {
    return NVCheapTariffOnly;
  }

  public void setNVCheapTariffOnly(final boolean NVCheapTariffOnly) {
    this.NVCheapTariffOnly = NVCheapTariffOnly;
    setUpToDate(false);
  }

  public boolean isNVHeatingActivated() {
    return NVHeatingActivated;
  }

  public void setNVHeatingActivated(final boolean NVHeatingActivated) {
    this.NVHeatingActivated = NVHeatingActivated;
    setUpToDate(false);
  }

  public boolean isNVWaterSuperheat() {
    return NVWaterSuperheat;
  }

  public void setNVWaterSuperheat(final boolean NVWaterSuperheat) {
    this.NVWaterSuperheat = NVWaterSuperheat;
    setUpToDate(false);
  }

  public double getNVReqTempBufferCO() {
    return NVReqTempBufferCO;
  }

  public void setNVReqTempBufferCO(final double NVReqTempBufferCO) {
    this.NVReqTempBufferCO = NVReqTempBufferCO;
    setUpToDate(false);
  }

  public double getNVReqTempBufferCWU() {
    return NVReqTempBufferCWU;
  }

  public void setNVReqTempBufferCWU(final double NVReqTempBufferCWU) {
    this.NVReqTempBufferCWU = NVReqTempBufferCWU;
    setUpToDate(false);
  }

  public double getNVHeatPumpAlarmTemp() {
    return NVHeatPumpAlarmTemp;
  }

  public void setNVHeatPumpAlarmTemp(final int NVHeatPumpAlarmTemp) {
    this.NVHeatPumpAlarmTemp = NVHeatPumpAlarmTemp;
    setUpToDate(false);
  }

  public boolean isAllUpToDate() {
    setUpToDate(true);
    if (isUpToDate()) {
      setUpToDate(NVCheapTariffOnly == cheapTariffOnly);
    }
    if (isUpToDate()) {
      setUpToDate(NVHeatingActivated == heatingActivated);
    }
    if (isUpToDate()) {
      setUpToDate(NVWaterSuperheat == waterSuperheat);
    }
    if (isUpToDate()) {
      setUpToDate(NVReqTempBufferCO == reqTempBufferCO);
    }
    if (isUpToDate()) {
      setUpToDate(NVReqTempBufferCWU == reqTempBufferCWU);
    }
    if (isUpToDate()) {
      setUpToDate(NVHeatPumpAlarmTemp == heatPumpAlarmTemp);
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
        heatSourceActive = packetData[3] >> 6;
        pump_InHouse = bitStatus(packetData[3], 5);
        pump_UnderGround = bitStatus(packetData[3], 4);
        reqHeatingPumpOn = bitStatus(packetData[3], 3);
        cheapTariffOnly = bitStatus(packetData[3], 2);
        heatingActivated = bitStatus(packetData[3], 1);
        waterSuperheat = bitStatus(packetData[3], 0);

        valve_3way = packetData[4] >> 6;
        final int tmp = packetData[4] >> 6;
        valve_bypass = (packetData[4] - (tmp << 6));

        zone[0] = bitStatus(packetData[5], 7);
        zone[1] = bitStatus(packetData[5], 6);
        zone[2] = bitStatus(packetData[5], 5);
        zone[3] = bitStatus(packetData[5], 4);
        zone[4] = bitStatus(packetData[5], 3);
        zone[5] = bitStatus(packetData[5], 2);
        zone[6] = bitStatus(packetData[5], 1);

        reqTempBufferCO = (float) packetData[6] / 2;
        reqTempBufferCWU = (float) packetData[7] / 2;

        tBufferCOLow = (float) packetData[8] / 2;
        tBufferCOMid = (float) packetData[9] / 2;
        tBufferCOHigh = (float) packetData[10] / 2;
        tBufferCWULow = (float) packetData[11] / 2;
        tBufferCWUMid = (float) packetData[12] / 2;
        tBufferCWUHigh = (float) packetData[13] / 2;

        tSupply = (float) packetData[14] / 2;
        tReturn = (float) packetData[15] / 2;
        tGroundSource = (float) packetData[16] / 2;
        tFirePlace = (float) packetData[17] / 2;
        tManifold = (float) packetData[18] / 2;
        tReturnGroundFloor = (float) packetData[19] / 2;
        tReturnLoft = (float) packetData[20] / 2;
        heatPumpAlarmTemp = packetData[21];
        break;

      case 200: // diagnostic frame
        updateDiag(packetData);
        break;
    }
    super.dataParser(packetData);
  }

  @Override
  protected void assignNV(final Object object) throws Exception {
    NVCheapTariffOnly = cheapTariffOnly;
    NVHeatingActivated = heatingActivated;
    NVWaterSuperheat = waterSuperheat;
    NVReqTempBufferCO = reqTempBufferCO;
    NVReqTempBufferCWU = reqTempBufferCWU;
    NVHeatPumpAlarmTemp = heatPumpAlarmTemp;
  }

  @Override
  void faultListInit() throws Exception {
    setFaultText(0, "Pompa ciepła przestała grzać");
    setFaultText(1, "Pompa ciepła osiągnęła nastawioną graniczną temperaturę");
    setFaultText(2, "T[tBufferCOLow] błąd odczytu");
    setFaultText(3, "T[tBufferCOMid] błąd odczytu");
    setFaultText(4, "T[tBufferCOHigh] błąd odczytu");
    setFaultText(5, "T[tBufferCWULow] błąd odczytu");
    setFaultText(6, "T[tBufferCWUMid] błąd odczytu");
    setFaultText(7, "T[tBufferCWUHigh] błąd odczytu");
    setFaultText(8, "T[tSupply] błąd odczytu");
    setFaultText(9, "T[tReturn] błąd odczytu");
    setFaultText(10, "T[tGroundSource] błąd odczytu");
    setFaultText(11, "T[tFirePlace] błąd odczytu");
    setFaultText(12, "T[tManifold] błąd odczytu");
    setFaultText(13, "T[tReturnGroundFloor] błąd odczytu");
    setFaultText(14, "T[tReturnLoft] błąd odczytu");
  }

  @Override
  void faultCheck() {
    // Clear previous faults status
    resetFaultPresent();

    // Fault check list
    // check if after 60s heating request PC is working continuously till no request
    if (reqPCStartTime != null) {
      if ((ChronoUnit.SECONDS.between(reqPCStartTime, LocalDateTime.now()) > 90)
          && (tSupply < (tReturn + 2))) {
        setFaultPresent(0, true);
      }
    }
    if (tSupply >= heatPumpAlarmTemp) {
      setFaultPresent(1, true);
    }
    if (tBufferCOLow == 100) {
      setFaultPresent(2, true);
    }
    if (tBufferCOMid == 100) {
      setFaultPresent(3, true);
    }
    if (tBufferCOHigh == 100) {
      setFaultPresent(4, true);
    }
    if (tBufferCWULow == 100) {
      setFaultPresent(5, true);
    }
    if (tBufferCWUMid == 100) {
      setFaultPresent(6, true);
    }
    if (tBufferCWUHigh == 100) {
      setFaultPresent(7, true);
    }
    if (tSupply == 100) {
      setFaultPresent(8, true);
    }
    if (tReturn == 100) {
      setFaultPresent(9, true);
    }
    if (tGroundSource == 100) {
      setFaultPresent(10, true);
    }
    if (tFirePlace == 100) {
      setFaultPresent(11, true);
    }
    if (tManifold == 100) {
      setFaultPresent(12, true);
    }
    if (tReturnGroundFloor == 100) {
      setFaultPresent(13, true);
    }
    if (tReturnLoft == 100) {
      setFaultPresent(14, true);
    }

    updateGlobalFaultList();
  }

  // compare data : last save status with new set
  public boolean compare(final Module_Heating module_Heating) {
    if (module_Heating == null) {
      return false;
    }
    boolean result = true;
    if (result) {
      result = cmp(module_Heating.heatSourceActive, heatSourceActive);
    }
    if (result) {
      result = cmp(module_Heating.cheapTariffOnly, cheapTariffOnly);
    }
    if (result) {
      result = cmp(module_Heating.pump_UnderGround, pump_UnderGround);
    }
    if (result) {
      result = cmp(module_Heating.reqHeatingPumpOn, reqHeatingPumpOn);
      // Save time when heating pump requested
      if ((!module_Heating.reqHeatingPumpOn) && (reqHeatingPumpOn)) {
        reqPCStartTime = LocalDateTime.now();
      }
      if (!reqHeatingPumpOn) {
        reqPCStartTime = null;
      }
    }
    if (result) {
      result = cmp(module_Heating.cheapTariffOnly, cheapTariffOnly);
    }
    if (result) {
      result = cmp(module_Heating.heatingActivated, heatingActivated);
    }
    if (result) {
      result = cmp(module_Heating.waterSuperheat, waterSuperheat);
    }
    if (result) {
      result = cmp(module_Heating.valve_3way, valve_3way);
    }
    if (result) {
      result = cmp(module_Heating.valve_bypass, valve_bypass);
    }
    if (result) {
      result = cmp(module_Heating.zone[0], zone[0]);
    }
    if (result) {
      result = cmp(module_Heating.zone[1], zone[1]);
    }
    if (result) {
      result = cmp(module_Heating.zone[2], zone[2]);
    }
    if (result) {
      result = cmp(module_Heating.zone[3], zone[3]);
    }
    if (result) {
      result = cmp(module_Heating.zone[4], zone[4]);
    }
    if (result) {
      result = cmp(module_Heating.zone[5], zone[5]);
    }
    if (result) {
      result = cmp(module_Heating.zone[6], zone[6]);
    }
    if (result) {
      result = cmp(module_Heating.reqTempBufferCO, reqTempBufferCO, 0);
    }
    if (result) {
      result = cmp(module_Heating.reqTempBufferCWU, reqTempBufferCWU, 0);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCOLow, tBufferCOLow, 2);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCOMid, tBufferCOMid, 2);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCOHigh, tBufferCOHigh, 2);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCWULow, tBufferCWULow, 2);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCWUMid, tBufferCWUMid, 2);
    }
    if (result) {
      result = cmp(module_Heating.tBufferCWUHigh, tBufferCWUHigh, 2);
    }
    if (result) {
      result = cmp(module_Heating.tSupply, tSupply, 1);
    }
    if (result) {
      result = cmp(module_Heating.tReturn, tReturn, 1);
    }
    if (result) {
      result = cmp(module_Heating.tGroundSource, tGroundSource, 1);
    }
    if (result) {
      result = cmp(module_Heating.tFirePlace, tFirePlace, 1);
    }
    if (result) {
      result = cmp(module_Heating.tManifold, tManifold, 1);
    }
    if (result) {
      result = cmp(module_Heating.tReturnGroundFloor, tReturnGroundFloor, 1);
    }
    if (result) {
      result = cmp(module_Heating.tReturnLoft, tReturnLoft, 1);
    }
    if (result) {
      result = cmp(module_Heating.heatPumpAlarmTemp, heatPumpAlarmTemp, 0);
    }
    if (isTooLongWithoutSave()) {
      result = false;
    }
    return result;
  }

  @Override
  public Module_Heating clone() throws CloneNotSupportedException {
    final Module_Heating module_heating = (Module_Heating) super.clone();
    module_heating.zone = zone.clone();
    return module_heating;
  }
}
