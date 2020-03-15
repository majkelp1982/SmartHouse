package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Komfort")
@Route(value = "Komfort", layout = MainLayout.class)
public class ComfortView extends VerticalLayout {

    @Autowired
    Module_Comfort module_comfort;

    public ComfortView(Module_Comfort module_comfort) {
        this.module_comfort = module_comfort;
        ViewComponents viewComponents = new ViewComponents();
        //Create header
        HorizontalLayout header = viewComponents.createHeader(module_comfort,"comfort.svg");

        add(header);
    }
}
