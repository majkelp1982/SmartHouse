package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class Mode {
  private final ControlValue trigger = new ControlValue(false); // trigger to turn on mode
  private final ControlValue triggerInt = new ControlValue(0); // trigger to turn on mode
  private final ControlValue delayTime =
      new ControlValue(0); // delay time after trigger no more active
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

  public void setTimeLeft(final int timeLeft) {
    this.timeLeft = timeLeft;
  }
}
