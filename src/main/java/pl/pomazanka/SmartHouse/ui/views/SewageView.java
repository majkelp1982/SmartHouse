package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Sewage;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Sewage;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Oczyszczalnia")
@Route(value = "Oczyszczalnia", layout = MainLayout.class)
public class SewageView extends View {

    @Autowired
    Module_Sewage module_sewage;

    //Update thread
    Thread thread;

    //Objects
    Header header;
    Section[] section = new Section[1];
    Info[][][] info = new Info[1][2][4];
    NumberField maxLevel;
    NumberField minLevel;
    NumberField refZero;
    NumberField interwal;


    public SewageView(Module_Sewage module_sewage) {
        this.module_sewage = module_sewage;

        //Header
        header = new Header(module_sewage, "sewage.svg");
        header.setLastUpdate(module_sewage.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_sewage.getDiagnosticLastUpdate());

        //Sections
        section[0] = new Section();

        //Create tile for sections
        //Section 0
        section[0].createTile("sewage.svg", "Status");
        section[0].createTile("settings.svg", "Ustawienia");

        //Create sections info/buttons/number fields
        createInfoSection0();

        section[0].getTileDetailsContainer(0).add(info[0][0][0].getSource());
        section[0].getTileDetailsContainer(0).add(info[0][0][1].getSource());
        section[0].getTileDetailsContainer(0).add(info[0][0][2].getSource());
        section[0].getTileDetailsContainer(0).add(info[0][0][3].getSource());

        section[0].getTileDetailsContainer(1).add(maxLevel.getSource());
        section[0].getTileDetailsContainer(1).add(minLevel.getSource());
        section[0].getTileDetailsContainer(1).add(refZero.getSource());
        section[0].getTileDetailsContainer(1).add(interwal.getSource());

        add(header.getHeader(), section[0].getSection());
    }

    private void createInfoSection0() {
        //Status
        info[0][0][0] = new Info("pompa powietrza", true, module_sewage.isAirPump());
        info[0][0][1] = new Info("pompa wody", true, module_sewage.isWaterPump());
        info[0][0][2] = new Info("limit sensor", true, module_sewage.isLimitSensor());
        info[0][0][3] = new Info("aktualny poziom", "cm", true, false, module_sewage.getIsWaterLevel(), -20, -10, -5);

        maxLevel = new NumberField("max poziom wody[cm]", module_sewage.getMaxWaterLevel(), -50, -5, 5);
        maxLevel.getSource().addValueChangeListener(valueChangeEvent -> {
            module_sewage.setNVmaxWaterLevel((int) Math.round(valueChangeEvent.getValue()));
            setPendingColor(maxLevel.getSource());
            module_sewage.setReqUpdateValues(true);
        });

        minLevel = new NumberField("min poziom wody[cm]", module_sewage.getMinWaterLevel(), -100, -10, 5);
        minLevel.getSource().addValueChangeListener(valueChangeEvent -> {
            module_sewage.setNVminWaterLevel((int) Math.round(valueChangeEvent.getValue()));
            setPendingColor(minLevel.getSource());
            module_sewage.setReqUpdateValues(true);
        });

        refZero = new NumberField("referencja ZERO[cm]", module_sewage.getZeroRefWaterLevel(), -200, 0, 1);
        refZero.getSource().addValueChangeListener(valueChangeEvent -> {
            module_sewage.setNVZeroRefWaterLevel((int) Math.round(valueChangeEvent.getValue()));
            setPendingColor(refZero.getSource());
            module_sewage.setReqUpdateValues(true);
        });

        interwal = new NumberField(" interwał napowietrzania[min]", module_sewage.getIntervalAirPump(), 5, 30, 1);
        interwal.getSource().addValueChangeListener(valueChangeEvent -> {
            module_sewage.setNVIntervalAirPump((int) Math.round(valueChangeEvent.getValue()));
            setPendingColor(interwal.getSource());
            module_sewage.setReqUpdateValues(true);
        });
    }

    private void update() {
        //Header
        header.setLastUpdate(module_sewage.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_sewage.getDiagnosticLastUpdate());

        info[0][0][0].setValue(module_sewage.isAirPump());
        info[0][0][1].setValue(module_sewage.isWaterPump());
        info[0][0][2].setValue(module_sewage.isLimitSensor());
        info[0][0][3].setValue(module_sewage.getIsWaterLevel());

        maxLevel.setNumberField(module_sewage.getMaxWaterLevel(), module_sewage.getNVmaxWaterLevel());
        minLevel.setNumberField(module_sewage.getMinWaterLevel(), module_sewage.getNVminWaterLevel());
        refZero.setNumberField(module_sewage.getZeroRefWaterLevel(), module_sewage.getNVZeroRefWaterLevel());
        interwal.setNumberField(module_sewage.getIntervalAirPump(), module_sewage.getNVIntervalAirPump());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new SewageView.FeederThread(attachEvent.getUI(), this);
        thread.start();       //On Attach update all components
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        thread.interrupt();
        thread = null;
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final SewageView view;

        public FeederThread(UI ui, SewageView view) {
            this.ui = ui;
            this.view = view;
        }

        @Override
        public void run() {
            while (true) {
                ui.access(view::update);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

