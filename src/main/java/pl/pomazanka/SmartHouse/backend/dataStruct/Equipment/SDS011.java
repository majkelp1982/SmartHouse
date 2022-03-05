package pl.pomazanka.SmartHouse.backend.dataStruct.Equipment;

public class SDS011 implements Cloneable {
  private int pm25;
  private int pm10;

  public SDS011() {}

  public int getPm25() {
    return pm25;
  }

  public void setPm25(final int pm25) {
    this.pm25 = pm25;
  }

  public int getPm10() {
    return pm10;
  }

  public void setPm10(final int pm10) {
    this.pm10 = pm10;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
