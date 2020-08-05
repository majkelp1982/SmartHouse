package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

@Service
@Configurable
public class Diagnostic {
    private String headerName = "Diagnostyka";
    private Date diagnosticLastUpdate = new Date();

    private ArrayList<ModuleDiagInfo> modules;

    public Diagnostic () {
        headerName = "Diagnostyka";
        modules = new ArrayList<>();
    }

    public String getModuleName() {
        return headerName;
    }
    public Date getDiagnosticLastUpdate() {
        return diagnosticLastUpdate;
    }
    public void setDiagnosticLastUpdate(Date diagnosticLastUpdate) {
        this.diagnosticLastUpdate = diagnosticLastUpdate;
    }

    public void addModule (int moduleType, String moduleName) {
        modules.add(new ModuleDiagInfo(moduleType,moduleName));
    }

    public void updateIP (int moduleTyp, int[] IP) {
        for (ModuleDiagInfo module: modules)
            if (module.getModuleType() == moduleTyp)
                module.setIP(IP);
    }

    public ArrayList<ModuleDiagInfo> getModules() {
        return modules;
    }

    public class ModuleDiagInfo {
        private int moduleType;
        private String moduleName;
        private int[] IP = new int[4];
        private boolean error = false;
        private ArrayList<FaultList> faultList;

        public ModuleDiagInfo(int moduleType, String moduleName) {
            this.moduleType = moduleType;
            this.moduleName = moduleName;
        }

        private class FaultList {
            private LocalDate incoming;
            private LocalDate outgoing;
            private String description;

        }

        public int getModuleType() {
            return moduleType;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getIP() {
            return String.format("%d.%d.%d.%d",IP[0],IP[1],IP[2],IP[3]);
        }

        public void setIP(int[] IP) {
            this.IP = IP;
            setDiagnosticLastUpdate(new Date());
        }

        public boolean isError() {
            return error;
        }
    }
}
