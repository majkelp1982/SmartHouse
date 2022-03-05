package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

import java.util.Objects;

public class ControlValue {
  private Object isValue;
  private Object newValue;

  public ControlValue(final Object value) {
    isValue = value;
    newValue = value;
  }

  public Object getIsValue() {
    return isValue;
  }

  public void setIsValue(final Object isValue) {
    checkCompatibility(this.isValue, isValue);
    this.isValue = isValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  public void setNewValue(final Object newValue) {
    checkCompatibility(this.newValue, newValue);
    this.newValue = newValue;
  }

  public boolean isUpToDate() {
    return isValue.equals(newValue);
  }

  public void setUpToDate() {
    setNewValue(getIsValue());
  }

  private boolean checkCompatibility(final Object obj1, final Object obj2) {
    if (!(obj1.getClass() == obj2.getClass())) {
      throw new IllegalArgumentException(
          "Klasa " + obj1.getClass() + " jest niezgodnia z oczekiwaną klasa " + obj2.getClass());
    }
    return true;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ControlValue that = (ControlValue) o;
    return isValue.equals(that.isValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValue);
  }
}
