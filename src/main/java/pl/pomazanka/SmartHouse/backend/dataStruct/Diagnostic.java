package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

@Service
@Configurable
public class Diagnostic {
    private String headerName = "Diagnostyka";
    private Date diagnosticLastUpdate = new Date();

    private ArrayList<ModuleDiagInfo> modules;
    private ArrayList<ModuleFault> globalFaultsList;

    public Diagnostic () {
        modules = new ArrayList<>();
        globalFaultsList = new ArrayList<>();
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

    public void updateModuleFaultList(int moduleTyp, Module.Fault[] moduleFaultList) {
        //Clear global list
        globalFaultsList.clear();
        //Get proper module type
        for (ModuleDiagInfo module : modules) {
            if (module.getModuleType() == moduleTyp) {
                // Get each fault already present in global list
                for (ModuleDiagInfo.Fault fault : module.getFaultList()) {
                    // When fault active, but not present
                    if ((!moduleFaultList[fault.getIndex()].isPresent()) && (fault.getOutgoing() == null))
                        fault.setOutgoing(LocalDateTime.now());
                }
                //Get each fault from module list
                for (int i = 0; i<Module.FAULT_MAX; i++) {
                    if (moduleFaultList[i] == null) break;

                    if (moduleFaultList[i].isPresent()) {
                        boolean reqNewInstance = true;
                        for (ModuleDiagInfo.Fault fault : module.getFaultList()) {
                            if ((fault.getIndex() == i) && (fault.getOutgoing() == null)) {
                                reqNewInstance = false;
                                continue;
                            }
                        }
                        if (reqNewInstance)
                            module.addFault(i, LocalDateTime.now(), moduleFaultList[i].getText());
                    }
                }
            }
            //Update global fault list
            for (ModuleDiagInfo.Fault fault : module.getFaultList()) {
                if (fault == null) break;
                globalFaultsList.add(new ModuleFault(module.moduleType, module.moduleName, fault.incoming, fault.outgoing, fault.index, fault.description));
            }
        }
    }

    public ArrayList<ModuleDiagInfo> getModules() {
        return modules;
    }

    public ArrayList<ModuleFault> getGlobalFaultsList() {
        return globalFaultsList;
    }

    public class ModuleDiagInfo {
        private int moduleType;
        private String moduleName;
        private int[] IP = new int[4];
        private ArrayList<Fault> faultList;

        public ModuleDiagInfo(int moduleType, String moduleName) {
            this.moduleType = moduleType;
            this.moduleName = moduleName;
            faultList = new ArrayList<>();
        }

        private class Fault {
            private LocalDateTime incoming;
            private LocalDateTime outgoing = null;
            private int index;
            private String description;

            public Fault (int index, LocalDateTime incoming, String description) {
                this.index = index;
                this.incoming = incoming;
                this.description = description;
            }

            public LocalDateTime getIncoming() {
                return incoming;
            }

            public LocalDateTime getOutgoing() {
                return outgoing;
            }

            public void setOutgoing(LocalDateTime outgoing) {
                this.outgoing = outgoing;
            }

            public int getIndex() {
                return index;
            }

            public String getDescription() {
                return description;
            }
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

        public ArrayList<Fault> getFaultList() {
            return faultList;
        }

        public void addFault(int index, LocalDateTime incoming, String description) {
            faultList.add(new Fault(index, incoming, description));
        }
    }

    public class ModuleFault {
        private int moduleType;
        private String moduleName;
        private LocalDateTime incoming;
        private LocalDateTime outgoing;
        private long activeTime;
        private int index;
        private String description;

        public ModuleFault(int moduleType, String moduleName, LocalDateTime incoming, LocalDateTime outgoing, int index, String description) {
            this.moduleType =moduleType;
            this.moduleName = moduleName;
            this.incoming = incoming;
            this.outgoing = outgoing;
            this.index = index;
            this.description = description;
        }

        public int getModuleType() {
            return moduleType;
        }

        public String getModuleName() {
            return moduleName;
        }

        public LocalDateTime getIncoming() {
            return incoming;
        }

        public String getIncomingToString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (incoming != null) return incoming.format(formatter);
            else return null;
        }

        public LocalDateTime getOutgoing() {
            return outgoing;
        }

        public String getOutgoingToString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (outgoing != null) return outgoing.format(formatter);
            else return null;
        }

        public long getActiveTime() {
            if (outgoing == null) activeTime = ChronoUnit.SECONDS.between(incoming, LocalDateTime.now());
            else activeTime = ChronoUnit.SECONDS.between(incoming, outgoing);
            return activeTime;
        }

        public int getIndex() {
            return index;
        }

        public String getDescription() {
            return description;
        }
    }
}
