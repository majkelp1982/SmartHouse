package pl.pomazanka.SmartHouse.backend.dataStruct;

import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.BME280;
import pl.pomazanka.SmartHouse.backend.dataStruct.Equipment.SDS011;

@Controller
public class Module_Weather extends Module implements Cloneable {
    //Module weather station type
    private transient static byte MODULE_TYPE = 11;

    private SDS011 sds011 = new SDS011();
    private BME280 bme280 = new BME280();

    public Module_Weather() {
        super(MODULE_TYPE, "Pogoda", "module_weather");
    }

    public SDS011 getSds011() {
        return sds011;
    }

    public BME280 getBme280() {
        return bme280;
    }

    public boolean isAllUpToDate() {
        setUpToDate(true);
        return true;
    }

    //Parser for data package coming via UDP
    public void dataParser(int[] packetData) {
        int controllerFrameNumber = packetData[2];

        switch (controllerFrameNumber) {
            case 0: // standard frame 0
                double tValue = packetData[3] + packetData[4] / 10.0;
                bme280.setTemp(getFloatValue(tValue));
                bme280.setHumidity(packetData[5]);
                int pressure = (packetData[6] << 8) + packetData[7];
                bme280.setPressure(pressure);
                int pm25 = (packetData[8] << 8) + packetData[9];
                sds011.setPm25(pm25);
                int pm10 = (packetData[10] << 8) + packetData[11];
                sds011.setPm10(pm10);
                setFrameLastUpdate(getCurrentDate());
                break;

            case 200: //diagnostic frame
                setDiagnosticLastUpdate(getCurrentDate());
                setIP(new int[]{packetData[3], packetData[4], packetData[5], packetData[6]});
                break;
        }
        if (!isReqUpdateValues()) assignNV();
    }

    private void assignNV() {
    }

    public boolean compare(Module_Weather module_weather) {
        //return FALSE if compare data are different
        boolean result = true;
        if (result) result = cmp(module_weather.bme280.getTemp(), bme280.getTemp(), 0.3);
        if (result) result = cmp(module_weather.bme280.getPressure(), bme280.getPressure(), 3);
        if (result) result = cmp(module_weather.bme280.getHumidity(), bme280.getHumidity(), 2);
        if (result) result = cmp(module_weather.sds011.getPm25(), sds011.getPm25(), 5);
        if (result) result = cmp(module_weather.sds011.getPm10(), sds011.getPm10(), 5);

        if (isTooLongWithoutSave())
            result = false;
        return result;
    }

    @Override
    public Module_Weather clone() throws CloneNotSupportedException {
        Module_Weather module_weather = (Module_Weather) super.clone();
        module_weather.bme280 = (BME280) bme280.clone();
        module_weather.sds011 = (SDS011) sds011.clone();
        return module_weather;
    }
}
