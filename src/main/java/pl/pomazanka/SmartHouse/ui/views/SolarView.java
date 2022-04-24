package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_SolarPanels;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.time.Duration;
import java.time.LocalDateTime;

@PageTitle("Smart House | Solar")
@Route(value = "Solar", layout = MainLayout.class)
public class SolarView extends View {
  private static final int SECTIONS = 2;

  @Autowired Module_SolarPanels module_solarPanels;

  // Update thread
  Thread thread;

  // Objects
  Header header;
  Section[] section = new Section[SECTIONS];
  Info[][][] info = new Info[1][2][4];
  Button forceCOBufferButton;
  NumberField forceCOBufferEnableLimit;
  NumberField forceCOBufferResetLimit;
  NumberField reqHeatTempCO;
  Button forceWaterSuperHeatButton;
  NumberField forceWaterSuperHeatEnableLimit;
  NumberField forceWaterSuperHeatResetLimit;

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
    section[0].createTile("status.svg", "Solar");
    section[1].createTile("settings.svg", "Bufor CO");
    section[1].createTile("settings.svg", "Gorąca woda");

    // Create sections info/buttons/number fields
    createInfoSection0();
    createInfoSection1();

    section[0].getTileDetailsContainer(0).add(info[0][0][0].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][1].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][2].getSource());
    section[0].getTileDetailsContainer(0).add(info[0][0][3].getSource());
    section[0].getTileDetailsContainer(1).add(info[0][1][0].getSource());
    section[0].getTileDetailsContainer(1).add(info[0][1][1].getSource());

    section[1].getTileDetailsContainer(0).add(forceCOBufferButton.getSource());
    section[1].getTileDetailsContainer(0).add(forceCOBufferEnableLimit.getSource());
    section[1].getTileDetailsContainer(0).add(forceCOBufferResetLimit.getSource());
    section[1].getTileDetailsContainer(0).add(reqHeatTempCO.getSource());

    section[1].getTileDetailsContainer(1).add(forceWaterSuperHeatButton.getSource());
    section[1].getTileDetailsContainer(1).add(forceWaterSuperHeatEnableLimit.getSource());
    section[1].getTileDetailsContainer(1).add(forceWaterSuperHeatResetLimit.getSource());

    final Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    for (int i = 0; i < SECTIONS; i++) {
      section[i].getSection().setEnabled(isUserLoggedIn());
    }
    addClickListener(
        event -> {
          if (!isUserLoggedIn()) {
            notification.open();
          }
        });
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

    info[0][1][0] = new Info("Konsumpcja", true, module_solarPanels.isForceCOBufferActive());
    info[0][1][1] =
        new Info(
            "Opóźnienie",
            "s",
            false,
            false,
            Duration.between(LocalDateTime.now(), module_solarPanels.getStateChangeTime())
                .getSeconds(),
            0,
            0,
            0);
  }

  private void createInfoSection1() {
    // Status
    forceCOBufferButton = new Button("Zezwól", false, module_solarPanels.isForceCOBufferEnabled());
    forceCOBufferButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_solarPanels.forceCOBuffer();
            });
    forceCOBufferEnableLimit =
        new NumberField(
            "moc załączenia [W]",
            module_solarPanels.getForceCOBufferEnableLimit(),
            -2000,
            5000,
            100);
    forceCOBufferEnableLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setForceCOBufferEnableLimit(
                  (int) Math.round(valueChangeEvent.getValue()));
            });
    forceCOBufferResetLimit =
        new NumberField(
            "moc wyłączenia [W]",
            module_solarPanels.getForceCOBufferResetLimit(),
            -2000,
            4000,
            100);
    forceCOBufferResetLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setForceCOBufferResetLimit(
                  (int) Math.round(valueChangeEvent.getValue()));
            });
    reqHeatTempCO = new NumberField("CO [°C]", module_solarPanels.getReqHeatTempCO(), 35, 55, 0.5);
    reqHeatTempCO
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setReqHeatTempCO(valueChangeEvent.getValue());
            });

    forceWaterSuperHeatButton =
        new Button("Zezwól", false, module_solarPanels.isForceWaterSuperHeatEnabled());
    forceWaterSuperHeatButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_solarPanels.forceWaterSuperHeat();
            });
    forceWaterSuperHeatEnableLimit =
        new NumberField(
            "moc załączenia [W]",
            module_solarPanels.getForceWaterSuperHeatEnableLimit(),
            -2000,
            5000,
            100);
    forceWaterSuperHeatEnableLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setForceWaterSuperHeatEnableLimit(
                  (int) Math.round(valueChangeEvent.getValue()));
            });
    forceWaterSuperHeatResetLimit =
        new NumberField(
            "moc wyłączenia [W]",
            module_solarPanels.getForceWaterSuperHeatResetLimit(),
            -2000,
            4000,
            100);
    forceWaterSuperHeatResetLimit
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_solarPanels.setForceWaterSuperHeatResetLimit(
                  (int) Math.round(valueChangeEvent.getValue()));
            });
  }

  @Override
  void update() {
    // Header
    header.setLastUpdate(module_solarPanels.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_solarPanels.getDiagnosticLastUpdate());
    forceCOBufferButton.setButtonColor(
        module_solarPanels.isForceCOBufferEnabled(), module_solarPanels.isForceCOBufferEnabled());
    forceWaterSuperHeatButton.setButtonColor(
        module_solarPanels.isForceWaterSuperHeatEnabled(),
        module_solarPanels.isForceWaterSuperHeatEnabled());
    info[0][0][0].setValue(module_solarPanels.getWebdata_now_p());
    info[0][0][1].setValue(module_solarPanels.getWebdata_today_e());
    info[0][0][2].setValue(module_solarPanels.getWebdata_total_e());
    info[0][0][3].setValue(module_solarPanels.getWebdata_alarm());

    info[0][1][0].setValue(module_solarPanels.isForceCOBufferActive());
    info[0][1][1].setValue(
        Duration.between(LocalDateTime.now(), module_solarPanels.getStateChangeTime())
            .getSeconds());

    module_solarPanels.isAllUpToDate();
  }
}
