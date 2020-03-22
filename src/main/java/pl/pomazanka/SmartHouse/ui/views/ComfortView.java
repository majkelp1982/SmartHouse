package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Komfort")
@Route(value = "Komfort", layout = MainLayout.class)
public class ComfortView extends ViewComponents {

    @Autowired
    Module_Comfort module_comfort;

    public ComfortView(Module_Comfort module_comfort) {
        this.module_comfort = module_comfort;
        //Create header
        HorizontalLayout header = createHeader(module_comfort,"comfort.svg");
        //Section 1
        HorizontalLayout section1 = createSection1();
        //Section 2
        HorizontalLayout section2 = createSection2();

        // Notification if user doesn't logged
        Notification notification = new Notification(
                "Nie jesteś zalogowany. Brak możliwości zmian ustawień.", 3000);
        section1.addClickListener(event -> {
            if (!isUserLoggedIn())
                notification.open();
        });
        section2.addClickListener(event -> {
            if (!isUserLoggedIn())
                notification.open();
        });

        add(header,section1,section2);
    }

    private HorizontalLayout createSection1() {
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("room.svg", "Salon");
        HorizontalLayout sectionTile1 = createTile("room.svg", "Pralnia");
        HorizontalLayout sectionTile2 = createTile("room.svg", "Łaź.dół");

        Module_Comfort.Zone[] zone = module_comfort.getZone();

        //Section Tile 0 Main data
        VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();
        sectionTile0DetailsContainer.add(addInfo("temperatura", "stC", true, zone[0].isTemp, zone[0].reqTemp, 1, 2));
        sectionTile0DetailsContainer.add(addInfo("wilgotność", "%", true, zone[0].isHumidity, 50, 10, 20));
        sectionTile0DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile0.add(sectionTile0DetailsContainer);

        //Section Tile 1 Main data
        VerticalLayout sectionTile1DetailsContainer = createDetailsContainer();
        sectionTile1DetailsContainer.add(addInfo("temperatura", "stC", true, zone[1].isTemp, zone[1].reqTemp, 1, 2));
        sectionTile1DetailsContainer.add(addInfo("wilgotność", "%", true, zone[1].isHumidity, 50, 10, 20));
        sectionTile1DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile1.add(sectionTile1DetailsContainer);

        //Section Tile 2 Main data
        VerticalLayout sectionTile2DetailsContainer = createDetailsContainer();
        sectionTile2DetailsContainer.add(addInfo("temperatura", "stC", true, zone[2].isTemp, zone[2].reqTemp, 1, 2));
        sectionTile2DetailsContainer.add(addInfo("wilgotność", "%", true, zone[2].isHumidity, 50, 10, 20));
        sectionTile2DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile2.add(sectionTile2DetailsContainer);

        section.add(sectionTile0,sectionTile1,sectionTile2);

        //  <--!!!settings disabled when user not sign in !!!-->
        sectionTile0.setEnabled(isUserLoggedIn());
        sectionTile1.setEnabled(isUserLoggedIn());
        sectionTile2.setEnabled(isUserLoggedIn());

        return section;
    }

    private HorizontalLayout createSection2() {
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("room.svg", "Rodzice");
        HorizontalLayout sectionTile1 = createTile("room.svg", "Natalia");
        HorizontalLayout sectionTile2 = createTile("room.svg", "Karolina");
        HorizontalLayout sectionTile3 = createTile("room.svg", "Łaź.góra");

        Module_Comfort.Zone[] zone = module_comfort.getZone();

        //Section Tile 0 Main data
        VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();
        sectionTile0DetailsContainer.add(addInfo("temperatura", "stC", true, zone[3].isTemp, zone[3].reqTemp, 1, 2));
        sectionTile0DetailsContainer.add(addInfo("wilgotność", "%", true, zone[3].isHumidity, 50, 10, 20));
        sectionTile0DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile0.add(sectionTile0DetailsContainer);

        //Section Tile 1 Main data
        VerticalLayout sectionTile1DetailsContainer = createDetailsContainer();
        sectionTile1DetailsContainer.add(addInfo("temperatura", "stC", true, zone[4].isTemp, zone[4].reqTemp, 1, 2));
        sectionTile1DetailsContainer.add(addInfo("wilgotność", "%", true, zone[4].isHumidity, 50, 10, 20));
        sectionTile1DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile1.add(sectionTile1DetailsContainer);

        //Section Tile 2 Main data
        VerticalLayout sectionTile2DetailsContainer = createDetailsContainer();
        sectionTile2DetailsContainer.add(addInfo("temperatura", "stC", true, zone[5].isTemp, zone[5].reqTemp, 1, 2));
        sectionTile2DetailsContainer.add(addInfo("wilgotność", "%", true, zone[5].isHumidity, 50, 10, 20));
        sectionTile2DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile2.add(sectionTile2DetailsContainer);

        //Section Tile 3 Main data
        VerticalLayout sectionTile3DetailsContainer = createDetailsContainer();
        sectionTile3DetailsContainer.add(addInfo("temperatura", "stC", true, zone[6].isTemp, zone[6].reqTemp, 1, 2));
        sectionTile3DetailsContainer.add(addInfo("wilgotność", "%", true, zone[6].isHumidity, 50, 10, 20));
        sectionTile3DetailsContainer.add(addNumberField("nastawa",21,18,28,0.5));
        sectionTile3.add(sectionTile3DetailsContainer);

        section.add(sectionTile0,sectionTile1,sectionTile2,sectionTile3);

        sectionTile0.setEnabled(isUserLoggedIn());
        sectionTile1.setEnabled(isUserLoggedIn());
        sectionTile2.setEnabled(isUserLoggedIn());
        sectionTile3.setEnabled(isUserLoggedIn());

        return section;
    }


}
