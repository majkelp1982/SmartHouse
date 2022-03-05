package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import pl.pomazanka.SmartHouse.backend.common.Logger;
import pl.pomazanka.SmartHouse.backend.communication.Email.Email;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.System.currentTimeMillis;

@Service
@Configurable
public class Diagnostic extends Module {
  private static final transient byte MODULE_TYPE = 01;
  private final String headerName = "Diagnostyka";
  private final ArrayList<ModuleDiagInfo> modules;
  private final ArrayList<ModuleFault> globalFaultsList;
  private LocalDateTime diagnosticLastUpdate = LocalDateTime.now();
  private boolean globalFaultsListGroupByFault;

  public Diagnostic() throws Exception {
    super(MODULE_TYPE, "Główny", "module_main");
    modules = new ArrayList<>();
    addModule(MODULE_TYPE, "Główny", "module_main");
    globalFaultsList = new ArrayList<>();
    faultListInit();
    globalFaultsListGroupByFault = true;
  }

  @Override
  @PostConstruct
  public void postConstructor() {
    final InnerThread innerThread = new InnerThread(this);
    innerThread.start();
  }

  @Override
  void faultCheck() {
    // Clear previous faults status
    resetFaultPresent();

    // Fault check list
    for (final ModuleDiagInfo module : modules) {
      if ((module.getDiagLastUpdate() >= 720) && (module.getDiagLastUpdate() != 999999)) {
        setFaultPresent(module.getModuleType(), true);
      }
    }

    // TODO fault list to extend
    try {
      updateGlobalFaultList();
    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void assignNV(final Object object) throws Exception {
    return;
  }

  @Override
  void faultListInit() throws Exception {}

  @Override
  public String getModuleName() {
    return headerName;
  }

  @Override
  public LocalDateTime getDiagnosticLastUpdate() {
    return diagnosticLastUpdate;
  }

  @Override
  public void setDiagnosticLastUpdate(final LocalDateTime diagnosticLastUpdate) {
    this.diagnosticLastUpdate = diagnosticLastUpdate;
  }

  public void globalFaultsListGroupByFault() {
    globalFaultsListGroupByFault = !globalFaultsListGroupByFault;
  }

  public boolean isGlobalFaultsListGroupByFault() {
    return globalFaultsListGroupByFault;
  }

  public void setGlobalFaultsListGroupByFault(final boolean globalFaultsListGroupByFault) {
    this.globalFaultsListGroupByFault = globalFaultsListGroupByFault;
  }

  public void addModule(final int moduleType, final String moduleName, final String structureName)
      throws Exception {
    modules.add(new ModuleDiagInfo(moduleType, moduleName, structureName));
    if (moduleType != 1) {
      setFaultText(
          moduleType,
          "Moduł "
              + moduleName.toUpperCase()
              + "["
              + moduleType
              + "] nie odpowiada od dłuższego czasu");
    }
  }

  public void updateDiag(final int moduleTyp, final int[] IP, final int signal) {
    Logger.debug("updateDiag");
    int i = 0;
    Logger.debug("ilość modułów " + modules.size());
    for (final ModuleDiagInfo module : modules) {
      i++;
      if (module.getModuleType() == moduleTyp) {
        Logger.debug("moduł odnaleziono na pozycji " + i);
        module.setIP(IP);
        Logger.debug("IP ustawione");
        module.setSignal(signal);
        Logger.debug("sygnał ustawiony");
        module.setFirmwareVersion(module.getIP());
        Logger.debug("firmware ustawiony");
        module.setLastDiagUpdate(LocalDateTime.now());
        Logger.debug("data ustawiona");
      }
    }
    Logger.debug("updateDiag zakończone");
  }

  public void updateModuleFaultList(final int moduleTyp, final Module.Fault[] moduleFaultList) {
    // Get proper module type
    for (final ModuleDiagInfo module : modules) {
      if (module.getModuleType() == moduleTyp) {
        // Get each fault already present in global list
        for (final ModuleDiagInfo.Fault fault : module.getFaultList()) {
          // When fault active, but not present
          if ((!moduleFaultList[fault.getIndex()].isPresent()) && (fault.getOutgoing() == null)) {
            fault.setOutgoing(LocalDateTime.now());
          }
        }
        // Get each fault from module list
        for (int i = 0; i < Module.FAULT_MAX; i++) {
          if (moduleFaultList[i] == null) {
            continue;
          }

          if (moduleFaultList[i].isPresent()) {
            boolean reqNewInstance = true;
            for (final ModuleDiagInfo.Fault fault : module.getFaultList()) {
              if ((fault.getIndex() == i) && (fault.getOutgoing() == null)) {
                reqNewInstance = false;
                continue;
              }
            }
            if (reqNewInstance) {
              module.addFault(i, LocalDateTime.now(), moduleFaultList[i].getText());
            }
          }
        }
      }
    }
    refreshGlobalFaultList();
  }

  void sendEmailAlert() {
    if (globalFaultsList.size() == 0) {
      return;
    }
    final Email email = new Email();
    final StringBuilder htmlTable = new StringBuilder();
    htmlTable.append(
        "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "<style>\n"
            + "table {\n"
            + "  font-family: arial, sans-serif;\n"
            + "  border-collapse: collapse;\n"
            + "  width: 100%;\n"
            + "}\n"
            + "\n"
            + "td, th {\n"
            + "  border: 1px solid #dddddd;\n"
            + "  text-align: left;\n"
            + "  padding: 8px;\n"
            + "}\n"
            + "\n"
            + "tr:nth-child(even) {\n"
            + "  background-color: #dddddd;\n"
            + "}\n"
            + "</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "\n"
            + "<h2>Aktualna lista błędów</h2>\n"
            + "\n"
            + "<table>\n"
            + "  <tr>\n"
            + "    <th>Typ</th>\n"
            + "    <th>Nazwa Modułu</th>\n"
            + "    <th>Opis</th>\n"
            + "    <th>Początek</th>\n"
            + "    <th>Koniec</th>\n"
            + "    <th>czas trwania</th>\n"
            + "    <th>lb.wystapień</th>\n"
            + "  </tr>\n");
    getGlobalFaultsList()
        .forEach(
            moduleFault -> {
              htmlTable.append(
                  "  <tr>\n"
                      + "<td>"
                      + moduleFault.getModuleType()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getModuleName()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getDescription()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getIncomingToString()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getOutgoingToString()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getActiveTime()
                      + "</td>\n"
                      + "<td>"
                      + moduleFault.getNumberOfErrors()
                      + "</td>\n"
                      + "  </tr>\n");
            });
    htmlTable.append("</table>\n" + "\n" + "</body>\n" + "</html>");
    email.sendEmail(htmlTable.toString());
  }

  public void refreshGlobalFaultList() {
    // Clear global list
    final ArrayList<String> activeErrorList = new ArrayList<>();
    for (final ModuleFault fault : globalFaultsList) {
      activeErrorList.add("" + fault.getModuleType() + fault.getIndex());
    }
    globalFaultsList.clear();
    for (final ModuleDiagInfo module : modules) {
      // Update global fault list
      for (final ModuleDiagInfo.Fault fault : module.getFaultList()) {
        if (fault == null) {
          break;
        }
        if (!isGlobalFaultsListGroupByFault()) {
          globalFaultsList.add(
              new ModuleFault(
                  module.moduleType,
                  module.moduleName,
                  fault.incoming,
                  fault.outgoing,
                  fault.index,
                  fault.description));
        } else {
          boolean exist = false;
          for (final ModuleFault tmp : globalFaultsList) {
            if ((tmp.getModuleType() == module.getModuleType())
                && (tmp.index == fault.index)
                && (tmp.outgoing != null)
                && (fault.outgoing != null)) {
              exist = true;
              tmp.setActiveTime(
                  tmp.getActiveTime()
                      + (ChronoUnit.SECONDS.between(fault.incoming, fault.outgoing)));
              tmp.increaseErrorNumber();
              tmp.outgoing = fault.outgoing;
            }
          }
          if (!exist) {
            globalFaultsList.add(
                new ModuleFault(
                    module.moduleType,
                    module.moduleName,
                    fault.incoming,
                    fault.outgoing,
                    fault.index,
                    fault.description));
          }
        }
      }
    }
    for (final ModuleFault fault : globalFaultsList) {
      if (!activeErrorList.contains("" + fault.getModuleType() + fault.getIndex())) {
        sendEmailAlert();
        break;
      }
    }
  }

  public ArrayList<ModuleDiagInfo> getModules() {
    return modules;
  }

  public ArrayList<ModuleFault> getGlobalFaultsList() {
    return globalFaultsList;
  }

  public void resetGlobalList() {
    for (final Iterator<ModuleDiagInfo> iterator = modules.iterator(); iterator.hasNext(); ) {
      final ModuleDiagInfo module = iterator.next();
      for (final Iterator<ModuleDiagInfo.Fault> iterator1 = module.getFaultList().iterator();
          iterator1.hasNext(); ) {
        final ModuleDiagInfo.Fault fault = iterator1.next();
        if (fault.getOutgoing() != null) {
          iterator1.remove();
        }
      }
    }
  }

  private static class InnerThread extends Thread {
    Diagnostic diagnostic;

    public InnerThread(final Diagnostic diagnostic) {
      this.diagnostic = diagnostic;
    }

    @Override
    public void run() {
      long lastWatchdogCheck = currentTimeMillis();
      while (true) {
        try {
          diagnostic.faultCheck();
          if (lastWatchdogCheck + 10000 < currentTimeMillis()) {
            lastWatchdogCheck = currentTimeMillis();
            //
            //	System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd
            // HH:mm:ss")) + " Diagnostic thread OK");
          }
          Thread.sleep(10000);
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
    }
  }

  public class ModuleDiagInfo {
    private final int moduleType;
    private final String moduleName;
    private final String moduleStructureName;
    private final ArrayList<Fault> faultList;
    private String firmwareVersion;
    private LocalDateTime diagLastUpdate;
    private int[] IP = new int[4];
    private int signal;

    public ModuleDiagInfo(
        final int moduleType, final String moduleName, final String moduleStructureName) {
      this.moduleType = moduleType;
      this.moduleName = moduleName;
      this.moduleStructureName = moduleStructureName;
      faultList = new ArrayList<>();
    }

    public int getModuleType() {
      return moduleType;
    }

    public String getModuleName() {
      return moduleName;
    }

    public String getFirmwareVersion() {
      return firmwareVersion;
    }

    public void setFirmwareVersion(final String IPAddress) {
      try {
        final URL url = new URL("http://" + IPAddress + "/");
        final URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        final BufferedReader in =
            new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String inputLine = in.readLine();
        final String version =
            inputLine.substring(inputLine.indexOf("<i>") + 15, inputLine.indexOf("</i>"));
        this.firmwareVersion = version;
      } catch (final Exception e) {
        Logger.error("Wyjątek przy pobrani firmware");
        this.firmwareVersion = e.toString();
      }
    }

    public Long getDiagLastUpdate() {
      if (diagLastUpdate != null) {
        return Duration.between(diagLastUpdate, LocalDateTime.now()).getSeconds();
      } else {
        return 999999L;
      }
    }

    public int getSignal() {
      return signal;
    }

    public void setSignal(final int signal) {
      this.signal = signal;
    }

    public String getIP() {
      return String.format("%d.%d.%d.%d", IP[0], IP[1], IP[2], IP[3]);
    }

    public void setIP(final int[] IP) {
      this.IP = IP;
      setDiagnosticLastUpdate(LocalDateTime.now());
    }

    public void setLastDiagUpdate(final LocalDateTime diagLastUpdate) {
      this.diagLastUpdate = diagLastUpdate;
    }

    public ArrayList<Fault> getFaultList() {
      return faultList;
    }

    public void addFault(final int index, final LocalDateTime incoming, final String description) {
      faultList.add(new Fault(index, incoming, description));
    }

    public String getModuleStructureName() {
      return moduleStructureName;
    }

    private class Fault {
      private final LocalDateTime incoming;
      private final int index;
      private final String description;
      private LocalDateTime outgoing = null;

      public Fault(final int index, final LocalDateTime incoming, final String description) {
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

      public void setOutgoing(final LocalDateTime outgoing) {
        this.outgoing = outgoing;
      }

      public int getIndex() {
        return index;
      }

      public String getDescription() {
        return description;
      }
    }
  }

  public class ModuleFault {
    private final int moduleType;
    private final String moduleName;
    private final LocalDateTime incoming;
    private final int index;
    private final String description;
    private LocalDateTime outgoing;
    private long activeTime;
    private int numberOfErrors;

    public ModuleFault(
        final int moduleType,
        final String moduleName,
        final LocalDateTime incoming,
        final LocalDateTime outgoing,
        final int index,
        final String description) {
      this.moduleType = moduleType;
      this.moduleName = moduleName;
      this.incoming = incoming;
      this.outgoing = outgoing;
      this.index = index;
      this.description = description;
      this.numberOfErrors = 1;

      if (outgoing == null) {
        activeTime = ChronoUnit.SECONDS.between(incoming, LocalDateTime.now());
      } else {
        activeTime = ChronoUnit.SECONDS.between(incoming, outgoing);
      }
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
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      if (incoming != null) {
        return incoming.format(formatter);
      } else {
        return null;
      }
    }

    public LocalDateTime getOutgoing() {
      return outgoing;
    }

    public String getOutgoingToString() {
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      if (outgoing != null) {
        return outgoing.format(formatter);
      } else {
        return null;
      }
    }

    public long getActiveTime() {
      return activeTime;
    }

    public void setActiveTime(final long activeTime) {
      this.activeTime = activeTime;
    }

    public void increaseErrorNumber() {
      numberOfErrors++;
    }

    public int getIndex() {
      return index;
    }

    public int getNumberOfErrors() {
      return numberOfErrors;
    }

    public String getDescription() {
      return description;
    }
  }
}
