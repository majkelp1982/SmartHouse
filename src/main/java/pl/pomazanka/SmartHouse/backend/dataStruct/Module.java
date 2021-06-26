package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.common.Logger;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public abstract class Module {
	public static final int FAULT_MAX = 20;
	@Autowired
	transient Diagnostic diagnostic;
	private int moduleType;
	private String moduleName;
	private transient String moduleStructureName;
	private transient int[] IP = new int[4];
	private transient Fault[] fault = new Fault[FAULT_MAX];
	private transient boolean upToDate = false;
	private LocalDateTime frameLastUpdate = LocalDateTime.now();
	private long localDateTimeLong;
	private transient LocalDateTime diagnosticLastUpdate = LocalDateTime.now();
	private transient boolean reqUpdateValues = false;
	private LocalDateTime lastSaveDateTime = LocalDateTime.now();

	public Module(int moduleType, String moduleName, String moduleStructureName) {
		this.moduleType = moduleType;
		this.moduleName = moduleName;
		this.moduleStructureName = moduleStructureName;
	}

	@PostConstruct
	public void postConstructor() throws Exception {
		diagnostic.addModule(moduleType, moduleName, moduleStructureName);
	}

	//Abstract declaration
	abstract void faultCheck();

	abstract void assignNV();

	abstract void faultListInit() throws Exception;

	public int getModuleType() {
		return moduleType;
	}

	public String getModuleName() {
		return moduleName;
	}

	public int[] getIP() {
		return IP;
	}

	public void updateDiag(int[] packetData) {
		diagnostic.updateDiag(getModuleType(), new int[]{packetData[3], packetData[4], packetData[5], packetData[6]},(-1*packetData[7]));
	}

	public void setFaultPresent(int faultNo, boolean present) {
		if (fault[faultNo] != null)
			fault[faultNo].setPresent(present);
	}

	public void setFaultText(int faultNo, String text) throws Exception {
		if (fault[faultNo] == null) fault[faultNo] = new Fault(text);
		else {
			throw new Exception("Double declaration of fault number " + faultNo);
		}
	}

	public void resetFaultPresent() {
		//Clear old fault present status
		for (int i = 0; i < FAULT_MAX; i++)
			if (fault[i] != null) setFaultPresent(i, false);
	}

	public void updateGlobalFaultList() {
		diagnostic.updateModuleFaultList(getModuleType(), fault);
	}

	public LocalDateTime getFrameLastUpdate() {
		return frameLastUpdate;
	}

	public void setFrameLastUpdate(LocalDateTime frameLastUpdate) {
		this.frameLastUpdate = frameLastUpdate;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String value = frameLastUpdate.format(formatter);
		this.localDateTimeLong = Long.valueOf(value);
	}

	public long getLocalDateTimeLong() {
		return localDateTimeLong;
	}

	public LocalDateTime getDiagnosticLastUpdate() {
		return diagnosticLastUpdate;
	}

	public void setDiagnosticLastUpdate(LocalDateTime diagnosticLastUpdate) {
		this.diagnosticLastUpdate = diagnosticLastUpdate;
	}

	protected boolean isUpToDate() {
		return upToDate;
	}

	public void setUpToDate(boolean upToDate) {
		this.upToDate = upToDate;
	}

	public boolean isReqUpdateValues() {
		return reqUpdateValues;
	}

	public void setReqUpdateValues(boolean reqUpdateValues) {
		this.reqUpdateValues = reqUpdateValues;
	}

	// return bit status from corresponding byte according to position in byte
	public boolean bitStatus(int data, int bytePos) {
		return ((data >> bytePos) & 1) == 1;
	}

	public boolean cmp(int value1, int value2) {
		return value1 == value2;
	}

	public boolean cmp(double value1, double value2, double tolerance) {
		if (Math.abs(value1 - value2) > tolerance) {
			return false;
		} else return true;
	}

	public boolean cmp(boolean value1, boolean value2) {
		return value1 == value2;
	}

	protected LocalDateTime getCurrentDate() {
		return LocalDateTime.now();
	}

	public boolean isTooLongWithoutSave() {
		long lastTime = ChronoUnit.MINUTES.between(lastSaveDateTime, getCurrentDate());
		if (lastTime > 10)
			return true;
		return false;
	}

	public void setLastSaveDateTime(LocalDateTime lastSaveDateTime) {
		this.lastSaveDateTime = lastSaveDateTime;
	}

	public double getFloatValue(double value) {
		double result;
		if (value >= 128)
			result = 128 - value;
		else result = value;
		int temp = (int) (result * 10.0);
		return ((double) temp) / 10.0;
	}

	public void dataParser(int[] packetData) {
		int controllerFrameNumber = packetData[2];
		if (controllerFrameNumber != 200)
			setFrameLastUpdate(getCurrentDate());
		else
			setDiagnosticLastUpdate(getCurrentDate());

		faultCheck();
		if (!isReqUpdateValues()) assignNV();

	}

	public class Fault {
		private boolean present;
		private String text;
		;

		public Fault(String text) {
			this.text = text;
		}

		public boolean isPresent() {
			return present;
		}

		public void setPresent(boolean present) {
			this.present = present;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
}
