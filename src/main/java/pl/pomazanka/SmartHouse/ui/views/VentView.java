package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        HorizontalLayout header = viewComponents.createHeader(module_vent,"fan.svg");

        HorizontalLayout ventActiveGrid = new HorizontalLayout();

        //FIXME swap to proper list (not test list)
        List<VentByHour> ventDiagram = new ArrayList<>();
        ventDiagram = getActualDiagram();

        Grid<VentByHour> grid = new Grid<>();
        grid.addColumn(VentByHour::getHour).setHeader("Godzina");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter1();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            return info;
        })).setHeader("Kwadrans 0-14");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter2();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            return info;
        })).setHeader("Kwadrans 15-29");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter3();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            return info;
        })).setHeader("Kwadrans 30-44");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter4();
            String text = result ? "Wł" : "Wył";
            HorizontalLayout info = addInfo(text,true, result);
            return info;
        })).setHeader("Kwadrans 45-59");

        grid.setItems(ventDiagram);

        grid.addItemClickListener(ventActiveItemClickEvent -> {
            System.out.println(ventActiveItemClickEvent.getItem());
        });

        grid.addItemClickListener(ventActiveItemClickEvent -> {
            //TODO reaction on cell clicked
            VentByHour item = ventActiveItemClickEvent.getItem();

            System.out.println("hour:"+item.getHour()+" column : "+ventActiveItemClickEvent.getColumn());
        });

        ventActiveGrid.add(grid);
        ventActiveGrid.setSizeFull();

        add(header,ventActiveGrid);
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
        if (((data >> bytePos) & 1) == 1) return true;
        else return false;
    }
}
