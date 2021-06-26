package pl.pomazanka.SmartHouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.pomazanka.SmartHouse.backend.common.Logger;

@SpringBootApplication
public class SmartHouseApplication {
	public static void main(String[] args) {
		Logger.level = Logger.DEBUG;
		SpringApplication.run(SmartHouseApplication.class, args);
	}
}
