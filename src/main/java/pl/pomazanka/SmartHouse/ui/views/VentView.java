package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.BME280;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.Fan;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.VentZones;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.Zone;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Smart House | Wentylacja")
@Route(value = "", layout = MainLayout.class)
public class VentView extends View {
  private static final int SECTIONS = 5;

  @Autowired Module_Vent module_vent;

  // Update thread
  Thread thread;

  // Objects
  Header header;
  Section[] section = new Section[SECTIONS];
  Info[][][] info = new Info[4][7][4];

  NumberField normalDelayTime;
  NumberField humidityTriggerInt;
  NumberField humidityDelayTime;
  NumberField defrostTriggerInt;
  NumberField defrostDelayTime;
  NumberField minTempNumberField;

  Button activeCoolingButton;
  Button activeHeatingButton;
  Button reqLazDolButton;
  Button reqLazGoraButton;
  Button reqKuchniaButton;
  Button reqAutoDiagnosisButton;

  Grid<VentZonesByHour> activeRegGrid;
  Grid<VentZonesByHour> normalModeGrid;

  public VentView(final Module_Vent module_vent) {
    this.module_vent = module_vent;
    // Header
    header = new Header(module_vent, "recu.svg");
    header.setLastUpdate(module_vent.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_vent.getDiagnosticLastUpdate());

    // Sections
    section[0] = new Section();
    section[1] = new Section();
    section[2] = new Section();
    section[3] = new Section();
    section[4] = new Section();

    // Section 0 Tryby
    section[0].createTile("settings.svg", "Tryby");
    section[0].createTile("status.svg", "Status");
    section[0].createTile("fan.svg", "Normalny");
    section[0].createTile("fan.svg", "Wilgoć");
    section[0].createTile("fan.svg", "Odmrażanie");

    // Section 1 Tryby
    section[1].createTile("settings.svg", "Tryby");
    section[1].createTile("settings.svg", "Wymuś");
    section[1].createTile("thermometer.svg", "Wymiennik");
    section[1].createTile("fan.svg", "Klapy nawiew");
    section[1].createTile("fan.svg", "Klapy nawiew");
    section[1].createTile("fan.svg", "Klapy wywiew");
    section[1].createTile("fan.svg", "Klapy wywiew");

    // Section 2 BME i wentylatory
    section[2].createTile("thermometer.svg", "Czerpnia");
    section[2].createTile("thermometer.svg", "Wyrzutnia");
    section[2].createTile("thermometer.svg", "Nawiew");
    section[2].createTile("thermometer.svg", "Wywiew");
    section[2].createTile("fan.svg", "Wentylatory");

    // Section 3 ActiveTempMode
    section[3].createTile("settings.svg", "ActiveTemp Mode");

    // Section 2 NormalMode
    section[4].createTile("settings.svg", "Normal Mode");

    // Create sections info/buttons/number fields
    createInfoSection0();
    createInfoSection1();
    createInfoSection2();
    createInfoSection3();
    createInfoSection4();

    // Section 0
    int index = 0;
    section[index].getTileDetailsContainer(0).add(info[index][0][0].getSource());
    section[index].getTileDetailsContainer(0).add(info[index][0][1].getSource());
    section[index].getTileDetailsContainer(0).add(info[index][0][2].getSource());
    section[index].getTileDetailsContainer(0).add(reqAutoDiagnosisButton.getSource());

    section[index].getTileDetailsContainer(1).add(info[index][1][0].getSource());
    section[index].getTileDetailsContainer(1).add(info[index][1][1].getSource());
    section[index].getTileDetailsContainer(1).add(info[index][1][2].getSource());
    section[index].getTileDetailsContainer(1).add(info[index][1][3].getSource());

    section[index].getTileDetailsContainer(2).add(info[index][2][0].getSource());
    section[index].getTileDetailsContainer(2).add(info[index][2][1].getSource());
    section[index].getTileDetailsContainer(2).add(normalDelayTime.getSource());

    section[index].getTileDetailsContainer(3).add(info[index][3][0].getSource());
    section[index].getTileDetailsContainer(3).add(info[index][3][1].getSource());
    section[index].getTileDetailsContainer(3).add(humidityTriggerInt.getSource());
    section[index].getTileDetailsContainer(3).add(humidityDelayTime.getSource());

    section[index].getTileDetailsContainer(4).add(info[index][4][0].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][1].getSource());
    section[index].getTileDetailsContainer(4).add(defrostTriggerInt.getSource());
    section[index].getTileDetailsContainer(4).add(defrostDelayTime.getSource());

    // Section 1
    index = 1;
    section[index].getTileDetailsContainer(0).add(activeCoolingButton.getSource());
    section[index].getTileDetailsContainer(0).add(activeHeatingButton.getSource());
    section[index].getTileDetailsContainer(0).add(minTempNumberField.getSource());

    section[index].getTileDetailsContainer(1).add(reqLazDolButton.getSource());
    section[index].getTileDetailsContainer(1).add(reqLazGoraButton.getSource());
    section[index].getTileDetailsContainer(1).add(reqKuchniaButton.getSource());
    for (int i = 0; i < 4; i++) {
      section[index].getTileDetailsContainer(2).add(info[index][2][i].getSource());
    }

    section[index].getTileDetailsContainer(3).add(info[index][3][0].getSource());
    section[index].getTileDetailsContainer(3).add(info[index][3][1].getSource());
    section[index].getTileDetailsContainer(3).add(info[index][3][2].getSource());
    section[index].getTileDetailsContainer(3).add(info[index][3][3].getSource());

    section[index].getTileDetailsContainer(4).add(info[index][4][0].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][1].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][2].getSource());

    section[index].getTileDetailsContainer(5).add(info[index][5][0].getSource());
    section[index].getTileDetailsContainer(5).add(info[index][5][1].getSource());
    section[index].getTileDetailsContainer(5).add(info[index][5][2].getSource());
    section[index].getTileDetailsContainer(5).add(info[index][5][3].getSource());

    section[index].getTileDetailsContainer(6).add(info[index][6][0].getSource());
    section[index].getTileDetailsContainer(6).add(info[index][6][1].getSource());
    section[index].getTileDetailsContainer(6).add(info[index][6][2].getSource());
    section[index].getTileDetailsContainer(6).add(info[index][6][3].getSource());

    // Section 2
    index = 2;
    for (int i = 0; i < 4; i++) {
      section[index].getTileDetailsContainer(i).add(info[index][i][0].getSource());
      section[index].getTileDetailsContainer(i).add(info[index][i][1].getSource());
      section[index].getTileDetailsContainer(i).add(info[index][i][2].getSource());
    }
    section[index].getTileDetailsContainer(4).add(info[index][4][0].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][1].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][2].getSource());
    section[index].getTileDetailsContainer(4).add(info[index][4][3].getSource());

    // Section 3
    index = 3;
    section[index].getTileDetailsContainer(0).add(activeRegGrid);
    section[index].getTileDetailsContainer(0).setWidth("690px");
    section[index].getTileDetailsContainer(0).setHeight("960px");
    // Section 4
    index = 4;
    section[index].getTileDetailsContainer(0).add(normalModeGrid);
    section[index].getTileDetailsContainer(0).setWidth("690px");
    section[index].getTileDetailsContainer(0).setHeight("960px");

    // Notification if user doesn't logged
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
    add(
        header.getHeader(),
        section[0].getSection(),
        section[1].getSection(),
        section[2].getSection(),
        section[3].getSection(),
        section[4].getSection());
  }

  private void createInfoSection0() {
    final int index = 0;
    // Tryby
    info[index][0][0] = new Info("normalON", true, module_vent.isNormalOn());
    info[index][0][1] = new Info("humidityAlert", true, module_vent.isHumidityAlert());
    info[index][0][2] = new Info("defrost", true, module_vent.isDefrostActive());
    reqAutoDiagnosisButton =
        new Button("Autodiagnoza", true, (boolean) module_vent.getReqAutoDiagnosis().getIsValue());
    reqAutoDiagnosisButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getReqAutoDiagnosis()
                  .setNewValue(!(boolean) module_vent.getReqAutoDiagnosis().getIsValue());
              setPendingColor(reqAutoDiagnosisButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    // Status
    info[index][1][0] = new Info("ByPass", true, module_vent.isBypassOpen());
    info[index][1][1] = new Info("Pompa", true, module_vent.isCircuitPump());
    info[index][1][2] = new Info("reqColdWater", true, module_vent.isReqPumpColdWater());
    info[index][1][3] = new Info("reqHotWater", true, module_vent.isReqPumpHotWater());

    // NormaOn tryb
    info[index][2][0] =
        new Info("trigger", true, (boolean) module_vent.getNormalMode().getTrigger().getIsValue());
    info[index][2][1] =
        new Info(
            "czas do końca",
            "min",
            false,
            false,
            module_vent.getNormalMode().getTimeLeft(),
            0,
            0,
            0);
    double val = (int) module_vent.getNormalMode().getDelayTime().getIsValue();
    normalDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
    normalDelayTime
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent
                  .getNormalMode()
                  .getDelayTime()
                  .setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(normalDelayTime.getSource());
              module_vent.setReqUpdateValues(true);
            });

    // HumidityAlert tryb
    info[index][3][0] =
        new Info(
            "trigger",
            true,
            (boolean) module_vent.getHumidityAlertMode().getTrigger().getIsValue());
    info[index][3][1] =
        new Info(
            "czas do końca",
            "min",
            false,
            false,
            module_vent.getHumidityAlertMode().getTimeLeft(),
            0,
            0,
            0);
    val = (int) module_vent.getHumidityAlertMode().getTriggerInt().getIsValue();
    humidityTriggerInt = new NumberField("próg załączenia [%]", val, 20, 100, 1);
    humidityTriggerInt
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent
                  .getHumidityAlertMode()
                  .getTriggerInt()
                  .setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(humidityTriggerInt.getSource());
              module_vent.setReqUpdateValues(true);
            });
    val = (int) module_vent.getHumidityAlertMode().getDelayTime().getIsValue();
    humidityDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
    humidityDelayTime
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent
                  .getHumidityAlertMode()
                  .getDelayTime()
                  .setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(humidityDelayTime.getSource());
              module_vent.setReqUpdateValues(true);
            });

    // Defrost tryb
    info[index][4][0] =
        new Info("trigger", true, (boolean) module_vent.getDefrostMode().getTrigger().getIsValue());
    info[index][4][1] =
        new Info(
            "czas do końca",
            "min",
            false,
            false,
            module_vent.getDefrostMode().getTimeLeft(),
            0,
            0,
            0);
    val = (int) module_vent.getDefrostMode().getTriggerInt().getIsValue();
    defrostTriggerInt = new NumberField("próg załączenia [hPa]", val, 20, 100, 1);
    defrostTriggerInt
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent
                  .getDefrostMode()
                  .getTriggerInt()
                  .setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(defrostTriggerInt.getSource());
              module_vent.setReqUpdateValues(true);
            });
    val = (int) module_vent.getDefrostMode().getDelayTime().getIsValue();
    defrostDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
    defrostDelayTime
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent
                  .getDefrostMode()
                  .getDelayTime()
                  .setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(defrostDelayTime.getSource());
              module_vent.setReqUpdateValues(true);
            });
  }

  private void createInfoSection1() {
    final int index = 1;
    activeCoolingButton =
        new Button("chłodzenie", true, (boolean) module_vent.getActiveCooling().getIsValue());
    activeCoolingButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getActiveCooling()
                  .setNewValue(!(boolean) module_vent.getActiveCooling().getIsValue());
              setPendingColor(activeCoolingButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    activeHeatingButton =
        new Button("dogrzewanie", true, (boolean) module_vent.getActiveHeating().getIsValue());
    activeHeatingButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getActiveHeating()
                  .setNewValue(!(boolean) module_vent.getActiveHeating().getIsValue());
              setPendingColor(activeHeatingButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    minTempNumberField =
        new NumberField(
            "histereza dogrzewania [°C]", (int) module_vent.getMinTemp().getIsValue(), 20, 100, 1);
    minTempNumberField
        .getSource()
        .addValueChangeListener(
            valueChangeEvent -> {
              module_vent.getMinTemp().setNewValue((int) Math.round(valueChangeEvent.getValue()));
              setPendingColor(minTempNumberField.getSource());
              module_vent.setReqUpdateValues(true);
            });

    reqLazDolButton =
        new Button("łazienka dół", true, (boolean) module_vent.getReqLazDol().getIsValue());
    reqLazDolButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getReqLazDol()
                  .setNewValue(!(boolean) module_vent.getReqLazDol().getIsValue());
              setPendingColor(reqLazDolButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    reqLazGoraButton =
        new Button("łazienka góra", true, (boolean) module_vent.getReqLazGora().getIsValue());
    reqLazGoraButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getReqLazGora()
                  .setNewValue(!(boolean) module_vent.getReqLazGora().getIsValue());
              setPendingColor(reqLazGoraButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    reqKuchniaButton =
        new Button("kuchnia", true, (boolean) module_vent.getReqKuchnia().getIsValue());
    reqKuchniaButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getReqKuchnia()
                  .setNewValue(!(boolean) module_vent.getReqKuchnia().getIsValue());
              setPendingColor(reqKuchniaButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    reqKuchniaButton =
        new Button("kuchnia", true, (boolean) module_vent.getReqKuchnia().getIsValue());
    reqKuchniaButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_vent
                  .getReqKuchnia()
                  .setNewValue(!(boolean) module_vent.getReqKuchnia().getIsValue());
              setPendingColor(reqKuchniaButton.getSource());
              module_vent.setReqUpdateValues(true);
            });

    info[index][2][0] =
        new Info(
            "woda wlot",
            "°C",
            false,
            false,
            module_vent.getHeatExchanger()[0],
            module_vent.getHeatExchanger()[0],
            0.5,
            1);
    info[index][2][1] =
        new Info(
            "woda wylot",
            "°C",
            false,
            false,
            module_vent.getHeatExchanger()[1],
            module_vent.getHeatExchanger()[1],
            0.5,
            1);
    info[index][2][2] =
        new Info(
            "powietrze wlot",
            "°C",
            false,
            false,
            module_vent.getHeatExchanger()[2],
            module_vent.getHeatExchanger()[2],
            0.5,
            1);
    info[index][2][3] =
        new Info(
            "powietrze wylot",
            "°C",
            false,
            false,
            module_vent.getHeatExchanger()[3],
            module_vent.getHeatExchanger()[3],
            0.5,
            1);

    info[index][3][0] = new Info("salon1", true, module_vent.isSalon1());
    info[index][3][1] = new Info("salon2", true, module_vent.isSalon2());
    info[index][3][2] = new Info("gabinet", true, module_vent.isGabinet());
    info[index][3][3] = new Info("warsztat", true, module_vent.isWarsztat());

    info[index][4][0] = new Info("rodzice", true, module_vent.isRodzice());
    info[index][4][1] = new Info("Natalia", true, module_vent.isNatalia());
    info[index][4][2] = new Info("Karolina", true, module_vent.isKarolina());

    info[index][5][0] = new Info("kuchnia", true, module_vent.isKuchnia());
    info[index][5][1] = new Info("lazDol1", true, module_vent.isLazDol1());
    info[index][5][2] = new Info("lazDol2", true, module_vent.isLazDol2());
    info[index][5][3] = new Info("pralnia", true, module_vent.isPralnia());

    info[index][6][0] = new Info("przedpokoj", true, module_vent.isPrzedpokoj());
    info[index][6][1] = new Info("garderoba", true, module_vent.isGarderoba());
    info[index][6][2] = new Info("lazGora1", true, module_vent.isLazGora1());
    info[index][6][3] = new Info("lazGora2", true, module_vent.isLazGora2());
  }

  private void createInfoSection2() {
    final int index = 2;
    final BME280[] bme280 = module_vent.getBme280();
    for (int i = 0; i < 4; i++) {
      info[index][i][0] = new Info("temp", "°C", false, false, bme280[i].getTemp(), 0, 0, 0);
      info[index][i][1] =
          new Info("wilgotność", "%", false, false, bme280[i].getHumidity(), 0, 0, 0);
      info[index][i][2] =
          new Info("ciśnienie", "hPa", false, false, bme280[i].getPressure(), 0, 0, 0);
    }
    final Fan[] fans = module_vent.getFan();
    info[index][4][0] =
        new Info("prędkość CZERPNIA", "%", false, false, fans[0].getSpeed(), 0, 0, 0);
    info[index][4][1] =
        new Info("obroty CZERPNIA", "min-1", false, false, fans[0].getRev(), 0, 0, 0);
    info[index][4][2] =
        new Info("prędkość WYRZUTNIA", "%", false, false, fans[1].getSpeed(), 0, 0, 0);
    info[index][4][3] =
        new Info("obroty WYRZUTNIA", "min-1", false, false, fans[1].getRev(), 0, 0, 0);
  }

  private void createInfoSection3() {
    final int index = 3;
    activeRegGrid = prepareGrid(module_vent.getActiveTempRegByHours());
    activeRegGrid.setItems(getDiagram(module_vent.getActiveTempRegByHours()));
  }

  private void createInfoSection4() {
    final int index = 4;
    normalModeGrid = prepareGrid(module_vent.getNormalOnByHours());
    normalModeGrid.setItems(getDiagram(module_vent.getNormalOnByHours()));
  }

  private Grid prepareGrid(final VentZones[] ventZonesArr) {
    final Grid<VentZonesByHour> grid = new Grid<>();
    //		grid.addColumn(VentZonesByHour::getHour).setHeader("Godzina");

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Label label = new Label("" + cell.getHour());
                  layout.add(label);
                  layout.addClickListener(
                      horizontalLayoutClickEvent -> {
                        gridListnerRow(ventZonesArr[cell.getHour()]);
                      });
                  return layout;
                  //		})).setHeader("Godzina");
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>Godzina</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getSalon());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>Salon</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getPralnia());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>Pralnia</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getLazDol());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>łaź.dół</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getRodzice());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>rodzice</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getNatalia());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>Natalia</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getKarolina());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>Karolina</div>"));

    grid.addColumn(
            new ComponentRenderer<>(
                cell -> {
                  return setCell(cell.getVentZones().getLazGora());
                }))
        .setHeader(
            new Html(
                "<div style='text-orientation: mixed;writing-mode: vertical-rl;'>łaź.góra</div>"));
    grid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));

    grid.getStyle().set("font-size", "15px");
    return grid;
  }

  private HorizontalLayout setCell(final Zone zone) {
    final boolean result = (boolean) zone.getRequest().getIsValue();
    final String text = result ? "❶" : "⓿";
    //		String text = result ? "●" : "◦";
    final Info info = new Info(text, true, result);
    info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(zone));
    info.setValue(result);
    if (!zone.getRequest().isUpToDate()) {
      info.getNameLabel().getStyle().set("color", "orange");
    }
    return info.getSource();
  }

  private List<VentZonesByHour> getDiagram(final VentZones[] ventZones) {
    final ArrayList<VentZonesByHour> list = new ArrayList<>();
    for (int i = 0; i < 24; i++) {
      final VentZonesByHour ventZonesByHour = new VentZonesByHour();
      ventZonesByHour.setHour(i);
      ventZonesByHour.setVentZones(ventZones[i]);
      list.add(ventZonesByHour);
    }
    return list;
  }

  private void gridListener(final Zone zone) {
    if (!isUserLoggedIn()) {
      return;
    }
    zone.getRequest().setNewValue(!(boolean) zone.getRequest().getIsValue());
    module_vent.setReqUpdateValues(true);
  }

  private void gridListnerRow(final VentZones ventZones) {
    if (!isUserLoggedIn()) {
      return;
    }
    final boolean status = !(boolean) ventZones.getSalon().getRequest().getIsValue();
    ventZones.getSalon().getRequest().setNewValue(status);
    ventZones.getPralnia().getRequest().setNewValue(status);
    ventZones.getLazDol().getRequest().setNewValue(status);
    ventZones.getRodzice().getRequest().setNewValue(status);
    ventZones.getNatalia().getRequest().setNewValue(status);
    ventZones.getKarolina().getRequest().setNewValue(status);
    ventZones.getLazGora().getRequest().setNewValue(status);
    module_vent.setReqUpdateValues(true);
  }

  @Override
  void update() {
    // Header
    header.setLastUpdate(module_vent.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_vent.getDiagnosticLastUpdate());

    int index = 0;
    // Tryby
    info[index][0][0].setValue(module_vent.isNormalOn());
    info[index][0][1].setValue(module_vent.isHumidityAlert());
    info[index][0][2].setValue(module_vent.isDefrostActive());
    reqAutoDiagnosisButton.setButtonColor(
        (boolean) module_vent.getReqAutoDiagnosis().getIsValue(),
        (boolean) module_vent.getReqAutoDiagnosis().getNewValue());

    // Status
    info[index][1][0].setValue(module_vent.isBypassOpen());
    info[index][1][1].setValue(module_vent.isCircuitPump());
    info[index][1][2].setValue(module_vent.isReqPumpColdWater());
    info[index][1][3].setValue(module_vent.isReqPumpHotWater());

    // NormaOn tryb
    info[index][2][0].setValue((boolean) module_vent.getNormalMode().getTrigger().getIsValue());
    info[index][2][1].setValue(module_vent.getNormalMode().getTimeLeft());
    normalDelayTime.setNumberField(
        (int) module_vent.getNormalMode().getDelayTime().getIsValue(),
        (int) module_vent.getNormalMode().getDelayTime().getNewValue());

    // HumidityAlert tryb
    info[index][3][0].setValue(
        (boolean) module_vent.getHumidityAlertMode().getTrigger().getIsValue());
    info[index][3][1].setValue(module_vent.getHumidityAlertMode().getTimeLeft());
    double val = (int) module_vent.getHumidityAlertMode().getTriggerInt().getIsValue();
    humidityTriggerInt.setNumberField(
        (int) module_vent.getHumidityAlertMode().getTriggerInt().getIsValue(),
        (int) module_vent.getHumidityAlertMode().getTriggerInt().getNewValue());
    humidityDelayTime.setNumberField(
        (int) module_vent.getHumidityAlertMode().getDelayTime().getIsValue(),
        (int) module_vent.getHumidityAlertMode().getDelayTime().getNewValue());

    // Defrost tryb
    info[index][4][0].setValue((boolean) module_vent.getDefrostMode().getTrigger().getIsValue());
    info[index][4][1].setValue(module_vent.getDefrostMode().getTimeLeft());
    val = (int) module_vent.getDefrostMode().getTriggerInt().getIsValue();
    defrostTriggerInt.setNumberField(
        (int) module_vent.getDefrostMode().getTriggerInt().getIsValue(),
        (int) module_vent.getDefrostMode().getTriggerInt().getNewValue());
    defrostDelayTime.setNumberField(
        (int) module_vent.getDefrostMode().getDelayTime().getIsValue(),
        (int) module_vent.getDefrostMode().getDelayTime().getNewValue());

    index = 1;
    activeCoolingButton.setButtonColor(
        (boolean) module_vent.getActiveCooling().getIsValue(),
        (boolean) module_vent.getActiveCooling().getNewValue());
    activeHeatingButton.setButtonColor(
        (boolean) module_vent.getActiveHeating().getIsValue(),
        (boolean) module_vent.getActiveHeating().getNewValue());

    minTempNumberField.setNumberField(
        (int) module_vent.getMinTemp().getIsValue(), (int) module_vent.getMinTemp().getNewValue());
    reqLazDolButton.setButtonColor(
        (boolean) module_vent.getReqLazDol().getIsValue(),
        (boolean) module_vent.getReqLazDol().getNewValue());
    reqLazGoraButton.setButtonColor(
        (boolean) module_vent.getReqLazGora().getIsValue(),
        (boolean) module_vent.getReqLazGora().getNewValue());
    reqKuchniaButton.setButtonColor(
        (boolean) module_vent.getReqKuchnia().getIsValue(),
        (boolean) module_vent.getReqKuchnia().getNewValue());

    info[index][2][0].setValue(module_vent.getHeatExchanger()[0]);
    info[index][2][1].setValue(module_vent.getHeatExchanger()[1]);
    info[index][2][2].setValue(module_vent.getHeatExchanger()[2]);
    info[index][2][3].setValue(module_vent.getHeatExchanger()[3]);

    info[index][3][0].setValue(module_vent.isSalon1());
    info[index][3][0].setValue(module_vent.isSalon2());
    info[index][3][1].setValue(module_vent.isSalon2());
    info[index][3][2].setValue(module_vent.isGabinet());
    info[index][3][3].setValue(module_vent.isWarsztat());

    info[index][4][0].setValue(module_vent.isRodzice());
    info[index][4][1].setValue(module_vent.isNatalia());
    info[index][4][2].setValue(module_vent.isKarolina());

    info[index][5][0].setValue(module_vent.isKuchnia());
    info[index][5][1].setValue(module_vent.isLazDol1());
    info[index][5][2].setValue(module_vent.isLazDol2());
    info[index][5][3].setValue(module_vent.isPralnia());

    info[index][6][0].setValue(module_vent.isPrzedpokoj());
    info[index][6][1].setValue(module_vent.isGarderoba());
    info[index][6][2].setValue(module_vent.isLazGora1());
    info[index][6][3].setValue(module_vent.isLazGora2());

    index = 2;
    final BME280[] bme280 = module_vent.getBme280();
    for (int i = 0; i < 4; i++) {
      info[index][i][0].setValue(bme280[i].getTemp());
      info[index][i][1].setValue(bme280[i].getHumidity());
      info[index][i][2].setValue(bme280[i].getPressure());
    }

    final Fan[] fans = module_vent.getFan();
    info[index][4][0].setValue(fans[0].getSpeed());
    info[index][4][1].setValue(fans[0].getRev());
    info[index][4][2].setValue(fans[1].getSpeed());
    info[index][4][3].setValue(fans[1].getRev());

    activeRegGrid.setItems(getDiagram(module_vent.getActiveTempRegByHours()));
    normalModeGrid.setItems(getDiagram(module_vent.getNormalOnByHours()));
  }

  private static class VentZonesByHour {
    private int hour;
    private VentZones ventZones;

    public VentZonesByHour() {}

    public int getHour() {
      return hour;
    }

    public void setHour(final int hour) {
      this.hour = hour;
    }

    public VentZones getVentZones() {
      return ventZones;
    }

    public void setVentZones(final VentZones ventZones) {
      this.ventZones = ventZones;
    }
  }
}
