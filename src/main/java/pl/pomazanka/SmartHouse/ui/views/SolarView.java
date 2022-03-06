package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_SolarPanels;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Solar")
@Route(value = "Solar", layout = MainLayout.class)
public class SolarView extends View {

  @Autowired Module_SolarPanels module_solarPanels;

  // Update thread
  Thread thread;

  // Objects
  Header header;
  Section[] section = new Section[2];
  Info[][][] info = new Info[1][2][4];
  Button autoconsumptionButton;
  NumberField powerEnableLimit;
  NumberField powerResetLimit;
  NumberField reqHeatTempCO;

  public SolarView(final Module_SolarPanels module_solarPanels) {
    this.module_solarPanels = module_solarPanels;

    // Header
    header = new Header(module_solarPanels, "solar.svg");
    header.setLastUpdate(module_solarPanels.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_solarPanels.getDiagnosticLastUpdate());

    // Sections
    section[0] = new Section();
    section[1] = new Section();

    // Create tile for sections
    // Section 0
    section[0].createTile("solar.svg", "Solar");
    section[1].createTile("settings.svg", "Ustawienia");

    // Create sections info/buttons/number fields
    createInfoSection0();
    createInfoSection1();

    section[0].getTileDetailsContainer(0).add(info[0][0][0].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][1].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][2].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][3].getSource());

    section[1].getTileDetailsContainer(0).add(autoconsumptionButton.getSource());
    section[1].getTileDetailsContainer(0).add(powerEnableLimit.getSource());
    section[1].getTileDetailsContainer(0).add(powerResetLimit.getSource());
    section[1].getTileDetailsContainer(0).add(reqHeatTempCO.getSource());

    final Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    section[1]
        .getSection()
        .addClickListener(
            event -> {
              if (!isUserLoggedIn()) {
                notification.open();
              }
            });
    section[1].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
    add(header.getHeader(), section[0].getSection(), section[1].getSection());
  }

  private void createInfoSection0() {
    info[0][0][0] =
        new Info("Obecnie", "W", false, false, module_solarPanels.getWebdata_now_p(), 0, 0, 0);
    info[0][0][1] =
        new Info("Dziś", "kWh", false, false, module_solarPanels.getWebdata_today_e(), 0, 0, 0);
    info[0][0][2] =
        new Info("Razem", "kWh", false, false, module_solarPanels.getWebdata_total_e(), 0, 0, 0);
    info[0][0][3] =
        new Info("Alarm: " + module_solarPanels.getWebdata_alarm(), "", false, false, 0, 0, 0, 0);
  }

  private void createInfoSection1() {
    // Status
    autoconsumptionButton =
        new Button("Auto-konsumpcja", false, module_solarPanels.isAutoConsumption());
    autoconsumptionButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_solarPanels.autoConsumption();
            });
    powerEnableLimit =
        new NumberField(
            "moc załączenia [W]", module_solarPanels.getPowerEnableLimit(), -2000, 5000, 100);
    powerEnableLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setPowerEnableLimit((int) Math.round(valueChangeEvent.getValue()));
            });
    powerResetLimit =
        new NumberField(
            "moc wyłączenia [W]", module_solarPanels.getPowerResetLimit(), -2000, 4000, 100);
    powerResetLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setPowerResetLimit((int) Math.round(valueChangeEvent.getValue()));
            });
    reqHeatTempCO = new NumberField("CO [°C]", module_solarPanels.getReqHeatTempCO(), 35, 55, 0.5);
    reqHeatTempCO
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setReqHeatTempCO(valueChangeEvent.getValue());
            });
  }

  @Override
  void update() {
    // Header
    header.setLastUpdate(module_solarPanels.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_solarPanels.getDiagnosticLastUpdate());
    autoconsumptionButton.setButtonColor(
        module_solarPanels.isAutoConsumption(), module_solarPanels.isAutoConsumption());
    info[0][0][0].setValue(module_solarPanels.getWebdata_now_p());
    info[0][0][1].setValue(module_solarPanels.getWebdata_today_e());
    info[0][0][2].setValue(module_solarPanels.getWebdata_total_e());
    info[0][0][3].setValue(module_solarPanels.getWebdata_alarm());

    module_solarPanels.isAllUpToDate();
  }
}
