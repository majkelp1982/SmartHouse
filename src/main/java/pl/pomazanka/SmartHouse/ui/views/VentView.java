package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Wentylacja")
@Route(value = "", layout = MainLayout.class)
public class VentView extends HorizontalLayout {
    public VentView() {
        H1 logo = new H1("VENT");

        add(logo);

    }
}
