package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Weather;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Powietrze")
@Route(value = "Powietrze", layout = MainLayout.class)
public class WeatherView extends View {

    @Autowired
    Module_Weather module_weather;

    //Update thread
    Thread thread;

    //Objects
    Header header;
    Section[] section = new Section[2];
    Info[][][] info = new Info[2][4][3];

    public WeatherView(Module_Weather module_weather) {
        this.module_weather = module_weather;

        //Header
        header = new Header(module_weather, "cloud.svg");
        header.setLastUpdate(module_weather.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_weather.getDiagnosticLastUpdate());

        //Sections
        section[0] = new Section();

        //Create tile for sections
        //Section 0
        section[0].createTile("air.svg", "Jakość");
        section[0].createTile("room.svg", "Parametry");

        //Create sections info/buttons/number fields
        createInfoSection0();

        //Add components to details containers
        for (int i = 0; i < 1; i++)
            for (int j = 0; j < 2; j++)
                for (int k = 0; k < 3; k++)
                    if (info[i][j][k] != null)
                        section[i].getTileDetailsContainer(j).add(info[i][j][k].getSource());

        add(header.getHeader(), section[0].getSection());
    }

    private void createInfoSection0() {
        //Create info's for [section][tileNo][intoNo]
        info[0][0][0] = new Info("PM2,5", "ug/m3", true, true, module_weather.getSds011().getPm25(), 50, 50, 100);
        info[0][0][1] = new Info("PM10", "ug/m3", true, true, module_weather.getSds011().getPm10(), 50, 50, 100);

        info[0][1][0] = new Info("temp", "°C", false, false, module_weather.getBme280().getTemp(), 0, 0, 0);
        info[0][1][1] = new Info("wilgotność", "%", false, false, module_weather.getBme280().getHumidity(), 0, 0, 0);
        info[0][1][2] = new Info("ciśnienie", "hPa", false, false, module_weather.getBme280().getPressure(), 0, 0, 0);
    }

    private void update() {
        //Header
        header.setLastUpdate(module_weather.getFrameLastUpdate());
        header.setDiagnoseUpdate(module_weather.getDiagnosticLastUpdate());

        info[0][0][0].setValue(module_weather.getSds011().getPm25());
        info[0][0][1].setValue(module_weather.getSds011().getPm10());

        info[0][1][0].setValue(module_weather.getBme280().getTemp());
        info[0][1][1].setValue(module_weather.getBme280().getHumidity());
        info[0][1][2].setValue(module_weather.getBme280().getPressure());

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //Start thread when view active
        thread = new WeatherView.FeederThread(attachEvent.getUI(), this);
        thread.start();       //On Attach update all components
    }

    @Override
    protected void onDetach(DetachEvent attachEvent) {
        thread.interrupt();
        thread = null;
    }

    private static class FeederThread extends Thread {
        private final UI ui;
        private final WeatherView view;

        public FeederThread(UI ui, WeatherView view) {
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

