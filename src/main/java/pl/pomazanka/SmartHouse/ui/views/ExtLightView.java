package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_ExtLight;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.time.LocalTime;

@PageTitle("Smart House | Oświetlenie")
@Route(value = "Oswietlenie", layout = MainLayout.class)
public class ExtLightView extends View {

	@Autowired
	Module_ExtLight module_extLight;

	//Update thread
	Thread thread;

	//Objects
	Header header;
	Section[] section = new Section[2];
	Info[][][] info = new Info[1][2][4];


	public ExtLightView(Module_ExtLight module_extLight) {
		this.module_extLight = module_extLight;

		//Header
		header = new Header(module_extLight, "light-bulb.svg");
		header.setLastUpdate(module_extLight.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_extLight.getDiagnosticLastUpdate());

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
			if (!isUserLoggedIn())
				notification.open();
		});
		section[0].getTileDetailsContainer(1).setEnabled(isUserLoggedIn());
		add(header.getHeader(), section[0].getSection(), section[1].getSection());
	}

	private VerticalLayout createLightView(int id) {
		VerticalLayout layout = new VerticalLayout();
		Button button100 = new Button("Wymuś Max", true, module_extLight.getLightDimmer()[id].isForceMax());
		button100.getSource().addClickListener(buttonClickEvent -> {
			module_extLight.getNVLightDimmer()[id].setForceMax(!module_extLight.getLightDimmer()[id].isForceMax());
			if (module_extLight.getNVLightDimmer()[id].isForceMax())
				module_extLight.getNVLightDimmer()[id].setForce0(false);
			setPendingColor(button100.getSource());
			module_extLight.setReqUpdateValues(true);
		});

		Button button0 = new Button("Wymuś 0%", true, module_extLight.getLightDimmer()[id].isForce0());
		button0.getSource().addClickListener(buttonClickEvent -> {
			module_extLight.getNVLightDimmer()[id].setForce0(!module_extLight.getLightDimmer()[id].isForce0());
			if (module_extLight.getNVLightDimmer()[id].isForce0())
				module_extLight.getNVLightDimmer()[id].setForceMax(false);
			setPendingColor(button0.getSource());
			module_extLight.setReqUpdateValues(true);
		});

		Info info = new Info("intensywność", String.valueOf(module_extLight.getLightDimmer()[id].getIntens()));

		NumberField setIntens = new NumberField("standBy [%]", module_extLight.getLightDimmer()[id].getStandByIntens(), 10, 90, 1);
		setIntens.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLight.getNVLightDimmer()[id].setStandByIntens((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(setIntens.getSource());
			module_extLight.setReqUpdateValues(true);
		});

		NumberField maxIntens = new NumberField("Max [%]", module_extLight.getLightDimmer()[id].getMaxIntens(), 50, 100, 1);
		maxIntens.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLight.getNVLightDimmer()[id].setMaxIntens((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(maxIntens.getSource());
			module_extLight.setReqUpdateValues(true);
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

		NumberField setStartIntensLevel = new NumberField("próg załączenia [%]", module_extLight.getStartLightLevel(), 5, 50, 1);
		setStartIntensLevel.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLight.setNVstartLightLevel((int) Math.round(valueChangeEvent.getValue()));
			setPendingColor(setStartIntensLevel.getSource());
			module_extLight.setReqUpdateValues(true);
		});

		NumberField setOffTimeHour = new NumberField("godzina wyłączenia", module_extLight.getOffTime().getHour(), 0, 23, 1);
		setOffTimeHour.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLight.setNVoffTime(LocalTime.of((int) Math.round(valueChangeEvent.getValue()),module_extLight.getOffTime().getMinute()));
			setPendingColor(setOffTimeHour.getSource());
			module_extLight.setReqUpdateValues(true);
		});
		NumberField setOffTimeMinute = new NumberField("minuta wyłączenia", module_extLight.getOffTime().getMinute(), 0, 59, 1);
		setOffTimeMinute.getSource().addValueChangeListener(valueChangeEvent -> {
			module_extLight.setNVoffTime(LocalTime.of(module_extLight.getOffTime().getHour(), (int) Math.round(valueChangeEvent.getValue())));
			setPendingColor(setOffTimeMinute.getSource());
			module_extLight.setReqUpdateValues(true);
		});

		layout.add(setStartIntensLevel.getSource(), setOffTimeHour.getSource(), setOffTimeMinute.getSource());
		section[1].getTileDetailsContainer(0).add(layout);
	}

	private void update() {
		//Header
		header.setLastUpdate(module_extLight.getFrameLastUpdate());
		header.setDiagnoseUpdate(module_extLight.getDiagnosticLastUpdate());

		for (int i = 0; i < 4; i++) {
			VerticalLayout layout = (VerticalLayout) section[0].getTileDetailsContainer(i).getComponentAt(0);
			com.vaadin.flow.component.button.Button button100 = (com.vaadin.flow.component.button.Button) layout.getComponentAt(0);
			com.vaadin.flow.component.button.Button button0 = (com.vaadin.flow.component.button.Button) layout.getComponentAt(1);
			HorizontalLayout subLayout = (HorizontalLayout) layout.getComponentAt(2);
			Label label = (Label) subLayout.getComponentAt(1);
			com.vaadin.flow.component.textfield.NumberField numberField = (com.vaadin.flow.component.textfield.NumberField) layout.getComponentAt(3);

			System.out.println("update");

		}
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		//Start thread when view active
		thread = new ExtLightView.FeederThread(attachEvent.getUI(), this);
		thread.start();       //On Attach update all components
	}

	@Override
	protected void onDetach(DetachEvent attachEvent) {
		thread.interrupt();
		thread = null;
	}

	private static class FeederThread extends Thread {
		private final UI ui;
		private final ExtLightView view;

		public FeederThread(UI ui, ExtLightView view) {
			this.ui = ui;
			this.view = view;
		}

		@Override
		public void run() {
			while (true) {
				ui.access(view::update);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}

