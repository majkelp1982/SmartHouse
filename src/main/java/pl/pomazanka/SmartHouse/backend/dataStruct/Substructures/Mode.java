package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class Mode {
	private ControlValue trigger = new ControlValue(false);			        // trigger to turn on mode
	private ControlValue triggerInt = new ControlValue(0);						// trigger to turn on mode
	private ControlValue delayTime = new ControlValue(0);						// delay time after trigger no more active
	private int timeLeft;

	public ControlValue getTrigger() {
		return trigger;
	}

	public ControlValue getTriggerInt() {
		return triggerInt;
	}

	public ControlValue getDelayTime() {
		return delayTime;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}
}
