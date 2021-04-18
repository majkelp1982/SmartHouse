package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
public class Module_Sewage extends Module implements Cloneable {
    //Module heating type
    private static byte MODULE_TYPE = 12;

    // Values only to read
    private boolean airPump;
    private boolean waterPump;
    private boolean limitSensor;
    private int isWaterLevel;
    private int maxWaterLevel;
    private int minWaterLevel;
    private int zeroRefWaterLevel;
    private int intervalAirPump;

    //New values of variables which can be updated
    private transient int NVmaxWaterLevel;
    private transient int NVminWaterLevel;
    private transient int NVZeroRefWaterLevel;
    private transient int NVIntervalAirPump;


    public Module_Sewage() throws Exception {
        super(MODULE_TYPE, "Oczyszczalnia", "module_sewage");
        faultListInit();
    }

    public boolean isAirPump() {
        return airPump;
    }

    public boolean isWaterPump() {
        return waterPump;
    }

    public boolean isLimitSensor() {
        return limitSensor;
    }

    public int getIsWaterLevel() {
        return isWaterLevel;
    }

    public int getMaxWaterLevel() {
        return maxWaterLevel;
    }

    public int getMinWaterLevel() {
        return minWaterLevel;
    }

    public int getZeroRefWaterLevel() {
        return zeroRefWaterLevel;
    }

    public int getIntervalAirPump() {
        return intervalAirPump;
    }

    public void setNVmaxWaterLevel(int NVmaxWaterLevel) {
        this.NVmaxWaterLevel = NVmaxWaterLevel;
        setUpToDate(false);
    }

    public void setNVminWaterLevel(int NVminWaterLevel) {
        this.NVminWaterLevel = NVminWaterLevel;
        setUpToDate(false);
    }

    public void setNVZeroRefWaterLevel(int NVzeroRefWaterLevel) {
        this.NVZeroRefWaterLevel = NVzeroRefWaterLevel;
        setUpToDate(false);
   }

    public void setNVIntervalAirPump(int NVitervalAirPump) {
        this.NVIntervalAirPump = NVitervalAirPump;
        setUpToDate(false);
    }

    public int getNVmaxWaterLevel() {
        return NVmaxWaterLevel;
    }

    public int getNVminWaterLevel() {
        return NVminWaterLevel;
    }

    public int getNVZeroRefWaterLevel() {
        return NVZeroRefWaterLevel;
    }

    public int getNVIntervalAirPump() {
        return NVIntervalAirPump;
    }

    public boolean isAllUpToDate() {
        setUpToDate(true);
        if (isUpToDate()) setUpToDate(NVmaxWaterLevel       == maxWaterLevel);
        if (isUpToDate()) setUpToDate(NVminWaterLevel       == minWaterLevel);
        if (isUpToDate()) setUpToDate(NVZeroRefWaterLevel   == zeroRefWaterLevel);
        if (isUpToDate()) setUpToDate(NVIntervalAirPump      == intervalAirPump);

        setReqUpdateValues(!isUpToDate());

        return isUpToDate();
    }

    //Parser for data package coming via UDP
    public void dataParser(int[] packetData) {
        int controllerFrameNumber = packetData[2];

        switch (controllerFrameNumber) {
            case 0: // standard frame 0
                airPump = bitStatus(packetData[3], 7);
                waterPump = bitStatus(packetData[3], 6);
                limitSensor = bitStatus(packetData[3], 5);
                isWaterLevel = -1*packetData[4];
                maxWaterLevel = -1*packetData[5];
                minWaterLevel = -1*packetData[6];
                zeroRefWaterLevel = -1*packetData[7];
                intervalAirPump = packetData[8];
                break;

            case 200: //diagnostic frame
                setIP(new int[]{packetData[3],packetData[4],packetData[5],packetData[6]});
                break;
        }
        super.dataParser(packetData);
    }
    
    @Override
    void assignNV() {
        NVmaxWaterLevel = maxWaterLevel;
        NVminWaterLevel = minWaterLevel;
        NVZeroRefWaterLevel = zeroRefWaterLevel;
        NVIntervalAirPump = intervalAirPump;
    }

    @Override
    void faultListInit () throws Exception {
        setFaultText(0,"Pompa wody przestała działać");
        setFaultText(1,"Poziom wody przekroczony!!!");
        setFaultText(2,"Sensor limitu poziomu wody aktywny!!!");
    }

    @Override
    void faultCheck() {
        //Clear previous faults status
        resetFaultPresent();

        //Fault check list
        //TODO
        setFaultPresent(2,limitSensor);

        updateGlobalFaultList();
    }

    //compare data : last save status with new set
    public boolean compare(Module_Sewage module_sewage) {
        boolean result = true;
        if (result) result = cmp(module_sewage.airPump, airPump);
        if (result) result = cmp(module_sewage.waterPump, waterPump);
        if (result) result = cmp(module_sewage.limitSensor, limitSensor);

        if (result) result = cmp(module_sewage.isWaterLevel, isWaterLevel, 3);
        if (result) result = cmp(module_sewage.maxWaterLevel, maxWaterLevel, 0);
        if (result) result = cmp(module_sewage.minWaterLevel, minWaterLevel, 0);
        if (result) result = cmp(module_sewage.zeroRefWaterLevel, zeroRefWaterLevel, 0);
        if (result) result = cmp(module_sewage.intervalAirPump, intervalAirPump, 0);
        if (isTooLongWithoutSave())
            result = false;
        return result;
    }

    @Override
    public Module_Sewage clone() throws CloneNotSupportedException {
        Module_Sewage module_sewage = (Module_Sewage) super.clone();
        return module_sewage;
    }
}
