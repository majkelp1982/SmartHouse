package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.BME280;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.Fan;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent2;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.VentZones;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.Zone;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Smart House | Wentylacja2")
@Route(value = "Wentylacja2", layout = MainLayout.class)
public class VentView2 extends View {

	@Autowired
	Module_Vent2 module_vent2;

	//Update thread
	Thread thread;

	//Objects
	Header header;
	Section[] section = new Section[5];
	Info[][][] info = new Info[4][5][4];

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

	public VentView2(Module_Vent2 module_vent2) {
		this.module_vent2 = module_vent2;
		//Header
		header = new Header(module_vent2, "recu.svg");
		header.setLastUpdate(module_vent2.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_vent2.getDiagnosticLastUpdate());

		//Sections
		section[0] = new Section();
		section[1] = new Section();
		section[2] = new Section();
		section[3] = new Section();
		section[4] = new Section();

		//Section 0 Tryby
		section[0].createTile("settings.svg", "Tryby");
		section[0].createTile("status.svg", "Status");
		section[0].createTile("fan.svg", "Normalny");
		section[0].createTile("fan.svg", "Wilgoć");
		section[0].createTile("fan.svg", "Odmrażanie");

		//Section 1 Tryby
		section[1].createTile("settings.svg", "Tryby");
		section[1].createTile("settings.svg", "Wymuś");
		section[1].createTile("thermometer.svg", "Wymiennik");


		//Section 2 BME i wentylatory
		section[2].createTile("thermometer.svg", "Czerpnia");
		section[2].createTile("thermometer.svg", "Wyrzutnia");
		section[2].createTile("thermometer.svg", "Nawiew");
		section[2].createTile("thermometer.svg", "Wywiew");
		section[2].createTile("fan.svg", "Wentylatory");

		//Section 3 ActiveTempMode
		section[3].createTile("settings.svg", "ActiveTemp Mode");

		//Section 2 NormalMode
		section[4].createTile("settings.svg", "Normal Mode");

		//Create sections info/buttons/number fields
		createInfoSection0();
		createInfoSection1();
		createInfoSection2();
		createInfoSection3();
		createInfoSection4();

		//Section 0
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

		//Section 1
		index = 1;
		section[index].getTileDetailsContainer(0).add(activeCoolingButton.getSource());
		section[index].getTileDetailsContainer(0).add(activeHeatingButton.getSource());
		section[index].getTileDetailsContainer(0).add(minTempNumberField.getSource());

		section[index].getTileDetailsContainer(1).add(reqLazDolButton.getSource());
		section[index].getTileDetailsContainer(1).add(reqLazGoraButton.getSource());
		section[index].getTileDetailsContainer(1).add(reqKuchniaButton.getSource());
		for (int i = 0; i < 4; i++)
			section[index].getTileDetailsContainer(2).add(info[index][2][i].getSource());

		//Section 2
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

		//Section 3
		index = 3;
		section[index].getTileDetailsContainer(0).add(activeRegGrid);
		section[index].getTileDetailsContainer(0).setWidth("690px");
		section[index].getTileDetailsContainer(0).setHeight("960px");
		//Section 4
		index = 4;
		section[index].getTileDetailsContainer(0).add(normalModeGrid);
		section[index].getTileDetailsContainer(0).setWidth("690px");
		section[index].getTileDetailsContainer(0).setHeight("960px");

		// Notification if user doesn't logged
		Notification notification = new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
		section[0].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[0].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[1].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[2].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[3].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[4].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});

		section[0].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
		section[0].getTileDetailsContainer(3).setEnabled(isUserLoggedIn());
		section[0].getTileDetailsContainer(4).setEnabled(isUserLoggedIn());

		section[1].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
		section[1].getTileDetailsContainer(1).setEnabled(isUserLoggedIn());

		add(header.getHeader(), section[0].getSection(), section[1].getSection(),
				section[2].getSection(), section[3].getSection(), section[4].getSection());
	}

	private void createInfoSection0() {
		int index = 0;
		//Tryby
		info[index][0][0] = new Info("normalON", true, module_vent2.isNormalOn());
		info[index][0][1] = new Info("humidityAlert", true, module_vent2.isHumidityAlert());
		info[index][0][2] = new Info("defrost", true, module_vent2.isDefrostActive());
		reqAutoDiagnosisButton = new Button("Autodiagnoza", true, (boolean) module_vent2.getReqAutoDiagnosis().getIsValue());
		reqAutoDiagnosisButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getReqAutoDiagnosis().setNewValue(!(boolean) module_vent2.getReqAutoDiagnosis().getIsValue());
			setPendingColor(reqAutoDiagnosisButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		//Status
		info[index][1][0] = new Info("ByPass", true, module_vent2.isBypassOpen());
		info[index][1][1] = new Info("Pompa", true, module_vent2.isCircuitPump());
		info[index][1][2] = new Info("reqColdWater", true, module_vent2.isReqPumpColdWater());
		info[index][1][3] = new Info("reqHotWater", true, module_vent2.isReqPumpHotWater());

		//NormaOn tryb
		info[index][2][0] = new Info("trigger", true, (boolean) module_vent2.getNormalMode().getTrigger().getIsValue());
		info[index][2][1] = new Info("czas do końca", "min", false, false, module_vent2.getNormalMode().getTimeLeft(), 0, 0, 0);
		double val = (int) module_vent2.getNormalMode().getDelayTime().getIsValue();
		normalDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
		normalDelayTime.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getNormalMode().getDelayTime().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(normalDelayTime.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		//HumidityAlert tryb
		info[index][3][0] = new Info("trigger", true, (boolean) module_vent2.getHumidityAlertMode().getTrigger().getIsValue());
		info[index][3][1] = new Info("czas do końca", "min", false, false, module_vent2.getHumidityAlertMode().getTimeLeft(), 0, 0, 0);
		val = (int) module_vent2.getHumidityAlertMode().getTriggerInt().getIsValue();
		humidityTriggerInt = new NumberField("próg załączenia [%]", val, 20, 100, 1);
		humidityTriggerInt.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getHumidityAlertMode().getTriggerInt().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(humidityTriggerInt.getSource());
			module_vent2.setReqUpdateValues(true);
		});
		val = (int) module_vent2.getHumidityAlertMode().getDelayTime().getIsValue();
		humidityDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
		humidityDelayTime.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getHumidityAlertMode().getDelayTime().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(humidityDelayTime.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		//Defrost tryb
		info[index][4][0] = new Info("trigger", true, (boolean) module_vent2.getDefrostMode().getTrigger().getIsValue());
		info[index][4][1] = new Info("czas do końca", "min", false, false, module_vent2.getDefrostMode().getTimeLeft(), 0, 0, 0);
		val = (int) module_vent2.getDefrostMode().getTriggerInt().getIsValue();
		defrostTriggerInt = new NumberField("próg załączenia [hPa]", val, 20, 100, 1);
		defrostTriggerInt.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getDefrostMode().getTriggerInt().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(defrostTriggerInt.getSource());
			module_vent2.setReqUpdateValues(true);
		});
		val = (int) module_vent2.getDefrostMode().getDelayTime().getIsValue();
		defrostDelayTime = new NumberField("czas podtrzymania [min]", val, 20, 100, 1);
		defrostDelayTime.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getDefrostMode().getDelayTime().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(defrostDelayTime.getSource());
			module_vent2.setReqUpdateValues(true);
		});
	}

	private void createInfoSection1() {
		int index = 1;
		activeCoolingButton = new Button("chłodzenie", true, (boolean) module_vent2.getActiveCooling().getIsValue());
		activeCoolingButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getActiveCooling().setNewValue(!(boolean) module_vent2.getActiveCooling().getIsValue());
			setPendingColor(activeCoolingButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		activeHeatingButton = new Button("dogrzewanie", true, (boolean) module_vent2.getActiveHeating().getIsValue());
		activeHeatingButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getActiveHeating().setNewValue(!(boolean) module_vent2.getActiveHeating().getIsValue());
			setPendingColor(activeHeatingButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		minTempNumberField = new NumberField("histereza dogrzewania [°C]", (int) module_vent2.getMinTemp().getIsValue(), 20, 100, 1);
		minTempNumberField.getSource().addValueChangeListener(valueChangeEvent -> {
			module_vent2.getMinTemp().setNewValue((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(minTempNumberField.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		reqLazDolButton = new Button("łazienka dół", true, (boolean) module_vent2.getReqLazDol().getIsValue());
		reqLazDolButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getReqLazDol().setNewValue(!(boolean) module_vent2.getReqLazDol().getIsValue());
			setPendingColor(reqLazDolButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		reqLazGoraButton = new Button("łazienka góra", true, (boolean) module_vent2.getReqLazGora().getIsValue());
		reqLazGoraButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getReqLazGora().setNewValue(!(boolean) module_vent2.getReqLazGora().getIsValue());
			setPendingColor(reqLazGoraButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		reqKuchniaButton = new Button("kuchnia", true, (boolean) module_vent2.getReqKuchnia().getIsValue());
		reqKuchniaButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getReqKuchnia().setNewValue(!(boolean) module_vent2.getReqKuchnia().getIsValue());
			setPendingColor(reqKuchniaButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});

		reqKuchniaButton = new Button("kuchnia", true, (boolean) module_vent2.getReqKuchnia().getIsValue());
		reqKuchniaButton.getSource().addClickListener(buttonClickEvent -> {
			module_vent2.getReqKuchnia().setNewValue(!(boolean) module_vent2.getReqKuchnia().getIsValue());
			setPendingColor(reqKuchniaButton.getSource());
			module_vent2.setReqUpdateValues(true);
		});


		info[index][2][0] = new Info("woda wlot", "°C", false, false, module_vent2.getHeatExchanger()[0], module_vent2.getHeatExchanger()[0], 0.5, 1);
		info[index][2][1] = new Info("woda wylot", "°C", false, false, module_vent2.getHeatExchanger()[1], module_vent2.getHeatExchanger()[1], 0.5, 1);
		info[index][2][2] = new Info("powietrze wlot", "°C", false, false, module_vent2.getHeatExchanger()[2], module_vent2.getHeatExchanger()[2], 0.5, 1);
		info[index][2][3] = new Info("powietrze wylot", "°C", false, false, module_vent2.getHeatExchanger()[3], module_vent2.getHeatExchanger()[3], 0.5, 1);

	}

	private void createInfoSection2() {
		int index = 2;
		BME280[] bme280 = module_vent2.getBme280();
		for (int i = 0; i < 4; i++) {
			info[index][i][0] = new Info("temp", "°C", false, false, bme280[i].getTemp(), 0, 0, 0);
			info[index][i][1] = new Info("wilgotność", "%", false, false, bme280[i].getHumidity(), 0, 0, 0);
			info[index][i][2] = new Info("ciśnienie", "hPa", false, false, bme280[i].getPressure(), 0, 0, 0);
		}
		Fan[] fans = module_vent2.getFan();
		info[index][4][0] = new Info("prędkość CZERPNIA", "%", false, false, fans[0].getSpeed(), 0, 0, 0);
		info[index][4][1] = new Info("obroty CZERPNIA", "min-1", false, false, fans[0].getRev(), 0, 0, 0);
		info[index][4][2] = new Info("prędkość WYRZUTNIA", "%", false, false, fans[1].getSpeed(), 0, 0, 0);
		info[index][4][3] = new Info("obroty WYRZUTNIA", "min-1", false, false, fans[1].getRev(), 0, 0, 0);
	}

	private void createInfoSection3() {
		int index = 3;
		activeRegGrid = prepareGrid();
		activeRegGrid.setItems(getDiagram(module_vent2.getActiveTempRegByHours()));
	}

	private void createInfoSection4() {
		int index = 4;
		normalModeGrid = prepareGrid();
		normalModeGrid.setItems(getDiagram(module_vent2.getNormalOnByHours()));
	}

	private Grid prepareGrid() {
		Grid<VentZonesByHour> grid = new Grid<>();
		grid.addColumn(VentZonesByHour::getHour).setHeader("Godzina");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getSalon());
		})).setHeader("Salon");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getPralnia());
		})).setHeader("Pralnia");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getLazDol());
		})).setHeader("łaź.dół");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getRodzice());
		})).setHeader("Rodzice");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getNatalia());
		})).setHeader("Natalia");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getKarolina());
		})).setHeader("Karolina");

		grid.addColumn(new ComponentRenderer<>(cell -> {
			return setCell(cell.getVentZones().getLazGora());
		})).setHeader("łaź.góra");
		grid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));
		return grid;
	}

	private HorizontalLayout setCell(Zone zone) {
		boolean result = (boolean) zone.getRequest().getIsValue();
		String text = result ? "❶" : "⓿";
		Info info = new Info(text, true, result);
		info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(zone));
		info.setValue(result);
		if (!zone.getRequest().isUpToDate())
		info.getNameLabel().getStyle().set("color", "orange");
		return info.getSource();
	}

	private List<VentZonesByHour> getDiagram(VentZones[] ventZones) {
		ArrayList<VentZonesByHour> list = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			VentZonesByHour ventZonesByHour = new VentZonesByHour();
			ventZonesByHour.setHour(i);
			ventZonesByHour.setVentZones(ventZones[i]);
			list.add(ventZonesByHour);
		}
		return list;
	}


	private void gridListener(Zone zone) {
		if (!isUserLoggedIn()) return;
		zone.getRequest().setNewValue(!(boolean)zone.getRequest().getIsValue());
		module_vent2.setReqUpdateValues(true);
	}

	void update() {
		//Header
		header.setLastUpdate(module_vent2.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_vent2.getDiagnosticLastUpdate());

		int index = 0;
		//Tryby
		info[index][0][0].setValue(module_vent2.isNormalOn());
		info[index][0][1].setValue(module_vent2.isHumidityAlert());
		info[index][0][2].setValue(module_vent2.isDefrostActive());
		reqAutoDiagnosisButton.setButtonColor((boolean)module_vent2.getReqAutoDiagnosis().getIsValue(), (boolean)module_vent2.getReqAutoDiagnosis().getNewValue());

		//Status
		info[index][1][0].setValue(module_vent2.isBypassOpen());
		info[index][1][1].setValue(module_vent2.isCircuitPump());
		info[index][1][2].setValue(module_vent2.isReqPumpColdWater());
		info[index][1][3].setValue(module_vent2.isReqPumpHotWater());

		//NormaOn tryb
		info[index][2][0].setValue((boolean) module_vent2.getNormalMode().getTrigger().getIsValue());
		info[index][2][1].setValue(module_vent2.getNormalMode().getTimeLeft());
		normalDelayTime.setNumberField((int)module_vent2.getNormalMode().getDelayTime().getIsValue(),(int)module_vent2.getNormalMode().getDelayTime().getNewValue());

		//HumidityAlert tryb
		info[index][3][0].setValue((boolean)module_vent2.getHumidityAlertMode().getTrigger().getIsValue());
		info[index][3][1].setValue(module_vent2.getHumidityAlertMode().getTimeLeft());
		double val = (int) module_vent2.getHumidityAlertMode().getTriggerInt().getIsValue();
		humidityTriggerInt.setNumberField((int)module_vent2.getHumidityAlertMode().getTriggerInt().getIsValue(),(int)module_vent2.getHumidityAlertMode().getTriggerInt().getNewValue());
		humidityDelayTime.setNumberField((int)module_vent2.getHumidityAlertMode().getDelayTime().getIsValue(),(int)module_vent2.getHumidityAlertMode().getDelayTime().getNewValue());

		//Defrost tryb
		info[index][4][0].setValue((boolean) module_vent2.getDefrostMode().getTrigger().getIsValue());
		info[index][4][1].setValue(module_vent2.getDefrostMode().getTimeLeft());
		val = (int) module_vent2.getDefrostMode().getTriggerInt().getIsValue();
		defrostTriggerInt.setNumberField((int)module_vent2.getDefrostMode().getTriggerInt().getIsValue(), (int)module_vent2.getDefrostMode().getTriggerInt().getNewValue());
		defrostDelayTime.setNumberField((int)module_vent2.getDefrostMode().getDelayTime().getIsValue(),(int)module_vent2.getDefrostMode().getDelayTime().getNewValue());

		index=1;
		activeCoolingButton.setButtonColor((boolean) module_vent2.getActiveCooling().getIsValue(), (boolean) module_vent2.getActiveCooling().getNewValue());
		activeHeatingButton.setButtonColor((boolean) module_vent2.getActiveHeating().getIsValue(), (boolean) module_vent2.getActiveHeating().getNewValue());

		minTempNumberField.setNumberField((int)module_vent2.getMinTemp().getIsValue(),(int)module_vent2.getMinTemp().getNewValue());
		reqLazDolButton.setButtonColor((boolean) module_vent2.getReqLazDol().getIsValue(), (boolean) module_vent2.getReqLazDol().getNewValue());
		reqLazGoraButton.setButtonColor((boolean) module_vent2.getReqLazGora().getIsValue(), (boolean) module_vent2.getReqLazGora().getNewValue());
		reqKuchniaButton.setButtonColor((boolean) module_vent2.getReqKuchnia().getIsValue(), (boolean) module_vent2.getReqKuchnia().getNewValue());


		info[index][2][0].setValue(module_vent2.getHeatExchanger()[0]);
		info[index][2][1].setValue(module_vent2.getHeatExchanger()[1]);
		info[index][2][2].setValue(module_vent2.getHeatExchanger()[2]);
		info[index][2][3].setValue(module_vent2.getHeatExchanger()[3]);

		index = 2;
		BME280[] bme280 = module_vent2.getBme280();
		for (int i = 0; i < 4; i++) {
			info[index][i][0].setValue(bme280[i].getTemp());
			info[index][i][1].setValue(bme280[i].getHumidity());
			info[index][i][2].setValue(bme280[i].getPressure());
		}

		Fan[] fans = module_vent2.getFan();
		info[index][4][0].setValue(fans[0].getSpeed());
		info[index][4][1].setValue(fans[0].getRev());
		info[index][4][2].setValue(fans[1].getSpeed());
		info[index][4][3].setValue(fans[1].getRev());

		activeRegGrid.setItems(getDiagram(module_vent2.getActiveTempRegByHours()));
		normalModeGrid.setItems(getDiagram(module_vent2.getNormalOnByHours()));
	}

	private static class VentZonesByHour {
		private int hour;
		private VentZones ventZones;

		@Autowired
		Module_Vent2 module_vent2;

		public VentZonesByHour() {
		}

		public int getHour() {
			return hour;
		}

		public void setHour(int hour) {
			this.hour = hour;
		}

		public VentZones getVentZones() {
			return ventZones;
		}

		public void setVentZones(VentZones ventZones) {
			this.ventZones = ventZones;
		}
	}
}
