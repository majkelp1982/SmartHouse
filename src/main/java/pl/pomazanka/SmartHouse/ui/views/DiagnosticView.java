package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Smart House | Diagnostyka")
@Route(value = "Diagnostyka", layout = MainLayout.class)
public class DiagnosticView extends View {

    @Autowired
    Diagnostic diagnostic;

    //Update thread
    Thread thread;

    //Objects
    Header header;
    Section[] section = new Section[2];
    Grid<IPAddress> ipAddressGrid = new Grid<>();
    List<IPAddress> ipAddressList = new ArrayList<>();

    public DiagnosticView(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;

        //Header
        header = new Header(diagnostic, "support.svg");
        header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());

        section[0] = new Section();

        //Section 0
        section[0].createTile("place.svg","IP");
        section[0].getTileDetailsContainer(0).add(ipAddressGrid);
        section[0].getTileDetailsContainer(0).setWidth("1000px");
        section[0].getTileDetailsContainer(0).setHeight("300px");

        //Create sections info/buttons/number fields
        createInfoSection0();

        add(header.getHeader(),section[0].getSection());
    }
    private void createInfoSection0() {
        ipAddressList = getActualIPAddressList();

        ipAddressGrid.addColumn(IPAddress::getModuleType).setHeader("Typ");
        ipAddressGrid.addColumn(IPAddress::getModuleName).setHeader("Nazwa modułu");
        ipAddressGrid.addColumn(IPAddress::getIP).setHeader("Adres IP");

        ipAddressGrid.setItems(ipAddressList);
        ipAddressGrid.getColumns().forEach(ventByHourColumn -> ventByHourColumn.setAutoWidth(true));

    }

    private static class IPAddress {
        private int moduleType;
        private String moduleName;
        private int[] IP = new int[4];
        private boolean error = false;

        public IPAddress() {
        }

        public int getModuleType() {
            return moduleType;
        }

        public void setModuleType(int moduleType) {
            this.moduleType = moduleType;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getIP() {
            String ipAddress = IP[0]+"."+IP[1]+"."+IP[2]+"."+IP[3];
            return ipAddress;
        }

        public void setIP(int[] IP) {
            this.IP = IP;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }
    }

    private List<IPAddress> getActualIPAddressList () {
        //FIXME get module List from backend diagnostic list
    }

    private void update() {
        header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());
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