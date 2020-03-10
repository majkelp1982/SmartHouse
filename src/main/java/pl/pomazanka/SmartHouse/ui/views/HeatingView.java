package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.ui.MainLayout;


@PageTitle("Smart House | Ogrzewanie")
@Route(value = "Ogrzewanie", layout = MainLayout.class)
public class HeatingView extends VerticalLayout {

    @Autowired
    Module_Heating module_heating;

    public HeatingView(Module_Heating module_heating) {
        this.module_heating = module_heating;
        ViewComponents viewComponents = new ViewComponents();
        //Create header
        VerticalLayout header = viewComponents.createHeader(module_heating);

        //Create main information about module
        HorizontalLayout mainModuleView = new HorizontalLayout();


        add(header);


    }
}
