package pl.pomazanka.SmartHouse.backend.dataStruct.Equipment;

public class LightDimmer implements Cloneable {
  private boolean force0;
  private boolean forceMax;
  private int intens;
  private int standByIntens;
  private int maxIntens;

  public LightDimmer() {}

  public boolean isForce0() {
    return force0;
  }

  public void setForce0(boolean force0) {
    this.force0 = force0;
  }

  public boolean isForceMax() {
    return forceMax;
  }

  public void setForceMax(boolean forceMax) {
    this.forceMax = forceMax;
  }

  public int getIntens() {
    return intens;
  }

  public void setIntens(int intens) {
    this.intens = intens;
  }

  public int getStandByIntens() {
    return standByIntens;
  }

  public void setStandByIntens(int standByIntens) {
    this.standByIntens = standByIntens;
  }

  public int getMaxIntens() {
    return maxIntens;
  }

  public void setMaxIntens(int maxIntens) {
    this.maxIntens = maxIntens;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
