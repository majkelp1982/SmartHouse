package pl.pomazanka.SmartHouse.backend.dataStruct.Vent;

public class BME280 {
    private double temp;
    private int humidity;
    private int pressure;

    public BME280() {
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }
}

