package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@PageTitle("Smart House | Diagnostyka")
@Route(value = "Diagnostyka", layout = MainLayout.class)
public class DiagnosticView extends View {

    @Autowired
    Diagnostic diagnostic;

    //Update thread
    Thread thread;

    //Objects
    private Header header;
    private Section[] section = new Section[2];
    private Grid<Diagnostic.ModuleDiagInfo> moduleGrid = new Grid<>();
    private List<Diagnostic.ModuleDiagInfo> moduleList = new ArrayList<>();
    private Button globalResetButton;
    private Button groupButton;

    Grid<Diagnostic.ModuleFault> faultGrid = new Grid<>();
    List<Diagnostic.ModuleFault> globalFaultList = new ArrayList<>();

    public DiagnosticView(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;

        //Header
        header = new Header(diagnostic, "support.svg");
        header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());

        section[0] = new Section();
        section[1] = new Section();

        //Create sections info/buttons/number fields
        createInfoSection0();
        createInfoSection1();

        //Section 0
        section[0].createTile("place.svg","IP");
        section[0].getTileDetailsContainer(0).add(moduleGrid);
        section[0].getTileDetailsContainer(0).setWidth("1000px");
        section[0].getTileDetailsContainer(0).setHeight("300px");
        section[0].getTileDetailsContainer(0).setHeight((moduleList.size()*30+120)+"px");

        //Section 1
        section[1].createTile("support.svg","Błędy");
        section[1].getTileDetailsContainer(0).add(new HorizontalLayout(globalResetButton.getSource(), groupButton.getSource()), faultGrid);
        section[1].getTileDetailsContainer(0).setMinHeight("100px");
        if (diagnostic.isGlobalFaultsListGroupByFault()) section[1].getTileDetailsContainer(0).setMinWidth("1600px");
        else section[1].getTileDetailsContainer(0).setMinWidth("1200px");
        section[1].getTileDetailsContainer(0).setHeight((globalFaultList.size()*35+180)+"px");

        add(header.getHeader(),section[0].getSection(),section[1].getSection());
    }
    private void createInfoSection0() {
        moduleList = diagnostic.getModules();
        moduleGrid.addColumn(Diagnostic.ModuleDiagInfo::getModuleType).setHeader("Typ");
        moduleGrid.addColumn(Diagnostic.ModuleDiagInfo::getModuleName).setHeader("Nazwa modułu");
        moduleGrid.addColumn(Diagnostic.ModuleDiagInfo::getIP).setHeader("Adres IP");

        moduleGrid.setItems(moduleList);
        moduleGrid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));
    }

    private void createInfoSection1() {
        globalFaultList = diagnostic.getGlobalFaultsList();
        faultGrid.addColumn(Diagnostic.ModuleFault::getModuleType).setHeader("Typ").setAutoWidth(true).setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getModuleName).setHeader("Nazwa modułu").setAutoWidth(true).setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getIndex).setHeader("Index").setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getDescription).setHeader("Opis").setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getIncomingToString).setHeader("Początek").setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getOutgoingToString).setHeader("Koniec").setSortable(true);
        faultGrid.addColumn(Diagnostic.ModuleFault::getActiveTime).setHeader("czas trwania[s]").setKey("czas").setSortable(true);
        if (diagnostic.isGlobalFaultsListGroupByFault()) {
            faultGrid.addColumn(Diagnostic.ModuleFault::getNumberOfErrors).setHeader("liczba wystąpień").setKey("liczba").setSortable(true);
            faultGrid.getColumnByKey("czas").setHeader("łączny czas trwania[s]");

        }

        faultGrid.setItems(globalFaultList);
        faultGrid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));

        globalResetButton = new Button("Potwierdź",false,false);
        globalResetButton.getSource().addClickListener(event -> diagnostic.resetGlobalList());

        groupButton = new Button("Grupuj", true, diagnostic.isGlobalFaultsListGroupByFault());
        groupButton.getSource().addClickListener(event -> {
            diagnostic.globalFaultsListGroupByFault();
            if (diagnostic.isGlobalFaultsListGroupByFault()) {
                faultGrid.addColumn(Diagnostic.ModuleFault::getNumberOfErrors).setHeader("liczba wystąpień").setKey("liczba").setSortable(true);
                faultGrid.getColumnByKey("czas").setHeader("łączny czas trwania[s]");
            }
            else {
                faultGrid.removeColumnByKey("liczba");
                faultGrid.getColumnByKey("czas").setHeader("czas trwania[s]");
            }
            update();
        });
    }

    private void update() {
        header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());
        groupButton.setButtonColor(diagnostic.isGlobalFaultsListGroupByFault(),diagnostic.isGlobalFaultsListGroupByFault());
        diagnostic.refreshGlobalFaultList();
        faultGrid.setItems(diagnostic.getGlobalFaultsList());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new FeederThread(attachEvent.getUI(), this);
        thread.start();       //On Attach update all components
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        thread.interrupt();
        thread = null;
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final DiagnosticView view;

        public FeederThread(UI ui, DiagnosticView view ) {
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