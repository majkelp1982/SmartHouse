package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.impl.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.text.SimpleDateFormat;
import java.util.stream.Stream;

@PageTitle("Smart House | Ogrzewanie")
@Route(value = "Ogrzewanie", layout = MainLayout.class)
public class HeatingView extends ViewComponents {

    private FeederThread thread;

    @Autowired
    Module_Heating module_heating;

    //Create header
    private HorizontalLayout header;
    // Section 1 - Buffers
    private HorizontalLayout section1;
    // Section 2 - Status
    private HorizontalLayout section2;
    // Section 3 - Settings
    private HorizontalLayout section3;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new FeederThread(attachEvent.getUI(), this);
        thread.start();
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        //Stop thread when view not active
        //FIXME exception by interruption during thread sleep
        thread.interrupt();
        thread = null;
    }

    public HeatingView(Module_Heating module_heating) {
        this.module_heating = module_heating;

        //Create header
        header = createHeader(module_heating, "thermometer.svg");
        // Section 1 - Buffers
        section1 = createSection1();

        // Section 2 - Status
        section2 = createSection2();

        // Section 3 - Settings
        section3 = createSection3();

        // Notification if user doesn't logged
        Notification notification = new Notification(
                "Brak możliwości zmian ustawień. Zaloguj się.", 3000);
        section3.addClickListener(event -> {
            if (!isUserLoggedIn())
                notification.open();
        });

        // Add all created elements
        header.setMinWidth(section1.getWidth());
        add(header, section1, section2, section3);

    }

    private HorizontalLayout createSection1() {
        //Create tiles
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("cross.svg", "Główne");
        HorizontalLayout sectionTile1 = createTile("thermometer.svg", "Bufor CO");
        HorizontalLayout sectionTile2 = createTile("CWU.svg", "Bufor CWU");
        HorizontalLayout sectionTile3 = createTile("water-distribution.svg", "Podłogówka");

        //Section Tile 0 Main data
        VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();
        sectionTile0DetailsContainer.add(addInfo("źródła", "°C", false, false, module_heating.gettSupply(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("powrót", "°C", false, false, module_heating.gettReturn(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("kominek", "°C", false, false, module_heating.gettFirePlace(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("kolektor", "°C", false, false, module_heating.gettGroundSource(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0.add(sectionTile0DetailsContainer);

        //Section Tile 1 Buffer CO
        VerticalLayout sectionTile1DetailsContainer = createDetailsContainer();
        sectionTile1DetailsContainer.add(addInfo("góra", "°C", module_heating.isHeatingActivated(), false, module_heating.gettBufferCOHigh(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1DetailsContainer.add(addInfo("środek", "°C", module_heating.isHeatingActivated(), false , module_heating.gettBufferCOMid(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1DetailsContainer.add(addInfo("dół", "°C", module_heating.isHeatingActivated(), false, module_heating.gettBufferCODown(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1.add(sectionTile1DetailsContainer);

        //Section Tile 2 Buffer CWU
        VerticalLayout sectionTile2DetailsContainer = createDetailsContainer();
        sectionTile2DetailsContainer.add(addInfo("góra", "°C", true, false, module_heating.gettBufferCWUHigh(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2DetailsContainer.add(addInfo("środek", "°C", true, false, module_heating.gettBufferCWUMid(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2DetailsContainer.add(addInfo("dół", "°C", true, false, module_heating.gettBufferCWUDown(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2.add(sectionTile2DetailsContainer);

        //Section Tile 3 Floor heating water distribution
        VerticalLayout sectionTile3DetailsContainer = createDetailsContainer();
        sectionTile3DetailsContainer.add(addInfo("rozdzielacz", "°C", module_heating.isHeatingActivated(), false, module_heating.gettManifold(), 34, 3, 5));
        sectionTile3DetailsContainer.add(addInfo("powrót parter", "°C", module_heating.isHeatingActivated(), false, module_heating.gettReturnGroundFloor(), 30, 3, 5));
        sectionTile3DetailsContainer.add(addInfo("powrót piętro", "°C" +
                "", module_heating.isHeatingActivated(), false, module_heating.gettReturnLoft(), 30, 3, 5));
        sectionTile3.add(sectionTile3DetailsContainer);

        section.add(sectionTile0, sectionTile1, sectionTile2, sectionTile3);
        return section;
    }

    private HorizontalLayout createSection2() {
        //Create tiles
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("status.svg", "Status");
        HorizontalLayout sectionTile1 = createTile("piston.svg", "Pompy");
        HorizontalLayout sectionTile2 = createTile("heat_circuit.svg", "Strefy");
        String temp;

        //Section Tile 0 Main
        VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();
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
        sectionTile0DetailsContainer.add(addInfo("źródło ciepła", temp));

        double temp1 = (float) module_heating.getValve_bypass() * 2.5;    // scale from 1-40units to 1-100%
        temp = temp1 + "%";
        sectionTile0DetailsContainer.add(addInfo("bypass", temp));

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
        sectionTile0DetailsContainer.add(addInfo("kierunek", temp));
        sectionTile0.add(sectionTile0DetailsContainer);

        //Section Tile 1 Pumps
        VerticalLayout sectionTile1DetailsContainer = createDetailsContainer();
        sectionTile1DetailsContainer.add(addInfo("obieg dom", true, module_heating.isPump_InHouse()));
        sectionTile1DetailsContainer.add(addInfo("obieg ziemia", true, module_heating.isPump_UnderGround()));
        sectionTile1DetailsContainer.add(addInfo("pompa ciepła", true, module_heating.isReqHeatingPumpOn()));
        sectionTile1.add(sectionTile1DetailsContainer);

        //Section Tile 2 Zones
        boolean[] zone = module_heating.getZone();
        VerticalLayout sectionTile2Details1Container = createDetailsContainer();
        VerticalLayout sectionTile2Details2Container = createDetailsContainer();

        sectionTile2Details1Container.add(addInfo("salon", true, zone[0]));
        sectionTile2Details1Container.add(addInfo("pralnia", true, zone[1]));
        sectionTile2Details1Container.add(addInfo("łaź.dół", true, zone[2]));

        sectionTile2Details2Container.add(addInfo("rodzice", true, zone[3]));
        sectionTile2Details2Container.add(addInfo("Natalia", true, zone[4]));
        sectionTile2Details2Container.add(addInfo("Karolina", true, zone[5]));
        sectionTile2Details2Container.add(addInfo("łaź.góra", true, zone[6]));

        sectionTile2.add(sectionTile2Details1Container, sectionTile2Details2Container);

        section.add(sectionTile0, sectionTile1, sectionTile2);
        return section;
    }

    private HorizontalLayout createSection3() {
        //Create tiles
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("settings.svg", "Ustawienia");

        //Section Tile 1 Mode
        VerticalLayout sectionTile1Details1Container = createDetailsContainer();
        Button cheapTariffOnly = addButton("II taryfa",true,module_heating.isCheapTariffOnly());
        Button heatingActivated = addButton("ogrzewanie",true,module_heating.isHeatingActivated());
        Button waterSuperHeat = addButton("gorąca woda",true,module_heating.isWaterSuperheat());

        //Click Listeners
        cheapTariffOnly.addClickListener(buttonClickEvent -> {
            module_heating.setNVCheapTariffOnly(!module_heating.isCheapTariffOnly());
            setPendingColor(cheapTariffOnly);
            module_heating.setReqUpdateValues(true);
        });
        heatingActivated.addClickListener(buttonClickEvent -> {
            module_heating.setNVHeatingActivated(!module_heating.isHeatingActivated());
            setPendingColor(heatingActivated);
            module_heating.setReqUpdateValues(true);
        });
        waterSuperHeat.addClickListener(buttonClickEvent -> {
            module_heating.setNVWaterSuperheat(!module_heating.isWaterSuperheat());
            setPendingColor(waterSuperHeat);
            module_heating.setReqUpdateValues(true);
        });

        //Add components to container
        sectionTile1Details1Container.add(cheapTariffOnly,heatingActivated,waterSuperHeat);

        //Section Tile 1 required temperatures
        VerticalLayout sectionTile1Details2Container = createDetailsContainer();
        NumberField reqTempBufferCO = addNumberField("CO [°C]", module_heating.getReqTempBufferCO(), 35,45,0.5);
        NumberField reqTempBufferCWU = addNumberField("CWU [°C]",module_heating.getReqTempBufferCWU(),40,55,0.5);

        //Click Listeners
        reqTempBufferCO.addValueChangeListener(valueChangeEvent -> {
            module_heating.setNVReqTempBufferCO(valueChangeEvent.getValue());
            setPendingColor(reqTempBufferCO);
            module_heating.setReqUpdateValues(true);
        });
        reqTempBufferCWU.addValueChangeListener(valueChangeEvent -> {
            module_heating.setNVReqTempBufferCWU(valueChangeEvent.getValue());
            setPendingColor(reqTempBufferCWU);
            module_heating.setReqUpdateValues(true);
        });

        //Add components to container
        sectionTile1Details2Container.add(reqTempBufferCO, reqTempBufferCWU);

        sectionTile0.add(sectionTile1Details1Container,sectionTile1Details2Container);

        //  <--!!!settings disabled when user not sign in !!!-->
        sectionTile0.setEnabled(isUserLoggedIn());

        section.add(sectionTile0);

        return section;
    }

    private void ComponentsUpdate() {
        System.out.println("access");
        header.getElement().getChild(2).getChild(1).setText(module_heating.getFrameLastUpdate().toString());
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final HeatingView view;

        public FeederThread(UI ui, HeatingView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            while (true) {

                try {
                    ui.access(() -> view.ComponentsUpdate());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
