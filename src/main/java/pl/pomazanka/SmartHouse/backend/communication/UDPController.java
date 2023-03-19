package pl.pomazanka.SmartHouse.backend.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.common.Logger;
import pl.pomazanka.SmartHouse.backend.dataStruct.*;
import pl.pomazanka.SmartHouse.backend.dataStruct.Substructures.VentZones;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

@Controller
public class UDPController {

  public static final int PACKET_SIZE_MODULE_13 =
      93; // length of UDP data from module 3 "wentylacja"
  public static final int PACKET_SIZE_MODULE_13_DIAG =
      8; // length of UDP diagnose from module 3 "wentylacja"
  private static final int BUFFER_SIZE = 128;
  private static final int PACKET_SIZE_MODULE_10 =
      31; // length of UDP data from module 10 "komfort"
  private static final int PACKET_SIZE_MODULE_10_DIAG =
      8; // length of UDP diagnose from module 10 "komfort"
  private static final int PACKET_SIZE_MODULE_11 = 13; // length of UDP data from module 11 "pogoda"
  private static final int PACKET_SIZE_MODULE_11_DIAG =
      8; // length of UDP diagnose from module 11 "pogoda"
  private static final int PACKET_SIZE_MODULE_12 =
      9; // length of UDP data from module 12 "oczyszczalnia"
  private static final int PACKET_SIZE_MODULE_12_DIAG =
      8; // length of UDP diagnose from module 12 "oczyszczalnia"
  private static final int PACKET_SIZE_MODULE_14 =
      22; // length of UDP data from module 14 "Ogrzewanie"
  private static final int PACKET_SIZE_MODULE_14_DIAG =
      8; // length of UDP diagnose from module 14 "Ogrzewanie"
  private static final int PACKET_SIZE_MODULE_16 =
      19; // length of UDP data from module 16 "Oświetlenie zewnętrzne"
  private static final int PACKET_SIZE_MODULE_16_DIAG =
      8; // length of UDP diagnose from module 16 "Oświetlenie zewnętrzne"
  private static MulticastSocket multiSocket;
  private static byte[] buffer;
  private static int[] packetData;
  private final int moduleMain = 1;
  // Wired classes
  @Autowired MongoDBController mongoDBController;
  @Autowired Module_Heating module_heating;
  @Autowired Module_Comfort module_comfort;
  @Autowired Module_Vent module_vent;
  @Autowired Module_Weather module_weather;
  @Autowired Module_Sewage module_sewage;
  @Autowired Module_ExtLights module_extLights;
  int localPort = 6000;
  Thread UDPThread = new Thread(new UDPListener());
  // UDP variables
  private int timeSynchroLast = 100;

  public UDPController() {
    try {
      multiSocket = new MulticastSocket(localPort);
      multiSocket.setBroadcast(true);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    UDPThread.start();
    // Set new thread
    new Thread(new EventRunBackground()).start();
  }

  private boolean packetDataCorrect(final int[] packetData, final int packetLength) {
    boolean packetCorrect = false;
    if (packetData[2] == 200) { // Only for diagnose frames
      switch (packetData[0]) {
        case 10:
          if (packetLength == PACKET_SIZE_MODULE_10_DIAG) {
            packetCorrect = true;
          }
          break;
        case 11:
          if (packetLength == PACKET_SIZE_MODULE_11_DIAG) {
            packetCorrect = true;
          }
          break;
        case 12:
          if (packetLength == PACKET_SIZE_MODULE_12_DIAG) {
            packetCorrect = true;
          }
          break;
        case 13:
          if (packetLength == PACKET_SIZE_MODULE_13_DIAG) {
            packetCorrect = true;
          }
          break;
        case 14:
          if (packetLength == PACKET_SIZE_MODULE_14_DIAG) {
            packetCorrect = true;
          }
          break;
        case 16:
          if (packetLength == PACKET_SIZE_MODULE_16_DIAG) {
            packetCorrect = true;
          }
          break;
        default:
          System.out.println("Wrong data format : module[" + packetData[0] + "]");
      }
    } else {
      switch (packetData[0]) { // Only for standard frames
        case 1:
          packetCorrect = true;
          break;
        case 10:
          if (packetLength == PACKET_SIZE_MODULE_10) {
            packetCorrect = true;
          }
          break;
        case 11:
          if (packetLength == PACKET_SIZE_MODULE_11) {
            packetCorrect = true;
          }
          break;
        case 12:
          if (packetLength == PACKET_SIZE_MODULE_12) {
            packetCorrect = true;
          }
          break;
        case 13:
          if (packetLength == PACKET_SIZE_MODULE_13) {
            packetCorrect = true;
          }
          break;
        case 14:
          if (packetLength == PACKET_SIZE_MODULE_14) {
            packetCorrect = true;
          }
          break;
        case 16:
          if (packetLength == PACKET_SIZE_MODULE_16) {
            packetCorrect = true;
          }
          break;
        default:
          System.out.println("Wrong data format : module[" + packetData[0] + "]");
      }
    }
    if (!packetCorrect) {
      mongoDBController.saveNotice(
          "UDP",
          "Niekompletny pakiet. Modul [" + packetData[0] + "] odebrano [" + packetLength + "]");
      System.out.println(
          "UDP : Niekompletny pakiet. Modul ["
              + packetData[0]
              + "] odebrano ["
              + packetLength
              + "]");
    }
    return packetCorrect;
  }

  public void UDPSend(final byte[] packetData) {
    final DatagramPacket dp;
    try {
      // Prepare broadcast address
      final byte[] broadcastAddress = InetAddress.getLocalHost().getAddress();
      broadcastAddress[3] = (byte) 0xff;
      dp =
          new DatagramPacket(
              packetData, packetData.length, InetAddress.getByAddress(broadcastAddress), localPort);
      multiSocket.send(dp);
    } catch (final IOException e1) {
      e1.printStackTrace();
    }
  }

  private void sendTimeSynchro() {
    final Date actual = new Date();
    final int minutes = actual.getMinutes();
    if (minutes != timeSynchroLast) {
      timeSynchroLast = minutes;

      final byte[] buf = new byte[10];
      buf[0] = 1;
      buf[1] = 0;
      buf[2] = 0;
      buf[3] = (byte) (actual.getYear() - 100);
      buf[4] = (byte) (actual.getMonth() + 1);
      buf[5] = (byte) actual.getDate();
      buf[6] = (byte) actual.getDay();
      buf[7] = (byte) actual.getHours();
      buf[8] = (byte) actual.getMinutes();
      buf[9] = (byte) actual.getSeconds();

      UDPSend(buf);
    }
  }

  private void sendData(
      final int senderTyp,
      final int receiverTyp,
      final int receiverNo,
      final int byteNo,
      final int newValue) {
    final byte[] buffer = new byte[5];

    buffer[0] = (byte) senderTyp;
    buffer[1] = (byte) receiverTyp;
    buffer[2] = (byte) receiverNo;
    buffer[3] = (byte) byteNo;
    buffer[4] = (byte) newValue;
    UDPSend(buffer);
    try {
      TimeUnit.MILLISECONDS.sleep(50);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }

  // send Heating Module NV
  private void sendHeatingNV() {
    int newValue;

    newValue =
        (((module_heating.isNVCheapTariffOnly() ? 1 : 0) << 2)
            | ((module_heating.isNVHeatingActivated() ? 1 : 0) << 1)
            | ((module_heating.isNVWaterSuperheat() ? 1 : 0) << 0));
    sendData(moduleMain, module_heating.getModuleType(), 0, 3, newValue);

    newValue = (int) (module_heating.getNVReqTempBufferCO() * 2);
    sendData(moduleMain, module_heating.getModuleType(), 0, 6, newValue);

    newValue = (int) (module_heating.getNVReqTempBufferCWU() * 2);
    sendData(moduleMain, module_heating.getModuleType(), 0, 7, newValue);

    newValue = (int) (module_heating.getNVHeatPumpAlarmTemp());
    sendData(moduleMain, module_heating.getModuleType(), 0, 21, newValue);
  }

  // send Comfort Module NV
  private void sendComfortNV() {
    final Module_Comfort.Zone[] zone = module_comfort.getZone();
    sendData(moduleMain, module_comfort.getModuleType(), 0, 2, (int) (zone[0].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 6, (int) (zone[1].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 10, (int) (zone[2].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 14, (int) (zone[3].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 18, (int) (zone[4].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 22, (int) (zone[5].NVReqTemp * 2));
    sendData(moduleMain, module_comfort.getModuleType(), 0, 26, (int) (zone[6].NVReqTemp * 2));
  }

  private void sendVentNV() {
    int newValue;

    if (!module_vent.getReqAutoDiagnosis().isUpToDate()) {
      newValue = (((boolean) module_vent.getReqAutoDiagnosis().getNewValue() ? 1 : 0));
      sendData(moduleMain, module_vent.getModuleType(), 0, 0, newValue);
    }

    if (!module_vent.getActiveCooling().isUpToDate()
        || !module_vent.getActiveHeating().isUpToDate()
        || !module_vent.getReqLazDol().isUpToDate()
        || !module_vent.getReqLazGora().isUpToDate()
        || !module_vent.getReqKuchnia().isUpToDate()) {
      newValue =
          ((((boolean) module_vent.getActiveCooling().getNewValue() ? 1 : 0) << 6)
              | (((boolean) module_vent.getActiveHeating().getNewValue() ? 1 : 0) << 5)
              | (((boolean) module_vent.getReqLazDol().getNewValue() ? 1 : 0) << 4)
              | (((boolean) module_vent.getReqLazGora().getNewValue() ? 1 : 0) << 3)
              | (((boolean) module_vent.getReqKuchnia().getNewValue() ? 1 : 0) << 2));
      sendData(moduleMain, module_vent.getModuleType(), 0, 1, newValue);
    }

    if (!module_vent.getNormalMode().getDelayTime().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          30,
          (int) module_vent.getNormalMode().getDelayTime().getNewValue());
    }

    if (!module_vent.getHumidityAlertMode().getTriggerInt().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          31,
          (int) module_vent.getHumidityAlertMode().getTriggerInt().getNewValue());
    }
    if (!module_vent.getHumidityAlertMode().getDelayTime().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          32,
          (int) module_vent.getHumidityAlertMode().getDelayTime().getNewValue());
    }

    if (!module_vent.getDefrostMode().getTriggerInt().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          33,
          (int) module_vent.getDefrostMode().getTriggerInt().getNewValue());
    }
    if (!module_vent.getDefrostMode().getDelayTime().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          34,
          (int) module_vent.getDefrostMode().getDelayTime().getNewValue());
    }

    for (int i = 35; i <= 58; i++) {
      final VentZones ventZones = module_vent.getActiveTempRegByHours()[i - 35];
      if (!ventZones.getSalon().getRequest().isUpToDate()
          || !ventZones.getPralnia().getRequest().isUpToDate()
          || !ventZones.getLazDol().getRequest().isUpToDate()
          || !ventZones.getRodzice().getRequest().isUpToDate()
          || !ventZones.getNatalia().getRequest().isUpToDate()
          || !ventZones.getKarolina().getRequest().isUpToDate()
          || !ventZones.getLazGora().getRequest().isUpToDate()) {
        newValue =
            ((((boolean) ventZones.getSalon().getRequest().getNewValue() ? 1 : 0) << 7)
                | (((boolean) ventZones.getPralnia().getRequest().getNewValue() ? 1 : 0) << 6)
                | (((boolean) ventZones.getLazDol().getRequest().getNewValue() ? 1 : 0) << 5)
                | (((boolean) ventZones.getRodzice().getRequest().getNewValue() ? 1 : 0) << 4)
                | (((boolean) ventZones.getNatalia().getRequest().getNewValue() ? 1 : 0) << 3)
                | (((boolean) ventZones.getKarolina().getRequest().getNewValue() ? 1 : 0) << 2)
                | (((boolean) ventZones.getLazGora().getRequest().getNewValue() ? 1 : 0) << 1));
        sendData(moduleMain, module_vent.getModuleType(), 0, i, newValue);
      }
    }

    if (!module_vent.getMinTemp().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          59,
          (int) module_vent.getMinTemp().getNewValue());
    }

    for (int i = 60; i <= 83; i++) {
      final VentZones ventZones = module_vent.getNormalOnByHours()[i - 60];
      if (!ventZones.getSalon().getRequest().isUpToDate()
          || !ventZones.getPralnia().getRequest().isUpToDate()
          || !ventZones.getLazDol().getRequest().isUpToDate()
          || !ventZones.getRodzice().getRequest().isUpToDate()
          || !ventZones.getNatalia().getRequest().isUpToDate()
          || !ventZones.getKarolina().getRequest().isUpToDate()
          || !ventZones.getLazGora().getRequest().isUpToDate()) {
        newValue =
            ((((boolean) ventZones.getSalon().getRequest().getNewValue() ? 1 : 0) << 7)
                | (((boolean) ventZones.getPralnia().getRequest().getNewValue() ? 1 : 0) << 6)
                | (((boolean) ventZones.getLazDol().getRequest().getNewValue() ? 1 : 0) << 5)
                | (((boolean) ventZones.getRodzice().getRequest().getNewValue() ? 1 : 0) << 4)
                | (((boolean) ventZones.getNatalia().getRequest().getNewValue() ? 1 : 0) << 3)
                | (((boolean) ventZones.getKarolina().getRequest().getNewValue() ? 1 : 0) << 2)
                | (((boolean) ventZones.getLazGora().getRequest().getNewValue() ? 1 : 0) << 1));
        sendData(moduleMain, module_vent.getModuleType(), 0, i, newValue);
      }
    }

    if (!module_vent.getActiveCoolingFanSpeed().isUpToDate()) {
      sendData(
          moduleMain,
          module_vent.getModuleType(),
          0,
          89,
          (int) module_vent.getActiveCoolingFanSpeed().getNewValue());
    }
  }

  private void sendWeatherNV() {}

  private void sendSewageNV() {
    int newValue;

    newValue = Math.abs(module_sewage.getNVmaxWaterLevel());
    sendData(moduleMain, module_sewage.getModuleType(), 0, 5, newValue);

    newValue = Math.abs(module_sewage.getNVminWaterLevel());
    sendData(moduleMain, module_sewage.getModuleType(), 0, 6, newValue);

    newValue = Math.abs(module_sewage.getNVZeroRefWaterLevel());
    sendData(moduleMain, module_sewage.getModuleType(), 0, 7, newValue);

    newValue = Math.abs(module_sewage.getNVIntervalAirPump());
    sendData(moduleMain, module_sewage.getModuleType(), 0, 8, newValue);
  }

  private void sendExtLightNV() {
    int newValue = 0;
    newValue =
        (((module_extLights.getNVLightDimmer()[0].isForceMax() ? 1 : 0) << 7)
            | ((module_extLights.getNVLightDimmer()[1].isForceMax() ? 1 : 0) << 6)
            | ((module_extLights.getNVLightDimmer()[2].isForceMax() ? 1 : 0) << 5)
            | ((module_extLights.getNVLightDimmer()[3].isForceMax() ? 1 : 0) << 4)
            | ((module_extLights.getNVLightDimmer()[0].isForce0() ? 1 : 0) << 3)
            | ((module_extLights.getNVLightDimmer()[1].isForce0() ? 1 : 0) << 2)
            | ((module_extLights.getNVLightDimmer()[2].isForce0() ? 1 : 0) << 1)
            | ((module_extLights.getNVLightDimmer()[3].isForce0() ? 1 : 0) << 0));
    sendData(moduleMain, module_extLights.getModuleType(), 0, 3, newValue);

    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        8,
        module_extLights.getNVstartLightLevel());

    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        9,
        module_extLights.getNVLightDimmer()[0].getStandByIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        10,
        module_extLights.getNVLightDimmer()[1].getStandByIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        11,
        module_extLights.getNVLightDimmer()[2].getStandByIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        12,
        module_extLights.getNVLightDimmer()[3].getStandByIntens());

    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        13,
        module_extLights.getNVoffTime().getHour());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        14,
        module_extLights.getNVoffTime().getMinute());

    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        15,
        module_extLights.getNVLightDimmer()[0].getMaxIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        16,
        module_extLights.getNVLightDimmer()[1].getMaxIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        17,
        module_extLights.getNVLightDimmer()[2].getMaxIntens());
    sendData(
        moduleMain,
        module_extLights.getModuleType(),
        0,
        18,
        module_extLights.getNVLightDimmer()[3].getMaxIntens());
  }

  public class UDPListener implements Runnable {
    @Override
    public void run() {
      long lastWatchdogCheck = currentTimeMillis();
      while (true) {
        try {
          buffer = new byte[BUFFER_SIZE];
          final DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
          packetData = new int[BUFFER_SIZE];
          try {
            multiSocket.receive(packet);
          } catch (final IOException e) {
            e.printStackTrace();
          }
          if (packet.getLength() > 0) {
            for (int i = 0; i < packet.getLength(); i++) {
              packetData[i] = (packet.getData()[i] & 0xff); // 0xFF to change values to unsigned int
            }

            String message = "UDP=";
            for (int i = 0; i < packet.getLength(); i++) {
              message += "[" + packetData[i] + "]";
            }
            Logger.info(message);
          }
          if (packetDataCorrect(packetData, packet.getLength())) {
            try {
              Logger.debug(
                  "* * * *zapis pakietu ["
                      + packetData[0]
                      + "]["
                      + packetData[1]
                      + "]["
                      + packetData[2]
                      + "]* * * *");
              mongoDBController.saveUDPFrame(packetData);
              Logger.debug(
                  "* * * *koniec zapisu pakietu ["
                      + packetData[0]
                      + "]["
                      + packetData[1]
                      + "]["
                      + packetData[2]
                      + "]* * * *");
            } catch (final CloneNotSupportedException e) {
              e.printStackTrace();
            }
          }
          if (lastWatchdogCheck + 10000 < currentTimeMillis()) {
            lastWatchdogCheck = currentTimeMillis();
            Logger.debug("UDP controller thread OK");
          }

        } catch (final Error error) {
          error.printStackTrace();
          System.out.println("ERROR wątku " + error.getMessage());
          mongoDBController.saveError("MAIN ERROR", error.getMessage());
        } catch (final Exception exc) {
          exc.printStackTrace();
          System.out.println("EXCEPTION wątku " + exc.toString());
          mongoDBController.saveError("MAIN EXCEPTION", exc.toString());

        } catch (final Throwable throwable) {
          throwable.printStackTrace();
          System.out.println("THROWABLE WĄTKU " + throwable);
          mongoDBController.saveError("MAIN THROWABLE", throwable.getMessage());
        }
      }
    }
  }

  public class EventRunBackground implements Runnable {
    @Override
    public void run() {
      do {
        // check if UDP thread alive
        if ((!UDPThread.isAlive()) || (UDPThread.isInterrupted())) {
          mongoDBController.saveError("MAIN", "PRÓBA PONOWNEGO URUCHOMIENIA UDP THREAD");
          System.out.println("PRÓBA PONOWNEGO URUCHOMIENIA UDP THREAD");
          UDPThread.start();
        }
        try {
          TimeUnit.MILLISECONDS.sleep(1000);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }

        sendTimeSynchro();
        if ((module_heating.isReqUpdateValues()) && (!module_heating.isAllUpToDate())) {
          sendHeatingNV();
        }
        if ((module_comfort.isReqUpdateValues()) && (!module_comfort.isAllUpToDate())) {
          sendComfortNV();
        }
        if ((module_vent.isReqUpdateValues()) && (!module_vent.isAllUpToDate())) {
          sendVentNV();
        }
        if ((module_weather.isReqUpdateValues()) && (!module_weather.isAllUpToDate())) {
          sendWeatherNV();
        }
        if ((module_sewage.isReqUpdateValues()) && (!module_sewage.isAllUpToDate())) {
          sendSewageNV();
        }
        if ((module_extLights.isReqUpdateValues()) && (!module_extLights.isAllUpToDate())) {
          sendExtLightNV();
        }
      } while (true);
    }
  }
}
