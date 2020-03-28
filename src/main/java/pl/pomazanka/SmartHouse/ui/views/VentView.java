package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Smart House | Wentylacja")
@Route(value = "", layout = MainLayout.class)
public class VentView extends ViewComponents {

    @Autowired
    Module_Vent module_vent;

    public VentView(Module_Vent module_vent) {
        this.module_vent = module_vent;
        ViewComponents viewComponents = new ViewComponents();
        //Create header
        HorizontalLayout header = viewComponents.createHeader(module_vent,"recu.svg");
        // Section 1 - fan
        HorizontalLayout section1 = createSection1();

        // Section 1 - Settings
        HorizontalLayout section2 = createSection2();
        // Notification if user doesn't logged
        Notification notification = new Notification(
                "Brak możliwości zmian ustawień. Zaloguj się.", 3000);
        section2.addClickListener(event -> {
            if (!isUserLoggedIn())
                notification.open();
        });

        add(header,section1,section2);

     }

     private HorizontalLayout createSection1() {
         HorizontalLayout section = new HorizontalLayout();
         HorizontalLayout sectionTile0 = createTile("fan.svg", "Status");

         //Section Tile 0 fan
         VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();
         sectionTile0DetailsContainer.add(addInfo("Wentylator",true, module_vent.isFanON()));
         sectionTile0.add(sectionTile0DetailsContainer);
         section.add(sectionTile0);
         return section;

     }

    private HorizontalLayout createSection2() {
        HorizontalLayout section = new HorizontalLayout();
        HorizontalLayout sectionTile0 = createTile("settings.svg", "Ustawienia");

        //Section Tile 0 Main data
        VerticalLayout sectionTile0DetailsContainer = createDetailsContainer();

        List<VentByHour> ventDiagramList = getActualDiagram();
        Grid<VentByHour> grid = new Grid<>();
        grid.addColumn(VentByHour::getHour).setHeader("Godzina");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter1();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            info.addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),1));
            return info;
        })).setHeader("0-14");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter2();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            info.addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),2));
            return info;
        })).setHeader("15-29");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter3();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            info.addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),3));
            return info;
        })).setHeader("30-44");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter4();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            info.addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),4));
            return info;
        })).setHeader("45-59");

        grid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));
        grid.setItems(ventDiagramList);

        sectionTile0DetailsContainer.add(grid);
        sectionTile0.add(sectionTile0DetailsContainer);
        sectionTile0.setWidth("600px");

        section.add(sectionTile0);
        return section;
    }

    public class VentByHour {
        private int hour;
        private boolean quarter1 = false;
        private boolean quarter2 = false;
        private boolean quarter3 = false;
        private boolean quarter4 = false;

        public VentByHour() {
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public boolean isQuarter1() {
            return quarter1;
        }

        public void setQuarter1(boolean quarter1) {
            this.quarter1 = quarter1;
        }

        public boolean isQuarter2() {
            return quarter2;
        }

        public void setQuarter2(boolean quarter2) {
            this.quarter2 = quarter2;
        }

        public boolean isQuarter3() {
            return quarter3;
        }

        public void setQuarter3(boolean quarter3) {
            this.quarter3 = quarter3;
        }

        public boolean isQuarter4() {
            return quarter4;
        }

        public void setQuarter4(boolean quarter4) {
            this.quarter4 = quarter4;
        }
    }

    private List<VentByHour> getActualDiagram () {
        List<VentByHour> actualDiagram = new ArrayList<>();
        int[] hours = module_vent.getHour();
        VentByHour[] ventByHour = new VentByHour[24];
        for (int i =0; i<12; i++) {
            ventByHour[i*2] = new VentByHour();
            ventByHour[i*2].setHour(i*2);
            ventByHour[i*2].setQuarter1(bitStatus(hours[i],7));
            ventByHour[i*2].setQuarter2(bitStatus(hours[i],6));
            ventByHour[i*2].setQuarter3(bitStatus(hours[i],5));
            ventByHour[i*2].setQuarter4(bitStatus(hours[i],4));
            actualDiagram.add(ventByHour[i*2]);
            ventByHour[i*2+1] = new VentByHour();
            ventByHour[i*2+1].setHour(i*2+1);
            ventByHour[i*2+1].setQuarter1(bitStatus(hours[i],3));
            ventByHour[i*2+1].setQuarter2(bitStatus(hours[i],2));
            ventByHour[i*2+1].setQuarter3(bitStatus(hours[i],1));
            ventByHour[i*2+1].setQuarter4(bitStatus(hours[i],0));
            actualDiagram.add(ventByHour[i*2+1]);
        }
        return actualDiagram;
    }

    private int changeBitStatus(int data, int bitPos) {
        int value = 1;
        for (int i = 0; i<bitPos; i++)
            value = value << 1;
        if (((data >> bitPos) & 1) == 1)
            data -= value;
        else data += value;
        return (data);
    }

    // return bit status from corresponding byte according to position in byte
    private boolean bitStatus(int data, int bytePos) {
        return (((data >> bytePos) & 1) == 1);
    }

    private void gridListener(int hour, int quarter) {
        if (!isUserLoggedIn()) return;
        //TODO
        System.out.println("Clicked Hour:"+hour+" quarter:"+quarter);
    }
}
