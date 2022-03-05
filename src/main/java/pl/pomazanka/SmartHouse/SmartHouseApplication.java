package pl.pomazanka.SmartHouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.pomazanka.SmartHouse.backend.common.Logger;

@SpringBootApplication
public class SmartHouseApplication {
  public static void main(String[] args) {
    Logger.level = Logger.INFO;
    System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
    System.setProperty("sun.net.client.defaultReadTimeout", "5000");
    SpringApplication.run(SmartHouseApplication.class, args);
  }
}
