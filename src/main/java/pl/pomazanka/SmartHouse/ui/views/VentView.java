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
import java.util.Date;
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
    List<VentByHour> actualDiagram = new ArrayList<>();;
    VentByHour[] ventByHour = new VentByHour[24];

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
        for (int i=0; i<24; i++)
            ventByHour[i] = new VentByHour();

        actualDiagram = getActualDiagram();
        grid.addColumn(VentByHour::getHour).setHeader("Godzina");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            int quarterNo = 0;
            boolean result = VentActive.getQuarterStatus(quarterNo);
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),quarterNo));
            if (VentActive.getQuarterActive(quarterNo))
                 info.getNameLabel().getStyle().set("color", "white");
            if (VentActive.getQuarterPending(quarterNo))
                info.getNameLabel().getStyle().set("color", "orange");
             return info.getSource();
        })).setHeader("0-14");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            int quarterNo = 1;
            boolean result = VentActive.getQuarterStatus(quarterNo);
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),quarterNo));
            if (VentActive.getQuarterActive(quarterNo))
                info.getNameLabel().getStyle().set("color", "white");
            if (VentActive.getQuarterPending(quarterNo))
                info.getNameLabel().getStyle().set("color", "orange");
            return info.getSource();
        })).setHeader("15-29");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            int quarterNo = 2;
            boolean result = VentActive.getQuarterStatus(quarterNo);
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),quarterNo));
            if (VentActive.getQuarterActive(quarterNo))
                info.getNameLabel().getStyle().set("color", "white");
            if (VentActive.getQuarterPending(quarterNo))
                info.getNameLabel().getStyle().set("color", "orange");
            return info.getSource();
        })).setHeader("30-44");

        grid.addColumn(new ComponentRenderer<>(VentActive-> {
            int quarterNo = 3;
            boolean result = VentActive.getQuarterStatus(quarterNo);
            String text = result ? "❶" : "⓿";
            Info info = new Info(text,true, result);
            info.getSource().addClickListener(horizontalLayoutClickEvent -> gridListener(VentActive.getHour(),quarterNo));
            if (VentActive.getQuarterActive(quarterNo))
                info.getNameLabel().getStyle().set("color", "white");
            if (VentActive.getQuarterPending(quarterNo))
                info.getNameLabel().getStyle().set("color", "orange");
            return info.getSource();
        })).setHeader("45-59");

        grid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));
        grid.setItems(actualDiagram);
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
        private Quarter[] quarter = new Quarter[4];

        public VentByHour() {
            for (int i=0; i<4; i++)
                quarter[i] = new Quarter();
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public boolean getQuarterStatus(int quarterNo) {
            return quarter[quarterNo].isQuarter();
        }

        public void setQuarterStatus(int quarterNo, boolean status) {
            this.quarter[quarterNo].setActive(false);
            this.quarter[quarterNo].setQuarter(status);
        }

        public boolean getQuarterPending(int quarterNo) {
            return quarter[quarterNo].isPending();
        }

        public void setQuarterPending(int quarterNo, boolean status) {
            this.quarter[quarterNo].setPending(status);
        }

        public boolean getQuarterActive(int quarterNo) {
            return quarter[quarterNo].isActive();
        }

        public void setQuarterActive(int quarterNo, boolean status) {
            this.quarter[quarterNo].setActive(status);
        }
    }

    private List<VentByHour> getActualDiagram () {
        int[] hours = module_vent.getHour();
        actualDiagram.clear();
        for (int i =0; i<12; i++) {
            ventByHour[i*2].setHour(i*2);
            ventByHour[i*2].setQuarterStatus(0,bitStatus(hours[i],7));
            ventByHour[i*2].setQuarterStatus(1,bitStatus(hours[i],6));
            ventByHour[i*2].setQuarterStatus(2,bitStatus(hours[i],5));
            ventByHour[i*2].setQuarterStatus(3,bitStatus(hours[i],4));
            actualDiagram.add(ventByHour[i*2]);
            ventByHour[i*2+1].setHour(i*2+1);
            ventByHour[i*2+1].setQuarterStatus(0,bitStatus(hours[i],3));
            ventByHour[i*2+1].setQuarterStatus(1,bitStatus(hours[i],2));
            ventByHour[i*2+1].setQuarterStatus(2,bitStatus(hours[i],1));
            ventByHour[i*2+1].setQuarterStatus(3,bitStatus(hours[i],0));
            actualDiagram.add(ventByHour[i*2+1]);
        }

        Date currentDate = getCurrentDate();
        int quarterActive;
        if (currentDate.getMinutes()<15) quarterActive=0;
        else if (currentDate.getMinutes()<30) quarterActive=1;
        else if (currentDate.getMinutes()<45) quarterActive=2;
        else    quarterActive = 3;
        ventByHour[currentDate.getHours()].setQuarterActive(quarterActive,true);
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
        //TODO is [orange color stay permanent] must[after value overwritten should change color
        actualDiagram.get(hour).setQuarterPending(quarter,true);
        int[] hours = module_vent.getNVHour();
        if ((hour % 2)>0) quarter += 4;
        quarter = 7-quarter;
        hour = (int)(hour/2);
        switch (hour) {
            case 0 : module_vent.setNVHour01(changeBitStatus(hours[hour],quarter)); break;
            case 1 : module_vent.setNVHour23(changeBitStatus(hours[hour],quarter)); break;
            case 2 : module_vent.setNVHour45(changeBitStatus(hours[hour],quarter)); break;
            case 3 : module_vent.setNVHour67(changeBitStatus(hours[hour],quarter)); break;
            case 4 : module_vent.setNVHour89(changeBitStatus(hours[hour],quarter)); break;
            case 5 : module_vent.setNVHour1011(changeBitStatus(hours[hour],quarter)); break;
            case 6 : module_vent.setNVHour1213(changeBitStatus(hours[hour],quarter)); break;
            case 7 : module_vent.setNVHour1415(changeBitStatus(hours[hour],quarter)); break;
            case 8 : module_vent.setNVHour1617(changeBitStatus(hours[hour],quarter)); break;
            case 9 : module_vent.setNVHour1819(changeBitStatus(hours[hour],quarter)); break;
            case 10 : module_vent.setNVHour2021(changeBitStatus(hours[hour],quarter)); break;
            case 11 : module_vent.setNVHour2223(changeBitStatus(hours[hour],quarter)); break;
        }
        module_vent.setReqUpdateValues(true);
    }

    private void update() {
        //Header
        header.setLastUpdate(module_vent.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_vent.getDiagnosticLastUpdate());

        //Section 0 Status
        info[0][0][0].setValue(module_vent.isFanON());

        //Grid
        actualDiagram = getActualDiagram();
        grid.setItems(actualDiagram);
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
