package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Ogrzewanie")
@Route(value = "Ogrzewanie", layout = MainLayout.class)
public class HeatingView extends ViewComponents {

    @Autowired
    Module_Heating module_heating;

    public HeatingView(Module_Heating module_heating) {
        this.module_heating = module_heating;
        //Create header
        HorizontalLayout header = createHeader(module_heating, "thermometer.svg");
        // Section 1 - Buffers
        HorizontalLayout section1 = createSection1();

        // Section 2 - Status
        HorizontalLayout section2 = createSection2();

        // Section 3 - Settings
        HorizontalLayout section3 = createSection3();

        // Notification if user doesn't logged
        Notification notification = new Notification(
                "Nie jesteś zalogowany. Brak możliwości zmian ustawień.", 3000);
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
        sectionTile0DetailsContainer.add(addInfo("źródła", "stC", false, module_heating.gettSupply(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("powrót", "stC", false, module_heating.gettReturn(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("kominek", "stC", false, module_heating.gettFirePlace(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0DetailsContainer.add(addInfo("kolektor", "stC", false, module_heating.gettGroundSource(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile0.add(sectionTile0DetailsContainer);

        //Section Tile 1 Buffer CO
        VerticalLayout sectionTile1DetailsContainer = createDetailsContainer();
        sectionTile1DetailsContainer.add(addInfo("góra", "stC", module_heating.isHeatingActivated(), module_heating.gettBufferCOHigh(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1DetailsContainer.add(addInfo("środek", "stC", module_heating.isHeatingActivated(), module_heating.gettBufferCOMid(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1DetailsContainer.add(addInfo("dół", "stC", module_heating.isHeatingActivated(), module_heating.gettBufferCODown(), module_heating.getReqTempBufferCO(), 3, 5));
        sectionTile1.add(sectionTile1DetailsContainer);

        //Section Tile 2 Buffer CWU
        VerticalLayout sectionTile2DetailsContainer = createDetailsContainer();
        sectionTile2DetailsContainer.add(addInfo("góra", "stC", true, module_heating.gettBufferCWUHigh(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2DetailsContainer.add(addInfo("środek", "stC", true, module_heating.gettBufferCWUMid(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2DetailsContainer.add(addInfo("dół", "stC", true, module_heating.gettBufferCWUDown(), module_heating.getReqTempBufferCWU(), 3, 5));
        sectionTile2.add(sectionTile2DetailsContainer);

        //Section Tile 3 Floor heating water distribution
        VerticalLayout sectionTile3DetailsContainer = createDetailsContainer();
        sectionTile3DetailsContainer.add(addInfo("rozdzielacz", "stC", module_heating.isHeatingActivated(), module_heating.gettManifold(), 34, 3, 5));
        sectionTile3DetailsContainer.add(addInfo("powrót parter", "stC", module_heating.isHeatingActivated(), module_heating.gettReturnGroundFloor(), 30, 3, 5));
        sectionTile3DetailsContainer.add(addInfo("powrót piętro", "stC", module_heating.isHeatingActivated(), module_heating.gettReturnLoft(), 30, 3, 5));
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
        sectionTile1Details1Container.add(addButton("II taryfa",true,module_heating.isCheapTariffOnly()));
        sectionTile1Details1Container.add(addButton("ogrzewanie",true,module_heating.isHeatingActivated()));
        sectionTile1Details1Container.add(addButton("gorąca woda",true,module_heating.isWaterSuperheat()));

        //Section Tile 1 required temperatures
        VerticalLayout sectionTile1Details2Container = createDetailsContainer();
        sectionTile1Details2Container.add(addNumberField("CO", module_heating.getReqTempBufferCO(), 35,45,0.5));
        sectionTile1Details2Container.add(addNumberField("CWU",module_heating.getReqTempBufferCWU(),40,55,0.5));

        sectionTile0.add(sectionTile1Details1Container,sectionTile1Details2Container);

        //  <--!!!settings disabled when user not sign in !!!-->
        sectionTile0.setEnabled(isUserLoggedIn());

        section.add(sectionTile0);

        return section;
    }
}
