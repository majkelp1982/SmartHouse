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
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import pl.pomazanka.SmartHouse.ui.views.*;

@CssImport("styles.css")
@Theme(value = Lumo.class, variant = Lumo.DARK)
@Push
public class MainLayout extends AppLayout {

	public MainLayout() {
		createHeader();
		createDrawer();
	}

	private void createHeader() {
		HorizontalLayout header = new HorizontalLayout();
		header.addClassName("appLayout-header");

		H1 logo = new H1("Smart House");
		Button loginButton = new Button("Zaloguj");
		loginButton.addClickListener(buttonClickEvent -> UI.getCurrent().getPage().setLocation("/login"));
		if (View.isUserLoggedIn()) {
			loginButton.setVisible(false);
		}
		header.add(new DrawerToggle(), logo, loginButton);
		header.setSizeFull();
		header.setMinWidth("1000px");
		header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		final boolean touchOptimized = true;
		addToNavbar(touchOptimized, header);
	}

	private void createDrawer() {
		VerticalLayout drawer = new VerticalLayout();
		drawer.setWidth("100%");
		drawer.addClassName("appLayout-drawer");

		//prepare links
		RouterLink weatherViewLink = createDrawerElement("cloud.svg", "Powietrze", WeatherView.class);
		RouterLink ventViewLink = createDrawerElement("recu.svg", "Wentylacja", VentView.class);
		RouterLink ventViewLink2 = createDrawerElement("recu.svg", "Wentylacja2", VentView2.class);
		RouterLink comfortViewLink = createDrawerElement("comfort.svg", "Komfort", ComfortView.class);
		RouterLink heatingViewLink = createDrawerElement("thermometer.svg", "Ogrzewanie", HeatingView.class);
		RouterLink sewageViewLink = createDrawerElement("sewage.svg", "Oczyszczalnia", SewageView.class);
		RouterLink extLightViewLink = createDrawerElement("light-bulb.svg", "Oswietlenie", ExtLightsView.class);
		RouterLink chartsViewLink = createDrawerElement("graph.svg", "Wykresy", ChartsView.class);
		RouterLink errorsViewLink = createDrawerElement("support.svg", "Diagnostyka", DiagnosticView.class);

		drawer.add(weatherViewLink, ventViewLink,  ventViewLink2, comfortViewLink, heatingViewLink, sewageViewLink, extLightViewLink, chartsViewLink, errorsViewLink);
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
		element.add(image, label);

		//create link
		RouterLink routerLink = new RouterLink("", navigationTarget);
		routerLink.getElement().appendChild(element.getElement());

		return routerLink;
	}
}
