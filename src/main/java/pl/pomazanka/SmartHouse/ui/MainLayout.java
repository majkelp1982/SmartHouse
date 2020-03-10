package pl.pomazanka.SmartHouse.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import pl.pomazanka.SmartHouse.ui.views.ComfortView;
import pl.pomazanka.SmartHouse.ui.views.HeatingView;
import pl.pomazanka.SmartHouse.ui.views.VentView;

@CssImport("styles.css")
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("appLayout-header");
        H1 logo = new H1("Smart House");
        header.setWidth("100%");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        header.add(new DrawerToggle(), logo);

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.addClassName("appLayout-drawer");
        final String iconHeight = "100px";

        //Images declaration/buttons for navi links
        Image imageVent = new Image("vent.svg", "image Vent");
        imageVent.setHeight(iconHeight);
        Image imageComfort = new Image("comfort.svg", "image Comfort");
        imageComfort.setHeight(iconHeight);
        Image imageHeating = new Image("heating.svg", "image Heating");
        imageHeating.setHeight(iconHeight);
        Button buttonVent = new Button(imageVent);
        buttonVent.setHeight(iconHeight);
        Button buttonComfort = new Button(imageComfort);
        buttonComfort.setHeight(iconHeight);
        Button buttonHeating = new Button(imageHeating);
        buttonHeating.setHeight(iconHeight);

        RouterLink ventViewLink = new RouterLink("", VentView.class);
        ventViewLink.getElement().appendChild(buttonVent.getElement());
        RouterLink comfortViewLink = new RouterLink("", ComfortView.class);
        comfortViewLink.getElement().appendChild(buttonComfort.getElement());
        RouterLink heatingViewLink = new RouterLink("", HeatingView.class);
        heatingViewLink.getElement().appendChild(buttonHeating.getElement());

        ventViewLink.setHighlightCondition(HighlightConditions.sameLocation());
        comfortViewLink.setHighlightCondition(HighlightConditions.sameLocation());
        heatingViewLink.setHighlightCondition(HighlightConditions.sameLocation());

        drawer.add(ventViewLink,comfortViewLink,heatingViewLink);

        addToDrawer(drawer);
    }
}
