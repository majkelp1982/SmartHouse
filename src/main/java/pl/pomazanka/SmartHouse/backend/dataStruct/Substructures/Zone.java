package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class Zone {
  private String name;
  private ControlValue request = new ControlValue(false);

  public Zone(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ControlValue getRequest() {
    return request;
  }

  public void setRequest(ControlValue request) {
    this.request = request;
  }
}
