package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.html.Image;
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
        HorizontalLayout header = viewComponents.createHeader(module_heating,"heating.svg");

        //Section 1 Buffer CO
        VerticalLayout section1DetailsContainer = viewComponents.createDetailsContainer();
        section1DetailsContainer.add(viewComponents.addDetails("góra","stC",true,module_heating.gettBufferCOHigh(),module_heating.getReqTempBufferCO(),3,5));
        section1DetailsContainer.add(viewComponents.addDetails("środek","stC",true,module_heating.gettBufferCOMid(),module_heating.getReqTempBufferCO(),3,5));
        section1DetailsContainer.add(viewComponents.addDetails("dół","stC",true,module_heating.gettBufferCODown(),module_heating.getReqTempBufferCO(),3,5));

        HorizontalLayout section1 = viewComponents.createTile("heater.svg","Bufor CO");
        section1.add(section1DetailsContainer);

        //Section 2 Buffer CWU
        VerticalLayout section2DetailsContainer = viewComponents.createDetailsContainer();
        section2DetailsContainer.add(viewComponents.addDetails("góra","stC",true,module_heating.gettBufferCWUHigh(),module_heating.getReqTempBufferCWU(),3,5));
        section2DetailsContainer.add(viewComponents.addDetails("środek","stC",true,module_heating.gettBufferCWUMid(),module_heating.getReqTempBufferCWU(),3,5));
        section2DetailsContainer.add(viewComponents.addDetails("dół","stC",true,module_heating.gettBufferCWUDown(),module_heating.getReqTempBufferCWU(),3,5));

        HorizontalLayout section2 = viewComponents.createTile("heater.svg","Bufor CWU");
        section2.add(section2DetailsContainer);


        add(header,section1,section2);
    }
}
