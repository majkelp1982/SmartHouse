package pl.pomazanka.SmartHouse.backend.dataStruct.Equipment;

public class Fan {
  private int speed;
  private int rev;

  public Fan() {}

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(final int speed) {
    this.speed = speed;
  }

  public int getRev() {
    return rev;
  }

  public void setRev(final int rev) {
    this.rev = rev;
  }
}
