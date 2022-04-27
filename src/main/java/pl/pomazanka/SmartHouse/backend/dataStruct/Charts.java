package pl.pomazanka.SmartHouse.backend.dataStruct;

import com.github.appreciated.apexcharts.helper.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.communication.MongoDBController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Controller
public class Charts {

  private final int OFFSET = 1;

  // Wired classes
  @Autowired MongoDBController mongoDBController;

  public Charts() {}

  public Coordinate[] getSerie(
      final String collectionName,
      final String variableName,
      final LocalDateTime from,
      final LocalDateTime to)
      throws Exception {
    final ArrayList<Data> list;
    list = mongoDBController.getValues(collectionName, variableName, from, to);
    final int size;
    if (list.get(0).isNumber) {
      size = list.size();
    } else {
      size = list.size() * 2;
    }
    final Coordinate[] serie = new Coordinate[size];

    int i = 0;
    int tempValue = 0;
    for (final Data temp : list) {
      final LocalDateTime timeStamp = temp.getTimeStamp();
      // Add offset
      final LocalDateTime timeWithOffset = timeStamp.plusHours(OFFSET);
      if (temp.isNumber) {
        serie[i] =
            new Coordinate<>(
                timeWithOffset.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), temp.getDouble());
      } else {
        final int value;
        if (temp.getBoolean()) {
          value = 20;
        } else {
          value = 1;
        }
        serie[i] =
            new Coordinate<>(
                timeWithOffset.minusNanos(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                tempValue);
        i++;
        serie[i] =
            new Coordinate<>(timeWithOffset.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), value);
        tempValue = value;
      }
      i++;
    }
    return serie;
  }

  public ArrayList<VariableList> refreshVariables() {
    return mongoDBController.refreshVariables();
  }

  public void saveVariablesList(final ArrayList<Charts.VariableList> list) {
    mongoDBController.dropCollection("chart_variable_list");
    list.forEach(
        item -> {
          mongoDBController.saveNewEntry("chart_variable_list", item);
        });
  }

  public static class Data {
    private final LocalDateTime timeStamp;
    private final String value;
    private final boolean isNumber;

    public Data(final LocalDateTime timeStamp, final String value) throws Exception {
      this.timeStamp = timeStamp;
      this.value = value;
      if (tryParseDouble(value)) {
        isNumber = true;
      } else if (tryParseBoolean(value)) {
        isNumber = false;
      } else {
        throw new Exception("Value not a number and not a boolean");
      }
    }

    public static boolean tryParseDouble(final String value) {
      try {
        Double.parseDouble(value);
        return true;
      } catch (final NumberFormatException e) {
        return false;
      }
    }

    public static boolean tryParseBoolean(final String value) {
      if (value.equals("true") || (value.equals("false"))) {
        return true;
      } else {
        return false;
      }
    }

    public LocalDateTime getTimeStamp() {
      return timeStamp;
    }

    public double getDouble() throws Exception {
      if (isNumber) {
        return Double.parseDouble(value);
      } else {
        throw new Exception("Value not a Double format");
      }
    }

    public boolean getBoolean() throws Exception {
      if (!isNumber) {
        return Boolean.parseBoolean(value);
      } else {
        throw new Exception("Value not a Boolean format");
      }
    }

    public boolean isNumber() {
      return isNumber;
    }
  }

  public static class VariableList {
    private final String variableName;
    private boolean enabled;

    public VariableList(final String variableName, final boolean enabled) {
      this.variableName = variableName;
      this.enabled = enabled;
    }

    public String getVariableName() {
      return variableName;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(final boolean enabled) {
      this.enabled = enabled;
    }
  }
}
