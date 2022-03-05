package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class Zone {
  private final String name;
  private ControlValue request = new ControlValue(false);

  public Zone(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ControlValue getRequest() {
    return request;
  }

  public void setRequest(final ControlValue request) {
    this.request = request;
  }
}
