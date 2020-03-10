package pl.pomazanka.SmartHouse.backend.dataStruct;

import java.util.Date;

public class Module {
    private int moduleTyp;
    private String moduleName;
    private int IP[] = new int[4];
    private boolean error = false;
    private boolean upToDate = false;
    private Date frameLastUpdate = new Date();
    private Date diagnosticLastUpdate = new Date();


    public Module(int moduleTyp,String moduleName) {
        this.moduleTyp = moduleTyp;
        this.moduleName = moduleName;
    }

    public int getModuleTyp() {
        return moduleTyp;
    }

    public String getModuleName() {
        return moduleName;
    }

    public int[] getIP() {
        return IP;
    }

    public void setIP(int[] IP) {
        this.IP = IP;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public Date getFrameLastUpdate() {
        return frameLastUpdate;
    }

    public void setFrameLastUpdate(Date frameLastUpdate) {
        this.frameLastUpdate = frameLastUpdate;
    }

    public Date getDiagnosticLastUpdate() {
        return diagnosticLastUpdate;
    }

    public void setDiagnosticLastUpdate(Date diagnosticLastUpdate) {
        this.diagnosticLastUpdate = diagnosticLastUpdate;
    }

    protected boolean isUpToDate() {
        return upToDate;
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    // return bit status from corresponding byte according to position in byte
    public boolean bitStatus(int data, int bytePos) {
        if (((data >> bytePos) & 1) == 1) return true;
        else return false;
    }

    public boolean cmp(int value1, int value2) {
        if (value1 != value2) return false;
        else return true;
    }

    public boolean cmp(double value1, double value2, double tolerance) {
        if (Math.abs(value1 - value2) > tolerance) {
            System.out.println("FLOAT FALSE");
            return false;
        } else return true;
    }

    public boolean cmp(boolean value1, boolean value2) {
        if (value1 != value2) return false;
        else return true;
    }

    protected Date getCurrentDate() {
        Date nowDate = new Date();
        return nowDate;
    }
}
