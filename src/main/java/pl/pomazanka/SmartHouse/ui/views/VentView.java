package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Wentylacja")
@Route(value = "", layout = MainLayout.class)
public class VentView extends VerticalLayout {

    @Autowired
    Module_Vent module_vent;

    public VentView(Module_Vent module_vent) {
        this.module_vent = module_vent;
        ViewComponents viewComponents = new ViewComponents();
        //Create header
        HorizontalLayout header = viewComponents.createHeader(module_vent,"fan.svg");

        add(header);
    }
}
