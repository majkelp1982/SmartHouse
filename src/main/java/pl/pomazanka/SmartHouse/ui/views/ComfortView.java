package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Komfort")
@Route(value = "Komfort", layout = MainLayout.class)
public class ComfortView extends View {

    @Autowired
    Module_Comfort module_comfort;

    //Update thread
    Thread thread;

    //Objects
    Header header;
    Section[] section = new Section[2];
    Info[][][] info = new Info[2][4][3];
    NumberField[] numberFields = new NumberField[7];
    Module_Comfort.Zone[] zone;

    public ComfortView(Module_Comfort module_comfort) {
        this.module_comfort = module_comfort;
        zone = module_comfort.getZone();

        //Header
        header = new Header(module_comfort, "comfort.svg");
        header.setLastUpdate(module_comfort.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_comfort.getDiagnosticLastUpdate());

        //Sections
        section[0] = new Section();
        section[1] = new Section();

        //Create tile for sections
        //Section 0
        section[0].createTile("room.svg", "salon");
        section[0].createTile("room.svg", "pralnia");
        section[0].createTile("room.svg", "łaź.dół");
        //Section 1
        section[1].createTile("room.svg", "rodzice");
        section[1].createTile("room.svg", "Natalia");
        section[1].createTile("room.svg", "Karolina");
        section[1].createTile("room.svg", "łaź.góra");

        //Create sections info/buttons/number fields
        createInfoSection0();
        createInfoSection1();

        //Add components to details containers
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++)
                for (int k = 0; k < 2; k++)
                    if (info[i][j][k] != null)
                        section[i].getTileDetailsContainer(j).add(info[i][j][k].getSource());
         for (int i=0; i<=2; i++)
            section[0].getTileDetailsContainer(i).add(numberFields[i].getSource());
        for (int i=0; i<=3; i++)
            section[1].getTileDetailsContainer(i).add(numberFields[i+3].getSource());

        //Click Listeners
        numberFields[0].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ0(valueChangeEvent.getValue());
            setPendingColor(numberFields[0].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[1].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ1(valueChangeEvent.getValue());
            setPendingColor(numberFields[1].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[2].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ2(valueChangeEvent.getValue());
            setPendingColor(numberFields[2].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[3].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ3(valueChangeEvent.getValue());
            setPendingColor(numberFields[3].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[4].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ4(valueChangeEvent.getValue());
            setPendingColor(numberFields[4].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[5].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ5(valueChangeEvent.getValue());
            setPendingColor(numberFields[5].getSource());
            module_comfort.setReqUpdateValues(true);
        });
        numberFields[6].getSource().addValueChangeListener(valueChangeEvent -> {
            module_comfort.setNVReqZ6(valueChangeEvent.getValue());
            setPendingColor(numberFields[6].getSource());
            module_comfort.setReqUpdateValues(true);
        });

        // Notification if user doesn't logged
        Notification notification = new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
        for (int i=0; i<=2; i++) {
            section[0].getSection().addClickListener(event -> {
                if (!isUserLoggedIn())
                    notification.open();
            });
            section[0].getTileDetailsContainer(i).setEnabled(isUserLoggedIn());
        }
         for (int i=0; i<=3; i++) {
             section[1].getSection().addClickListener(event -> {
                 if (!isUserLoggedIn())
                     notification.open();
             });
             section[1].getTileDetailsContainer(i).setEnabled(isUserLoggedIn());

         }

        add(header.getHeader(),section[0].getSection(),section[1].getSection());
    }

    private void createInfoSection0 () {
        //Create info's for [section][tileNo][intoNo]
        //Salon
        info[0][0][0] = new Info("temperatura", "°C", true, false, zone[0].isTemp, zone[0].reqTemp, 0.5, 1);
        info[0][0][1] = new Info("wilgotność", "%", true, false, zone[0].isHumidity, 50, 10, 20);
        numberFields[0] = new NumberField("nastawa [°C]",zone[0].reqTemp,18,28,0.5);
        //Pralnia
        info[0][1][0] = new Info("temperatura", "°C", true, false, zone[1].isTemp, zone[0].reqTemp, 0.5, 1);
        info[0][1][1] = new Info("wilgotność", "%", true, false, zone[1].isHumidity, 50, 10, 20);
        numberFields[1] = new NumberField("nastawa [°C]",zone[1].reqTemp,18,28,0.5);
        //laz.dol
        info[0][2][0] = new Info("temperatura", "°C", true, false, zone[2].isTemp, zone[0].reqTemp, 0.5, 1);
        info[0][2][1] = new Info("wilgotność", "%", true, false, zone[2].isHumidity, 50, 10, 20);
        numberFields[2] = new NumberField("nastawa [°C]",zone[2].reqTemp,18,28,0.5);
    }

    private void createInfoSection1 () {
        //Create info's for [section][tileNo][intoNo]
        //rodzice
        info[1][0][0] = new Info("temperatura", "°C", true, false, zone[3].isTemp, zone[0].reqTemp, 0.5, 1);
        info[1][0][1] = new Info("wilgotność", "%", true, false, zone[3].isHumidity, 50, 10, 20);
        numberFields[3] = new NumberField("nastawa [°C]",zone[3].reqTemp,18,28,0.5);
        //Natalia
        info[1][1][0] = new Info("temperatura", "°C", true, false, zone[4].isTemp, zone[0].reqTemp, 0.5, 1);
        info[1][1][1] = new Info("wilgotność", "%", true, false, zone[4].isHumidity, 50, 10, 20);
        numberFields[4] = new NumberField("nastawa [°C]",zone[4].reqTemp,18,28,0.5);
        //Karolina
        info[1][2][0] = new Info("temperatura", "°C", true, false, zone[5].isTemp, zone[0].reqTemp, 0.5, 1);
        info[1][2][1] = new Info("wilgotność", "%", true, false, zone[5].isHumidity, 50, 10, 20);
        numberFields[5] = new NumberField("nastawa [°C]",zone[5].reqTemp,18,28,0.5);
        //laz.gora
        info[1][3][0] = new Info("temperatura", "°C", true, false, zone[6].isTemp, zone[0].reqTemp, 0.5, 1);
        info[1][3][1] = new Info("wilgotność", "%", true, false, zone[6].isHumidity, 50, 10, 20);
        numberFields[6] = new NumberField("nastawa [°C]",zone[6].reqTemp,18,28,0.5);
    }

    private void update() {
        //Header
        header.setLastUpdate(module_comfort.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_comfort.getDiagnosticLastUpdate());

        int k = 0;
        for (int i = 0; i < 2; i++)
            for (int j = 0; j <= 3; j++) {
                if (info[i][j][0] == null) break;
                info[i][j][0].setValue(zone[k].isTemp);
                info[i][j][1].setValue(zone[k].isHumidity);
                k++;
            }

        for (int i = 0; i <= 6; i++)
            numberFields[i].setNumberField(zone[i].reqTemp, zone[i].NVReqTemp);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new ComfortView.FeederThread(attachEvent.getUI(), this);
        thread.start();       //On Attach update all components
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        thread.interrupt();
        thread = null;
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final ComfortView view;

        public FeederThread(UI ui, ComfortView view ) {
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

