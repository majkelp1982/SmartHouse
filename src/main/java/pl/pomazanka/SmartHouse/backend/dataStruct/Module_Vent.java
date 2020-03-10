package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;

@Controller
public class Module_Vent extends Module implements Cloneable {

    private boolean fanON;
    private int[] hour = new int[12];
    protected int[] NVHour = new int[12];

    public Module_Vent() {
        super(3,"Wentylacja");
    }

    public boolean isFanON() {
        return fanON;
    }

    public int[] getHour() {
        return hour;
    }

    protected int[] getNVHour() {
        return NVHour;
    }

    // Check if values are Up to Date
    public void  setNVHour01(int NVHour){
        this.NVHour[0] = NVHour;
        setUpToDate(false);
    }
    public void  setNVHour23(int NVHour){
        this.NVHour[1] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour45(int NVHour){
        this.NVHour[2] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour67(int NVHour){
        this.NVHour[3] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour89(int NVHour){
        this.NVHour[4] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour1011(int NVHour){
        this.NVHour[5] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour1213(int NVHour){
        this.NVHour[6] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour1415(int NVHour){
        this.NVHour[7] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour1617(int NVHour){
        this.NVHour[8] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour1819(int NVHour){
        this.NVHour[9] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour2021(int NVHour){
        this.NVHour[10] = NVHour;
        setUpToDate(false);
    }

    public void  setNVHour2223(int NVHour){
        this.NVHour[11] = NVHour;
        setUpToDate(false);
    }

    protected boolean isAllUpToDate() {
        setUpToDate(true);
        for (int i=0; i<=11; i++)
            if (isUpToDate()) setUpToDate(hour[i] == NVHour[i]);
        return isUpToDate();
    }

    //Parser for data package coming via UDP
    public void dataParser(int[] packetData) {
        int controllerFrameNumber = packetData[2];

        switch (controllerFrameNumber) {
            case 0: // standard frame 0
                fanON = bitStatus(packetData[3], 7);

                // copy relevant data received into array
                for (int i=0; i<12; i++)
                    hour[i] = packetData[4+i];
                break;

            case 200: //diagnostic frame
                setDiagnosticLastUpdate(getCurrentDate());
                setIP(new int[]{packetData[3],packetData[4],packetData[5],packetData[6]});
                //TODO diagnostic frame
                break;
        }
    }

    public boolean compare(Module_Vent module_vent) {
        //return FALSE if compare data are different
        boolean result = true;
        if (result) result = (module_vent.fanON == fanON);
        for (int i=0; i<12; i++)
            if (result) result = (module_vent.hour[i] == hour[i]);
        return result;
    }

    @Override
    public Module_Vent clone() throws CloneNotSupportedException {
        Module_Vent module_vent = (Module_Vent) super.clone();
        module_vent.hour = hour.clone();
        module_vent.NVHour = NVHour.clone();
        return module_vent;
    }

}
