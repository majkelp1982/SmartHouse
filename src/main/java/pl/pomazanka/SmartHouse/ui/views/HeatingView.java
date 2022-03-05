package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Ogrzewanie")
@Route(value = "Ogrzewanie", layout = MainLayout.class)
public class HeatingView extends View {

  @Autowired Module_Heating module_heating;

  // Update thread
  Thread thread;

  // Objects
  Header header;
  Section[] section = new Section[3];
  Info[][][] info = new Info[3][4][4];
  Button cheapTariffOnly;
  Button heatingActivated;
  Button waterSuperHeat;
  NumberField reqTempBufferCO;
  NumberField reqTempBufferCWU;
  NumberField heatPumpAlarmTemp;

  public HeatingView(final Module_Heating module_heating) {
    this.module_heating = module_heating;

    // Header
    header = new Header(module_heating, "thermometer.svg");
    header.setLastUpdate(module_heating.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_heating.getDiagnosticLastUpdate());

    // Sections
    section[0] = new Section();
    section[1] = new Section();
    section[2] = new Section();

    // Create tile for sections
    // Section 0
    section[0].createTile("cross.svg", "Główne");
    section[0].createTile("thermometer.svg", "Bufor CO");
    section[0].createTile("CWU.svg", "Bufor CWU");
    section[0].createTile("water-distribution.svg", "Podłogówka");
    // Section 1
    section[1].createTile("status.svg", "Status");
    section[1].createTile("piston.svg", "Pompy");
    section[1].createTile("heat_circuit.svg", "Strefy");
    section[1].createTile("heat_circuit.svg", "Strefy");

    // Section 2 -> SETTINGS!!! SECURE
    section[2].createTile("settings.svg", "Tryb");
    section[2].createTile("settings.svg", "Ustawienia");

    // Create sections info/buttons/number fields
    createInfoSection0();
    createInfoSection1();
    createInfoSection2();

    // Add components to details containers
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        for (int k = 0; k < 4; k++) {
          if (info[i][j][k] != null) {
            section[i].getTileDetailsContainer(j).add(info[i][j][k].getSource());
          }
        }
      }
    }
    section[2]
        .getTileDetailsContainer(0)
        .add(cheapTariffOnly.getSource(), heatingActivated.getSource(), waterSuperHeat.getSource());
    section[2]
        .getTileDetailsContainer(1)
        .add(
            reqTempBufferCO.getSource(),
            reqTempBufferCWU.getSource(),
            heatPumpAlarmTemp.getSource());

    //  <--!!!settings disabled when user not sign in !!!-->
    section[2].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
    section[2].getTileDetailsContainer(1).setEnabled(isUserLoggedIn());

    // Notification if user doesn't logged
    final Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    section[2]
        .getSection()
        .addClickListener(
            event -> {
              if (!isUserLoggedIn()) {
                notification.open();
              }
            });

    add(
        header.getHeader(),
        section[0].getSection(),
        section[1].getSection(),
        section[2].getSection());
  }

  private void createInfoSection0() {
    // Create info's for [section][tileNo][intoNo]
    // Main
    info[0][0][0] =
        new Info(
            "źródła",
            "°C",
            false,
            false,
            module_heating.gettSupply(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    info[0][0][1] =
        new Info(
            "powrót",
            "°C",
            false,
            false,
            module_heating.gettReturn(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    info[0][0][2] =
        new Info(
            "kominek",
            "°C",
            false,
            false,
            module_heating.gettFirePlace(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    info[0][0][3] =
        new Info(
            "kolektor",
            "°C",
            false,
            false,
            module_heating.gettGroundSource(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    // Buffer CO
    info[0][1][0] =
        new Info(
            "góra",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettBufferCOHigh(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    info[0][1][1] =
        new Info(
            "środek",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettBufferCOMid(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    info[0][1][2] =
        new Info(
            "dół",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettBufferCOLow(),
            module_heating.getReqTempBufferCO(),
            3,
            5);
    // Section Tile 2 Buffer CWU
    info[0][2][0] =
        new Info(
            "góra",
            "°C",
            true,
            false,
            module_heating.gettBufferCWUHigh(),
            module_heating.getReqTempBufferCWU(),
            3,
            5);
    info[0][2][1] =
        new Info(
            "środek",
            "°C",
            true,
            false,
            module_heating.gettBufferCWUMid(),
            module_heating.getReqTempBufferCWU(),
            3,
            5);
    info[0][2][2] =
        new Info(
            "dół",
            "°C",
            true,
            false,
            module_heating.gettBufferCWULow(),
            module_heating.getReqTempBufferCWU(),
            3,
            5);
    // Floor heating water distribution
    info[0][3][0] =
        new Info(
            "rozdzielacz",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettManifold(),
            34,
            3,
            5);
    info[0][3][1] =
        new Info(
            "powrót parter",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettReturnGroundFloor(),
            30,
            3,
            5);
    info[0][3][2] =
        new Info(
            "powrót piętro",
            "°C",
            module_heating.isHeatingActivated(),
            false,
            module_heating.gettReturnLoft(),
            30,
            3,
            5);
  }

  private void createInfoSection1() {
    String temp;
    switch (module_heating.getHeatSourceActive()) {
      case 1:
        temp = "Pompa PC";
        break;
      case 2:
        temp = "Bufor CO";
        break;
      case 3:
        temp = "Kominek";
        break;
      default:
        temp = "Błąd";
        break;
    }
    info[1][0][0] = new Info("źródło ciepła", temp);

    final double temp1 =
        (float) module_heating.getValve_bypass() * 2.5; // scale from 1-40units to 1-100%
    temp = temp1 + "%";
    info[1][0][1] = new Info("bypass", temp);

    switch (module_heating.getValve_3way()) {
      case 1:
        temp = "CO";
        break;
      case 2:
        temp = "CWU";
        break;
      default:
        temp = "Błąd";
        break;
    }
    info[1][0][2] = new Info("kierunek", temp);

    // Section Tile 1 Pumps
    info[1][1][0] = new Info("obieg dom", true, module_heating.isPump_InHouse());
    info[1][1][1] = new Info("obieg ziemia", true, module_heating.isPump_UnderGround());
    info[1][1][2] = new Info("pompa ciepła", true, module_heating.isReqHeatingPumpOn());

    // Section Tile 2 Zones
    final boolean[] zone = module_heating.getZone();

    info[1][2][0] = new Info("salon", true, zone[0]);
    info[1][2][1] = new Info("pralnia", true, zone[1]);
    info[1][2][2] = new Info("łaź.dół", true, zone[2]);

    info[1][3][0] = new Info("rodzice", true, zone[3]);
    info[1][3][1] = new Info("Natalia", true, zone[4]);
    info[1][3][2] = new Info("Karolina", true, zone[5]);
    info[1][3][3] = new Info("łaź.góra", true, zone[6]);
  }

  private void createInfoSection2() {
    // Click Listeners
    cheapTariffOnly = new Button("II taryfa", true, module_heating.isCheapTariffOnly());
    heatingActivated = new Button("ogrzewanie", true, module_heating.isHeatingActivated());
    waterSuperHeat = new Button("gorąca woda", true, module_heating.isWaterSuperheat());

    cheapTariffOnly
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_heating.setNVCheapTariffOnly(!module_heating.isCheapTariffOnly());
              setPendingColor(cheapTariffOnly.getSource());
              module_heating.setReqUpdateValues(true);
            });
    heatingActivated
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_heating.setNVHeatingActivated(!module_heating.isHeatingActivated());
              setPendingColor(heatingActivated.getSource());
              module_heating.setReqUpdateValues(true);
            });
    waterSuperHeat
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_heating.setNVWaterSuperheat(!module_heating.isWaterSuperheat());
              setPendingColor(waterSuperHeat.getSource());
              module_heating.setReqUpdateValues(true);
            });

    // Section Tile 1 required temperatures
    reqTempBufferCO = new NumberField("CO [°C]", module_heating.getReqTempBufferCO(), 35, 45, 0.5);
    reqTempBufferCWU =
        new NumberField("CWU [°C]", module_heating.getReqTempBufferCWU(), 40, 55, 0.5);
    heatPumpAlarmTemp =
        new NumberField("Temp. max [°C]", module_heating.getHeatPumpAlarmTemp(), 52, 60, 1.0);

    // Click Listeners
    reqTempBufferCO
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_heating.setNVReqTempBufferCO(valueChangeEvent.getValue());
              setPendingColor(reqTempBufferCO.getSource());
              module_heating.setReqUpdateValues(true);
            });
    reqTempBufferCWU
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_heating.setNVReqTempBufferCWU(valueChangeEvent.getValue());
              setPendingColor(reqTempBufferCWU.getSource());
              module_heating.setReqUpdateValues(true);
            });
    heatPumpAlarmTemp
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_heating.setNVHeatPumpAlarmTemp((int) Math.rint(valueChangeEvent.getValue()));
              setPendingColor(heatPumpAlarmTemp.getSource());
              module_heating.setReqUpdateValues(true);
            });
  }

  @Override
  void update() {
    // Header
    header.setLastUpdate(module_heating.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_heating.getDiagnosticLastUpdate());

    // Settings
    cheapTariffOnly.setButtonColor(
        module_heating.isCheapTariffOnly(), module_heating.isNVCheapTariffOnly());
    heatingActivated.setButtonColor(
        module_heating.isHeatingActivated(), module_heating.isNVHeatingActivated());
    waterSuperHeat.setButtonColor(
        module_heating.isWaterSuperheat(), module_heating.isNVWaterSuperheat());
    reqTempBufferCO.setNumberField(
        module_heating.getReqTempBufferCO(), module_heating.getNVReqTempBufferCO());
    reqTempBufferCWU.setNumberField(
        module_heating.getReqTempBufferCWU(), module_heating.getNVReqTempBufferCWU());
    heatPumpAlarmTemp.setNumberField(
        module_heating.getHeatPumpAlarmTemp(), module_heating.getNVHeatPumpAlarmTemp());

    // Info's
    info[0][0][0].setValue(module_heating.gettSupply());
    info[0][0][1].setValue(module_heating.gettReturn());
    info[0][0][2].setValue(module_heating.gettFirePlace());
    info[0][0][3].setValue(module_heating.gettGroundSource());
    // Buffer CO
    info[0][1][0].setValue(module_heating.gettBufferCOHigh());
    info[0][1][1].setValue(module_heating.gettBufferCOMid());
    info[0][1][2].setValue(module_heating.gettBufferCOLow());
    // Section Tile 2 Buffer CWU
    info[0][2][0].setValue(module_heating.gettBufferCWUHigh());
    info[0][2][1].setValue(module_heating.gettBufferCWUMid());
    info[0][2][2].setValue(module_heating.gettBufferCWULow());
    // Floor heating water distribution
    info[0][3][0].setValue(module_heating.gettManifold());
    info[0][3][1].setValue(module_heating.gettReturnGroundFloor());
    info[0][3][2].setValue(module_heating.gettReturnLoft());

    String temp;
    switch (module_heating.getHeatSourceActive()) {
      case 1:
        temp = "Pompa PC";
        break;
      case 2:
        temp = "Bufor CO";
        break;
      case 3:
        temp = "Kominek";
        break;
      default:
        temp = "Błąd";
        break;
    }
    info[1][0][0].setValue(temp);

    final double temp1 =
        (float) module_heating.getValve_bypass() * 2.5; // scale from 1-40units to 1-100%
    temp = temp1 + "%";
    info[1][0][1].setValue(temp);

    switch (module_heating.getValve_3way()) {
      case 1:
        temp = "CO";
        break;
      case 2:
        temp = "CWU";
        break;
      default:
        temp = "Błąd";
        break;
    }
    info[1][0][2].setValue(temp);

    // Section Tile 1 Pumps
    info[1][1][0].setValue(module_heating.isPump_InHouse());
    info[1][1][1].setValue(module_heating.isPump_UnderGround());
    info[1][1][2].setValue(module_heating.isReqHeatingPumpOn());

    // Section Tile 2 Zones
    final boolean[] zone = module_heating.getZone();

    info[1][2][0].setValue(zone[0]);
    info[1][2][1].setValue(zone[1]);
    info[1][2][2].setValue(zone[2]);

    info[1][3][0].setValue(zone[3]);
    info[1][3][1].setValue(zone[4]);
    info[1][3][2].setValue(zone[5]);
    info[1][3][3].setValue(zone[6]);
  }
}
