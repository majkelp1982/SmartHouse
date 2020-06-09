package pl.pomazanka.SmartHouse.ui.views;

import com.mongodb.internal.connection.IndexMap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Ogrzewanie")
@Route(value = "Ogrzewanie", layout = MainLayout.class)
public class Heating_View_new extends View {

    @Autowired
    Module_Heating module_heating;

    //Objects
    Header header;
    Section[] section = new Section[3];
    Info[][] info = new Info[3][4];


    public Heating_View_new(Module_Heating module_heating) {
        this.module_heating = module_heating;

        //Header
        header = new Header(module_heating,"thermometer.svg");
        header.setLastUpdate(module_heating.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_heating.getDiagnosticLastUpdate());

        //Sections
        section[0] = new Section();
        section[1] = new Section();
        section[2] = new Section();

        //Create tile for section 0
        section[0].createTile("cross.svg", "Główne");
        section[0].createTile("thermometer.svg", "Bufor CO");
        section[0].createTile("CWU.svg", "Bufor CWU");
        section[0].createTile("water-distribution.svg", "Podłogówka");

        //Create info's for section 0
        info[0][0] = new Info("źródła", "°C", false, false, module_heating.gettSupply(), module_heating.getReqTempBufferCO(), 3, 5);
        info[0][1] = new Info("powrót", "°C", false, false, module_heating.gettReturn(), module_heating.getReqTempBufferCO(), 3, 5);
        info[0][2] = new Info("kominek", "°C", false, false, module_heating.gettFirePlace(), module_heating.getReqTempBufferCO(), 3, 5);
        info[0][3] = new Info("kolektor", "°C", false, false, module_heating.gettGroundSource(), module_heating.getReqTempBufferCO(), 3, 5);

        section[0].getTileDetailsContainer

        add(header.getHeader(),section[0].getSection(),section[1].getSection(),section[2].getSection());
    }

}
