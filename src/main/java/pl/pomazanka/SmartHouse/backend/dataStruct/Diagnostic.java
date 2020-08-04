package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class Diagnostic {
    private String moduleName = "Diagnostyka";
    private Date diagnosticLastUpdate = new Date();

    public Diagnostic () {
        moduleName = "Diagnostyka";
    }

    public String getModuleName() {
        return moduleName;
    }
    public Date getDiagnosticLastUpdate() {
        return diagnosticLastUpdate;
    }
    public void setDiagnosticLastUpdate(Date diagnosticLastUpdate) {
        this.diagnosticLastUpdate = diagnosticLastUpdate;
    }

}
