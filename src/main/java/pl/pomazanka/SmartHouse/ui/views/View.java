package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public abstract class View extends VerticalLayout {

  // static variables
  // ***************************************
  public static final String COLOR_WARNING = "yellow";
  public static final String COLOR_ALARM = "red";
  public static final String COLOR_OK = "green";
  public static final String COLOR_ON = "green";
  public static final String COLOR_NORMAL = "white";
  public static final String COLOR_OFF = "grey";
  public static final String COLOR_NV = "orange";

  // Last telegram updates info
  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  // Update thread
  Thread thread;

  public static boolean isUserLoggedIn() {
    boolean status = false;
    final HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    final String ipAddress = request.getRemoteAddr();
    if (ipAddress != null) {
      if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
        status = true;
      }
      if (ipAddress.contains("192") && ipAddress.contains("168")) {
        status = true;
      }
    }
    if (!status) {
      final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      status =
          authentication != null
              && !(authentication instanceof AnonymousAuthenticationToken) //
              && authentication.isAuthenticated();
    }
    return status;
  }

  public LocalDateTime getCurrentDate() {
    final LocalDateTime now = LocalDateTime.now();
    return now;
  }

  private void setComponentColor(
      final HtmlContainer component,
      final boolean colorEnabled,
      final boolean exceedAlarm,
      final Number isValue,
      final Number expectedValue,
      final Number warningLimit,
      final Number alarmLimit) {
    if (colorEnabled) {
      if (Math.abs(isValue.doubleValue() - expectedValue.doubleValue())
          > alarmLimit.doubleValue()) {
        component.getStyle().set("color", COLOR_ALARM);
      } else if (Math.abs(isValue.doubleValue() - expectedValue.doubleValue())
          > warningLimit.doubleValue()) {
        component.getStyle().set("color", COLOR_WARNING);
      } else {
        component.getStyle().set("color", COLOR_OK);
      }
      if (!exceedAlarm) {
        if ((!component.getStyle().get("color").equals(COLOR_OK))
            && (isValue.doubleValue() > expectedValue.doubleValue())) {
          component.getStyle().set("color", COLOR_OK);
        }
      }
    } else {
      component.getStyle().set("color", COLOR_NORMAL);
    }
  }

  private void setComponentColor(
      final HtmlContainer component, final boolean colorEnabled, final boolean status) {
    if (colorEnabled) {
      if (status) {
        component.getStyle().set("color", COLOR_ON);
      } else {
        component.getStyle().set("color", COLOR_OFF);
      }
    } else {
      component.getStyle().set("color", COLOR_NORMAL);
    }
  }

  private void setActualColor(final HasStyle hasStyle, final boolean status) {
    if (status) {
      hasStyle.getStyle().set("color", COLOR_ON);
    } else {
      hasStyle.getStyle().set("color", COLOR_OFF);
    }
  }

  public void setPendingColor(final HasStyle hasStyle) {
    hasStyle.getStyle().set("color", COLOR_NV);
  }

  abstract void update();

  @Override
  protected void onAttach(final AttachEvent attachEvent) {
    // Start thread when view active
    thread = new View.FeederThread(attachEvent.getUI(), this);
    thread.start(); // On Attach update all components
  }

  @Override
  protected void onDetach(final DetachEvent attachEvent) {
    thread.stop();
    thread = null;
  }

  private static class FeederThread extends Thread {
    private final UI ui;
    private final View view;

    public FeederThread(final UI ui, final View view) {
      this.ui = ui;
      this.view = view;
    }

    @Override
    public void run() {
      while (true) {
        try {
          ui.access(view::update);
          Thread.sleep(5000);
        } catch (final Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public class Header {
    private final HorizontalLayout header;
    private Label lastUpdateLabel;
    private Label diagnoseUpdateLabel;

    // Header for Modules
    public Header(final Module module, final String imageSrc) {
      header = new HorizontalLayout();

      // About module type
      final Image image = new Image(imageSrc, imageSrc);
      image.setHeight("80px");
      final Label moduleTyp = new Label(module.getModuleName());
      moduleTyp.getStyle().set("font-size", "30px");

      lastUpdateLabel = new Label();
      diagnoseUpdateLabel = new Label();
      final VerticalLayout info = new VerticalLayout();
      info.setAlignItems(FlexComponent.Alignment.CENTER);
      info.add(lastUpdateLabel, diagnoseUpdateLabel);
      info.setWidth("800px");
      info.setSizeFull();

      header.addClassName("module");
      header.setMinWidth("800px");
      header.setSizeFull();
      header.setHeight("80px");
      header.setAlignItems(FlexComponent.Alignment.CENTER);
      header.add(image, moduleTyp, info);
    }

    // Header for diagnostic view
    public Header(final Diagnostic diagnostic, final String imageSrc) {
      header = new HorizontalLayout();

      // About module type
      final Image image = new Image(imageSrc, imageSrc);
      image.setHeight("80px");
      final Label moduleTyp = new Label(diagnostic.getModuleName());
      moduleTyp.getStyle().set("font-size", "30px");

      diagnoseUpdateLabel = new Label();
      final VerticalLayout info = new VerticalLayout();
      info.setAlignItems(FlexComponent.Alignment.CENTER);
      info.add(diagnoseUpdateLabel);
      info.setWidth("800px");
      info.setSizeFull();

      header.addClassName("module");
      header.setMinWidth("800px");
      header.setSizeFull();
      header.setHeight("80px");
      header.setAlignItems(FlexComponent.Alignment.CENTER);
      header.add(image, moduleTyp, info);
    }

    // Header only pic and title
    public Header(final String title, final String imageSrc) {
      header = new HorizontalLayout();

      // About module type
      final Image image = new Image(imageSrc, imageSrc);
      image.setHeight("80px");
      final Label moduleTyp = new Label(title);
      moduleTyp.getStyle().set("font-size", "30px");

      header.addClassName("module");
      header.setMinWidth("800px");
      header.setSizeFull();
      header.setHeight("80px");
      header.setAlignItems(FlexComponent.Alignment.CENTER);
      header.add(image, moduleTyp);
    }

    public HorizontalLayout getHeader() {
      return header;
    }

    public void setLastUpdate(final LocalDateTime lastUpdate) {
      lastUpdateLabel.setText(
          "Update : " + lastUpdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      setComponentColor(
          lastUpdateLabel,
          true,
          true,
          lastUpdate.getLong(ChronoField.MILLI_OF_DAY),
          getCurrentDate().getLong(ChronoField.MILLI_OF_DAY),
          60000,
          120000);
    }

    public void setDiagnoseUpdate(final LocalDateTime diagnoseUpdate) {
      diagnoseUpdateLabel.setText(
          "Diagnose : "
              + diagnoseUpdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      setComponentColor(
          diagnoseUpdateLabel,
          true,
          true,
          diagnoseUpdate.getLong(ChronoField.MILLI_OF_DAY),
          getCurrentDate().getLong(ChronoField.MILLI_OF_DAY),
          60000,
          120000);
    }
  }

  public class Section {
    private final HorizontalLayout section;
    private final Tile[] tile = new Tile[10];

    public Section() {
      section = new HorizontalLayout();
    }

    public void createTile(final String imageSrc, final String tittle) {
      int tileNo = 0;
      while (tile[tileNo] != null) {
        tileNo++;
      }
      tile[tileNo] = new Tile(imageSrc, tittle);

      section.add(tile[tileNo].getTile());
    }

    public VerticalLayout getTileDetailsContainer(final int tileNo) {
      return tile[tileNo].getTileDetailsContainer();
    }

    public HorizontalLayout getSection() {
      return section;
    }
  }

  private class Tile {
    HorizontalLayout tile;
    VerticalLayout detailsContainer;

    public Tile(final String imageSrc, final String tittle) {
      tile = new HorizontalLayout();
      tile.setAlignItems(FlexComponent.Alignment.CENTER);
      tile.addClassName("module");

      final Image image = new Image(imageSrc, imageSrc);
      image.setHeight("50px");
      final Label tittleLabel = new Label(tittle);
      detailsContainer = new VerticalLayout();
      tile.add(tittleLabel, image, detailsContainer);
    }

    private HorizontalLayout getTile() {
      return tile;
    }

    public VerticalLayout getTileDetailsContainer() {
      return detailsContainer;
    }
  }

  public class Info {
    private final HorizontalLayout info;
    private final Label nameLabel;
    private final boolean colorEnabled;
    private Label valueLabel;
    private String unit;
    private boolean exceedAlarm;
    private Number expectedValue;
    private Number warningLimit;
    private Number alarmLimit;

    public Info(
        final String name,
        final String unit,
        final boolean colorEnabled,
        final boolean exceedAlarm,
        final Number isValue,
        final Number expectedValue,
        final Number warningLimit,
        final Number alarmLimit) {
      this.unit = unit;
      this.colorEnabled = colorEnabled;
      this.exceedAlarm = exceedAlarm;
      this.expectedValue = expectedValue;
      this.warningLimit = warningLimit;
      this.alarmLimit = alarmLimit;

      info = new HorizontalLayout();
      nameLabel = new Label("" + name);
      nameLabel.getStyle().set("color", COLOR_NORMAL);
      valueLabel = new Label();
      setValue(isValue);
      info.add(nameLabel, valueLabel);
    }

    public Info(final String name, final String value) {
      info = new HorizontalLayout();
      nameLabel = new Label("" + name);
      this.colorEnabled = false;
      nameLabel.getStyle().set("color", COLOR_NORMAL);
      valueLabel = new Label();
      nameLabel.getStyle().set("color", COLOR_NORMAL);
      setValue(value);
      info.add(nameLabel, valueLabel);
    }

    public Info(final HorizontalLayout valueInfo) {
      info = valueInfo;
      nameLabel = (Label) info.getComponentAt(0);
      this.colorEnabled = false;
      valueLabel = (Label) info.getComponentAt(1);
    }

    public Info(final String name, final boolean colorEnabled, final boolean status) {
      info = new HorizontalLayout();
      this.colorEnabled = colorEnabled;
      nameLabel = new Label(name);
      setValue(status);
      info.add(nameLabel);
    }

    public void setValue(final Number isValue) {
      if (unit == null) {
        try {
          unit =
              valueLabel
                  .getText()
                  .substring(
                      valueLabel.getText().indexOf("[") + 1, valueLabel.getText().indexOf("]"));
        } catch (final Exception e) {
        }
      }
      valueLabel.setText(" " + isValue + "[" + unit + "]");
      setComponentColor(
          valueLabel, colorEnabled, exceedAlarm, isValue, expectedValue, warningLimit, alarmLimit);
    }

    public void setValue(final String isValue) {
      valueLabel.setText(" " + isValue);
      setComponentColor(nameLabel, colorEnabled, true);
    }

    public void setValue(final boolean status) {
      setComponentColor(nameLabel, colorEnabled, status);
    }

    public HorizontalLayout getSource() {
      return info;
    }

    public Label getNameLabel() {
      return nameLabel;
    }
  }

  public class Button {
    com.vaadin.flow.component.button.Button button;

    public Button(final String name, final boolean colorEnabled, final boolean status) {
      button = new com.vaadin.flow.component.button.Button();
      button.setText(name);
      setButtonColor(status, status);
    }

    public Button(final com.vaadin.flow.component.button.Button button) {
      this.button = button;
    }

    public void setButtonColor(final boolean isStatus, final boolean expectedStatus) {
      setActualColor(button, isStatus);
      if (isStatus == expectedStatus) {
        setActualColor(button, isStatus);
      } else {
        setPendingColor(button);
      }
    }

    public com.vaadin.flow.component.button.Button getSource() {
      return button;
    }
  }

  public class NumberField {
    com.vaadin.flow.component.textfield.NumberField numberField;

    public NumberField(
        final String name,
        final double initValue,
        final double min,
        final double max,
        final double step) {
      numberField = new com.vaadin.flow.component.textfield.NumberField(name);
      numberField.setSizeFull();
      numberField.setHasControls(true);
      numberField.setValue(initValue);
      numberField.setStep(step);
      numberField.setMin(min);
      numberField.setMax(max);
    }

    public NumberField(final com.vaadin.flow.component.textfield.NumberField numberField) {
      this.numberField = numberField;
    }

    public void setNumberField(final double isValue, final double expectedValue) {
      if (isValue == expectedValue) {
        setActualColor(numberField, true);
        numberField.setValue(isValue);
      } else {
        setPendingColor(numberField);
        numberField.setValue(expectedValue);
      }
    }

    public com.vaadin.flow.component.textfield.NumberField getSource() {
      return numberField;
    }
  }
}
