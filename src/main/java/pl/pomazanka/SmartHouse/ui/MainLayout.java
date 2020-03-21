package pl.pomazanka.SmartHouse.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import pl.pomazanka.SmartHouse.ui.views.ComfortView;
import pl.pomazanka.SmartHouse.ui.views.HeatingView;
import pl.pomazanka.SmartHouse.ui.views.VentView;

@CssImport("styles.css")
@Theme(value = Lumo.class, variant = Lumo.DARK)
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
        header.setMinWidth("800px");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button loginButton = new Button("Zaloguj");
        loginButton.addClickListener(buttonClickEvent -> UI.getCurrent().getPage().setLocation("/login"));

        header.add(new DrawerToggle(), logo,loginButton);
        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.setWidth("100%");
        drawer.addClassName("appLayout-drawer");

        //prepare links
        RouterLink ventViewLink = createDrawerElement("fan.svg","Wentylacja", VentView.class);
        RouterLink comfortViewLink = createDrawerElement("comfort.svg","Komfort", ComfortView.class);
        RouterLink heatingViewLink = createDrawerElement("thermometer.svg","Ogrzewaine", HeatingView.class);

        drawer.add(ventViewLink,comfortViewLink,heatingViewLink);
        drawer.setSizeFull();
        drawer.setAlignItems(FlexComponent.Alignment.START);

        addToDrawer(drawer);
    }

    public RouterLink createDrawerElement(String imageName, String name, Class<? extends Component> navigationTarget) {
        //create container for elements
        HorizontalLayout element = new HorizontalLayout();
        element.setAlignItems(FlexComponent.Alignment.CENTER);

        //create elements
        Image image = new Image(imageName, imageName);
        image.setHeight("50px");
        Label label = new Label(name);
        label.getStyle().set("color", "grey");

        //add to horizontal layout
        element.add(image,label);

        //create link
        RouterLink routerLink = new RouterLink("", navigationTarget);
        routerLink.getElement().appendChild(element.getElement());

        return routerLink;
    }
}
