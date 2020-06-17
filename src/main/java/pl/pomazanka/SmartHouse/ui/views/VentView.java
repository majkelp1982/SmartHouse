package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
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
public class VentView extends View {

    @Autowired
    Module_Vent module_vent;

    //Update thread
    Thread thread;

    //Objects
    Header header;
    Section[] section = new Section[2];
    Info[][][] info = new Info[2][1][1];
    Grid<VentByHour> grid = new Grid<>();

    public VentView(Module_Vent module_vent) {
        this.module_vent = module_vent;
        //Header
        header = new Header(module_vent, "recu.svg");
        header.setLastUpdate(module_vent.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_vent.getDiagnosticLastUpdate());

        //Sections
        section[0] = new Section();
        section[1] = new Section();

        //Section 0
        section[0].createTile("fan.svg", "Status");

        //Section 1
        section[1].createTile("settings.svg", "Ustawienia");

        //Create sections info/buttons/number fields
        createInfoSection0();
        createInfoSection1();

        section[0].getTileDetailsContainer(0).add(info[0][0][0].getSource());
        section[1].getTileDetailsContainer(0).add(grid);
        section[1].getTileDetailsContainer(0).setWidth("600px");
        section[1].getTileDetailsContainer(0).setHeight("960px");

        // Notification if user doesn't logged
        Notification notification = new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
        section[1].getSection().addClickListener(event -> {
            if (!isUserLoggedIn())
                notification.open();
            });
        section[1].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
        add(header.getHeader(),section[0].getSection(),section[1].getSection());
    }

     private void createInfoSection0() {
         //Status
         info[0][0][0] = new Info("Wentylator",true, module_vent.isFanON());
     }

    private void createInfoSection1() {

        //Ustawienia

        List<VentByHour> ventDiagramList = getActualDiagram();
        grid.addColumn(VentByHour::getHour).setHeader("Godzina");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter1();
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            //FIXME
            info.getNameLabel().getStyle().set("color", "orange");

            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),1));
            return info.getSource();
        })).setHeader("0-14");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter2();
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),2));
            return info.getSource();
        })).setHeader("15-29");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter3();
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),3));
            return info.getSource();
        })).setHeader("30-44");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            boolean result = VentActive.isQuarter4();
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),4));
            return info.getSource();
        })).setHeader("45-59");

        grid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));
        grid.setItems(ventDiagramList);
    }

    private class Quarter {
        private boolean quarter;
        private boolean pending;
        private boolean active;

        public boolean isQuarter() {
            return quarter;
        }

        public void setQuarter(boolean quarter) {
            this.quarter = quarter;
        }

        public boolean isPending() {
            return pending;
        }

        public void setPending(boolean pending) {
            this.pending = pending;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    private class VentByHour {
        private int hour;
        private boolean quarter1;
        private boolean quarter2;
        private boolean quarter3;
        private boolean quarter4;

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
        //TODO need to be programed
        System.out.println("Clicked Hour:"+hour+" quarter:"+quarter);
    }

    private void update() {
        //Header
        header.setLastUpdate(module_vent.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_vent.getDiagnosticLastUpdate());

        //Grid
        List<VentByHour> ventDiagramList = getActualDiagram();
        grid.setItems(ventDiagramList);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new VentView.FeederThread(attachEvent.getUI(), this);
        thread.start();       //On Attach update all components
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        thread.interrupt();
        thread = null;
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final VentView view;

        public FeederThread(UI ui, VentView view ) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    ui.access(view::update);
                    //FIXME instead sleep add newData in all modules structure to respons immediately
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
