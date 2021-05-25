package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_ExtLights;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.time.LocalTime;

@PageTitle("Smart House | Oświetlenie")
@Route(value = "Oswietlenie", layout = MainLayout.class)
public class ExtLightsView extends View {

	@Autowired
	Module_ExtLights module_extLights;

	//Update thread
	Thread thread;

	//Objects
	Header header;
	Section[] section = new Section[2];
	Info[][][] info = new Info[1][2][4];


	public ExtLightsView(Module_ExtLights module_extLights) {
		this.module_extLights = module_extLights;

		//Header
		header = new Header(module_extLights, "light-bulb.svg");
		header.setLastUpdate(module_extLights.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_extLights.getDiagnosticLastUpdate());

		//Sections
		section[0] = new Section();
		section[1] = new Section();

		//Create tile for sections
		//Section 0
		section[0].createTile("light-bulb.svg", "Wejście");
		section[0].createTile("light-bulb.svg", "Podjazd");
		section[0].createTile("light-bulb.svg", "Carport");
		section[0].createTile("light-bulb.svg", "Ogrodzenie");

		section[1].createTile("settings.svg", "Ustawienia");

		//Create sections info/buttons/number fields
		createInfoSection0();
		createInfoSection1();

		Notification notification = new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
		section[0].getSection().addClickListener(event -> {
			System.out.println("click");
			if (!isUserLoggedIn())
				notification.open();
		});
		section[1].getSection().addClickListener(event -> {
			if (!isUserLoggedIn())
				notification.open();
		});
		section[0].getSection().setEnabled(isUserLoggedIn());
		section[1].getSection().setEnabled(isUserLoggedIn());
		add(header.getHeader(), section[0].getSection(), section[1].getSection());
	}

	private VerticalLayout createLightView(int id) {
		VerticalLayout layout = new VerticalLayout();
		Button button100 = new Button("Wymuś Max", true, module_extLights.getLightDimmer()[id].isForceMax());
		button100.getSource().addClickListener(buttonClickEvent -> {
			module_extLights.getNVLightDimmer()[id].setForceMax(!module_extLights.getLightDimmer()[id].isForceMax());
			if (module_extLights.getNVLightDimmer()[id].isForceMax())
				module_extLights.getNVLightDimmer()[id].setForce0(false);
			setPendingColor(button100.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		Button button0 = new Button("Wymuś 0%", true, module_extLights.getLightDimmer()[id].isForce0());
		button0.getSource().addClickListener(buttonClickEvent -> {
			module_extLights.getNVLightDimmer()[id].setForce0(!module_extLights.getLightDimmer()[id].isForce0());
			if (module_extLights.getNVLightDimmer()[id].isForce0())
				module_extLights.getNVLightDimmer()[id].setForceMax(false);
			setPendingColor(button0.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		Info info = new Info("intensywność", "%", false, false, module_extLights.getLightDimmer()[id].getIntens(), 0, 0, 0);

		NumberField setIntens = new NumberField("standBy [%]", module_extLights.getLightDimmer()[id].getStandByIntens(), 10, 90, 1);
		setIntens.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLights.getNVLightDimmer()[id].setStandByIntens((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(setIntens.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		NumberField maxIntens = new NumberField("Max [%]", module_extLights.getLightDimmer()[id].getMaxIntens(), 50, 100, 1);
		maxIntens.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLights.getNVLightDimmer()[id].setMaxIntens((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(maxIntens.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		layout.add(button100.getSource(), button0.getSource(), info.getSource(), setIntens.getSource(), maxIntens.getSource());
		return layout;
	}

	private void createInfoSection0() {
		for (int i = 0; i < 4; i++) {
			section[0].getTileDetailsContainer(i).add(createLightView(i));
		}
	}

	private void createInfoSection1() {
		VerticalLayout layout = new VerticalLayout();

		NumberField setStartIntensLevel = new NumberField("próg załączenia [%]", module_extLights.getStartLightLevel(), 5, 50, 1);
		setStartIntensLevel.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLights.setNVstartLightLevel((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(setStartIntensLevel.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		NumberField setOffTimeHour = new NumberField("godzina wyłączenia", module_extLights.getOffTime().getHour(), 0, 23, 1);
		setOffTimeHour.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLights.setNVoffTime(LocalTime.of((int) Math.round(valueChangeEvent.getValue()), module_extLights.getOffTime().getMinute()));
			setPendingColor(setOffTimeHour.getSource());
			module_extLights.setReqUpdateValues(true);
		});
		NumberField setOffTimeMinute = new NumberField("minuta wyłączenia", module_extLights.getOffTime().getMinute(), 0, 59, 1);
		setOffTimeMinute.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLights.setNVoffTime(LocalTime.of(module_extLights.getOffTime().getHour(), (int) Math.round(valueChangeEvent.getValue())));
			setPendingColor(setOffTimeMinute.getSource());
			module_extLights.setReqUpdateValues(true);
		});

		layout.add(setStartIntensLevel.getSource(), setOffTimeHour.getSource(), setOffTimeMinute.getSource());
		section[1].getTileDetailsContainer(0).add(layout);
	}

	void update() {
		//Header
		header.setLastUpdate(module_extLights.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_extLights.getDiagnosticLastUpdate());

		for (int i = 0; i < 4; i++) {
			VerticalLayout layout = (VerticalLayout) section[0].getTileDetailsContainer(i).getComponentAt(0);

			Button buttonMax = new Button((com.vaadin.flow.component.button.Button) layout.getComponentAt(0));
			buttonMax.setButtonColor(module_extLights.getLightDimmer()[i].isForceMax(), module_extLights.getNVLightDimmer()[i].isForceMax());
			Button button0 = new Button((com.vaadin.flow.component.button.Button) layout.getComponentAt(1));
			button0.setButtonColor(module_extLights.getLightDimmer()[i].isForce0(), module_extLights.getNVLightDimmer()[i].isForce0());

			Info intens = new Info((HorizontalLayout) layout.getComponentAt(2));
			intens.setValue(module_extLights.getLightDimmer()[i].getIntens());

			NumberField standByIntens = new NumberField((com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(3));
			standByIntens.setNumberField(module_extLights.getLightDimmer()[i].getStandByIntens(), module_extLights.getNVLightDimmer()[i].getStandByIntens());

			NumberField maxIntens = new NumberField((com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(4));
			maxIntens.setNumberField(module_extLights.getLightDimmer()[i].getMaxIntens(), module_extLights.getNVLightDimmer()[i].getMaxIntens());
		}

		VerticalLayout layout = (VerticalLayout) section[1].getTileDetailsContainer(0).getComponentAt(0);

		NumberField startLevel = new NumberField((com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(0));
		startLevel.setNumberField(module_extLights.getStartLightLevel(), module_extLights.getNVstartLightLevel());

		NumberField hour = new NumberField((com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(1));

		hour.setNumberField(module_extLights.getOffTime().getHour(), module_extLights.getNVoffTime().getHour());

		NumberField minute = new NumberField((com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(2));
		minute.setNumberField(module_extLights.getOffTime().getMinute(), module_extLights.getNVoffTime().getMinute());

	}
}

