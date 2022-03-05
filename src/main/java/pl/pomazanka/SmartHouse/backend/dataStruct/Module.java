package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.ControlValue;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.Mode;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.VentZones;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.Zone;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public abstract class Module {
  public static final int FAULT_MAX = 200;
  private final int moduleType;
  private final String moduleName;
  private final transient String moduleStructureName;
  private final transient int[] IP = new int[4];
  private final transient Fault[] fault = new Fault[FAULT_MAX];
  @Autowired transient Diagnostic diagnostic;
  private transient boolean upToDate = false;
  private LocalDateTime frameLastUpdate = LocalDateTime.now();
  private long localDateTimeLong;
  private transient LocalDateTime diagnosticLastUpdate = LocalDateTime.now();
  private transient boolean reqUpdateValues = false;
  private LocalDateTime lastSaveDateTime = LocalDateTime.now();

  public Module(final int moduleType, final String moduleName, final String moduleStructureName) {
    this.moduleType = moduleType;
    this.moduleName = moduleName;
    this.moduleStructureName = moduleStructureName;
  }

  @PostConstruct
  public void postConstructor() throws Exception {
    diagnostic.addModule(moduleType, moduleName, moduleStructureName);
  }

  // Abstract declaration
  abstract void faultCheck();

  abstract void faultListInit() throws Exception;

  public int getModuleType() {
    return moduleType;
  }

  public String getModuleName() {
    return moduleName;
  }

  public String getModuleStructureName() {
    return moduleStructureName;
  }

  public int[] getIP() {
    return IP;
  }

  public void updateDiag(final int[] packetData) {
    diagnostic.updateDiag(
        getModuleType(),
        new int[] {packetData[3], packetData[4], packetData[5], packetData[6]},
        (-1 * packetData[7]));
  }

  protected void assignNV(final Object object) throws Exception {
    final Field[] fields = this.getClass().getDeclaredFields();

    for (int i = 0; i < fields.length; i++) {
      final String fieldType = fields[i].getGenericType().getTypeName();
      if (fieldType == null) {
        throw new Exception("Zmienna nie może być typem null");
      }
      if (fieldType.toUpperCase().contains("INT")) {
        continue;
      }
      if (fieldType.toUpperCase().contains("BYTE")) {
        continue;
      }
      if (fieldType.toUpperCase().contains("BOOLEAN")) {
        continue;
      }
      if (fieldType.toUpperCase().contains("FLOAT")) {
        continue;
      }
      if (fieldType.toUpperCase().contains("BME")) {
        continue;
      }
      if (fieldType.toUpperCase().contains("FAN")) {
        continue;
      }
      fields[i].setAccessible(true);
      if (fields[i].getType() == ControlValue.class) {
        final ControlValue value = (ControlValue) fields[i].get(this);
        value.setUpToDate();
        continue;
      }
      if (fields[i].getType() == Mode.class) {
        final Mode value = (Mode) fields[i].get(this);
        final ControlValue trigger = value.getTrigger();
        final ControlValue triggerInt = value.getTriggerInt();
        final ControlValue delayTime = value.getDelayTime();
        trigger.setUpToDate();
        triggerInt.setUpToDate();
        delayTime.setUpToDate();
        continue;
      }

      if (fields[i].getType() == VentZones[].class) {
        final VentZones[] ventZones = (VentZones[]) fields[i].get(this);
        for (int j = 0; j < ventZones.length; j++) {
          final Field[] subFields = ventZones[j].getClass().getDeclaredFields();
          for (int k = 0; k < subFields.length; k++) {
            subFields[k].setAccessible(true);
            final Zone zone = (Zone) subFields[k].get(ventZones[j]);
            final ControlValue subValue = zone.getRequest();
            subValue.setUpToDate();
          }
        }
        continue;
      }
      // if still type not found throw exception
      throw new Exception("Nieznany typ zmiennej");
    }
  }

  public void setFaultPresent(final int faultNo, final boolean present) {
    if (fault[faultNo] != null) {
      fault[faultNo].setPresent(present);
    }
  }

  public void setFaultText(final int faultNo, final String text) throws Exception {
    if (fault[faultNo] == null) {
      fault[faultNo] = new Fault(text);
    } else {
      throw new Exception("Double declaration of fault number " + faultNo);
    }
  }

  public void resetFaultPresent() {
    // Clear old fault present status
    for (int i = 0; i < FAULT_MAX; i++) {
      if (fault[i] != null) {
        setFaultPresent(i, false);
      }
    }
  }

  public void updateGlobalFaultList() {
    diagnostic.updateModuleFaultList(getModuleType(), fault);
  }

  public LocalDateTime getFrameLastUpdate() {
    return frameLastUpdate;
  }

  public void setFrameLastUpdate(final LocalDateTime frameLastUpdate) {
    this.frameLastUpdate = frameLastUpdate;
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    final String value = frameLastUpdate.format(formatter);
    this.localDateTimeLong = Long.valueOf(value);
  }

  public long getLocalDateTimeLong() {
    return localDateTimeLong;
  }

  public LocalDateTime getDiagnosticLastUpdate() {
    return diagnosticLastUpdate;
  }

  public void setDiagnosticLastUpdate(final LocalDateTime diagnosticLastUpdate) {
    this.diagnosticLastUpdate = diagnosticLastUpdate;
  }

  protected boolean isUpToDate() {
    return upToDate;
  }

  public void setUpToDate(final boolean upToDate) {
    this.upToDate = upToDate;
  }

  public boolean isReqUpdateValues() {
    return reqUpdateValues;
  }

  public void setReqUpdateValues(final boolean reqUpdateValues) {
    this.reqUpdateValues = reqUpdateValues;
  }

  // return bit status from corresponding byte according to position in byte
  public boolean bitStatus(final int data, final int bytePos) {
    return ((data >> bytePos) & 1) == 1;
  }

  public boolean cmp(final int value1, final int value2) {
    return value1 == value2;
  }

  public boolean cmp(final double value1, final double value2, final double tolerance) {
    if (Math.abs(value1 - value2) > tolerance) {
      return false;
    } else {
      return true;
    }
  }

  public boolean cmp(final boolean value1, final boolean value2) {
    return value1 == value2;
  }

  public boolean cmp(final ControlValue value1, final ControlValue value2) {
    return value1.getIsValue().equals(value2.getIsValue());
  }

  protected LocalDateTime getCurrentDate() {
    return LocalDateTime.now();
  }

  public boolean isTooLongWithoutSave() {
    final long lastTime = ChronoUnit.MINUTES.between(lastSaveDateTime, getCurrentDate());
    if (lastTime > 10) {
      return true;
    }
    return false;
  }

  public void setLastSaveDateTime(final LocalDateTime lastSaveDateTime) {
    this.lastSaveDateTime = lastSaveDateTime;
  }

  public double getFloatValue(final double value) {
    final double result;
    if (value >= 128) {
      result = 128 - value;
    } else {
      result = value;
    }
    final int temp = (int) (result * 10.0);
    return ((double) temp) / 10.0;
  }

  public void dataParser(final int[] packetData) {
    final int controllerFrameNumber = packetData[2];
    if (controllerFrameNumber != 200) {
      setFrameLastUpdate(getCurrentDate());
    } else {
      setDiagnosticLastUpdate(getCurrentDate());
    }

    faultCheck();
    try {
      if (!isReqUpdateValues()) {
        assignNV(this);
      }
    } catch (final Exception e) {
      e.printStackTrace();
      // FIXME tłumimy?
    }
  }

  public class Fault {
    private boolean present;
    private String text;

    public Fault(final String text) {
      this.text = text;
    }

    public boolean isPresent() {
      return present;
    }

    public void setPresent(final boolean present) {
      this.present = present;
    }

    public String getText() {
      return text;
    }

    public void setText(final String text) {
      this.text = text;
    }
  }
}
