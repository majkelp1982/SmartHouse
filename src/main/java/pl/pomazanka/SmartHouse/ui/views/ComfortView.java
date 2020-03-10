package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Komfort")
@Route(value = "Komfort", layout = MainLayout.class)
public class ComfortView extends HorizontalLayout {
    public ComfortView() {
        H1 logo = new H1("Komfort");

        add(logo);
    }
}
