package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;

import java.time.temporal.ChronoUnit;

@Controller
public class Module_Comfort extends Module implements Cloneable {
    //Module comfort type
    private static byte MODULE_TYPE = 10;

    private Zone[] zone = new Zone[7];

    public Module_Comfort() throws Exception {
        super(MODULE_TYPE, "Komfort", "module_comfort");
        for (int i=0; i<7;i++)
            zone[i] = new Zone();

        faultListInit();
    }

    public Zone[] getZone() {
        return zone;
    }

    public void setNVReqZ0(double NVReqTempZ0) {
        zone[0].NVReqTemp = NVReqTempZ0;
        setUpToDate(false);
    }

    public void setNVReqZ1(double NVReqTempZ1) {
        zone[1].NVReqTemp = NVReqTempZ1;
        setUpToDate(false);
    }

    public void setNVReqZ2(double NVReqTempZ2) {
        zone[2].NVReqTemp = NVReqTempZ2;
        setUpToDate(false);
    }

    public void setNVReqZ3(double NVReqTempZ3) {
        zone[3].NVReqTemp = NVReqTempZ3;
        setUpToDate(false);
    }

    public void setNVReqZ4(double NVReqTempZ4) {
        zone[4].NVReqTemp = NVReqTempZ4;
        setUpToDate(false);
    }

    public void setNVReqZ5(double NVReqTempZ5) {
        zone[5].NVReqTemp = NVReqTempZ5;
        setUpToDate(false);
    }

    public void setNVReqZ6(double NVReqTempZ6) {
        zone[6].NVReqTemp = NVReqTempZ6;
        setUpToDate(false);
    }

    public boolean isAllUpToDate() {
        setUpToDate(true);
        for (int i=0; i<=6; i++)
            if (isUpToDate()) setUpToDate(zone[i].NVReqTemp == zone[i].reqTemp);

        setReqUpdateValues(!isUpToDate());
        return isUpToDate();
    }

    //Parser for data package coming via UDP
    public void dataParser(int[] packetData) {
        int controllerFrameNumber = packetData[2];

        switch (controllerFrameNumber) {
            case 0: // standard frame 0
                for (int i=0; i<7; i++) {
                    zone[i].isTemp = (float) (packetData[i * 4 + 3] * 10 + packetData[i * 4 + 4]) / 10;
                    zone[i].reqTemp = (packetData[i * 4 + 5] / 2.00);
                    zone[i].isHumidity = (int) (packetData[i * 4 + 6]);
                }
                break;
            case 200: // standard frame 0\
                setIP(new int[]{packetData[3],packetData[4],packetData[5],packetData[6]});
                break;
        }
        super.dataParser(packetData);
    }

    @Override
    void assignNV() {
        for (int i=0; i<7; i++)
            zone[i].NVReqTemp = zone[i].reqTemp;
    }

    @Override
    void faultListInit () throws Exception {
        setFaultText(0,"Termometr[salon] błąd odczytu temperatury");
        setFaultText(1,"Termometr[pralnia] błąd odczytu temperatury");
        setFaultText(2,"Termometr[laź.dół] błąd odczytu temperatury");
        setFaultText(3,"Termometr[rodzic] błąd odczytu temperatury");
        setFaultText(4,"Termometr[Natalia] błąd odczytu temperatury");
        setFaultText(5,"Termometr[Karolina] błąd odczytu temperatury");
        setFaultText(6,"Termometr[łaź.góra] błąd odczytu temperatury");
    }

    @Override
    void faultCheck() {
        //Clear previous faults status
        resetFaultPresent();

        //Fault check list
        for (int i=0; i<7; i++)
            if (zone[i].isTemp <11) setFaultPresent(i, true);
        //TODO fault list to extend
        updateGlobalFaultList();
    }


    public boolean compare(Module_Comfort module_comfort) {
        //return FALSE if compare data are different
        boolean result = true;
        for (int i=0; i<7; i++) {
            if (result) result = cmp(module_comfort.zone[i].isTemp,zone[i].isTemp,0.2);
            if (result) result = cmp(module_comfort.zone[i].reqTemp,zone[i].reqTemp,0);
            if (result) result = cmp(module_comfort.zone[i].isHumidity,zone[i].isHumidity,2);
        }
        if (isTooLongWithoutSave())
            result = false;
        return result;
    }

    public class Zone implements Cloneable {
        public float isTemp=0;
        public double reqTemp=0;
        public transient double NVReqTemp = 0;
        public int isHumidity=0;

        @Override
        protected Zone clone() throws CloneNotSupportedException {
            Zone zone = (Zone) super.clone();
            return zone;
        }
    }

    @Override
    public Module_Comfort clone() throws CloneNotSupportedException {
        Module_Comfort module_comfort = (Module_Comfort) super.clone();
        module_comfort.zone = zone.clone();
        for (int i=0; i<7; i++)
            module_comfort.zone[i] = zone[i].clone();
        return module_comfort;
    }
}
