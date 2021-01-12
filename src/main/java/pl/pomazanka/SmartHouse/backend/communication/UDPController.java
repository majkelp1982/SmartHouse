package pl.pomazanka.SmartHouse.backend.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Comfort;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Heating;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_Vent2;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
public class UDPController {

    private final int moduleMain = 1;

    //Wired classes
    @Autowired
    MongoDBController mongoDBController;
    @Autowired
    Module_Heating module_heating;
    @Autowired
    Module_Comfort module_comfort;
    @Autowired
    Module_Vent module_vent;
    @Autowired
    Module_Vent2 module_vent2;

    // UDP variables
    private int timeSynchroLast = 100;
    private static DatagramSocket datagramSocket;
    private static final int BUFFER_SIZE = 128;
    private static byte[] buffer;
    private static int[] packetData;
    int localPort = 6000;
    private static final int PACKET_SIZE_MODULE_3 = 16;					    // length of UDP data from module 3 "wentylacja"
    private static final int PACKET_SIZE_MODULE_3_DIAG = 7;				    // length of UDP diagnose from module 3 "wentylacja"
    private static final int PACKET_SIZE_MODULE_10 = 31;					// length of UDP data from module 10 "komfort"
    private static final int PACKET_SIZE_MODULE_10_DIAG = 7;				// length of UDP diagnose from module 10 "komfort"
    private static final int PACKET_SIZE_MODULE_13 = 37;					// length of UDP data from module 3 "wentylacja2"
    private static final int PACKET_SIZE_MODULE_13_DIAG = 7;			    // length of UDP diagnose from module 3 "wentylacja2"
    private static final int PACKET_SIZE_MODULE_14 = 22;					// length of UDP data from module 14 "Ogrzewanie"
    private static final int PACKET_SIZE_MODULE_14_DIAG = 8;				// length of UDP diagnose from module 14 "Ogrzewanie"

    public UDPController() {
        try {
            datagramSocket = new DatagramSocket(localPort);
            datagramSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        new Thread(new UDPListener()).start();
        // Set new thread
        new Thread(new EventRunBackground()).start();
    }

    private boolean packetDataCorrect(int[] packetData, int packetLength) {
        boolean packetCorrect =false;
        if (packetData[2] == 200) {			        // Only for diagnose frames
            switch (packetData[0]) {
                case 3  :if (packetLength == PACKET_SIZE_MODULE_3_DIAG)     packetCorrect = true; break;
                case 10 :if (packetLength == PACKET_SIZE_MODULE_10_DIAG)    packetCorrect = true; break;
                case 13 :if (packetLength == PACKET_SIZE_MODULE_13_DIAG)    packetCorrect = true; break;
                case 14 :if (packetLength == PACKET_SIZE_MODULE_14_DIAG)    packetCorrect = true; break;
                default : System.out.println("Wrong data format : module["+packetData[0]+"]");
            }
        }
        else {
            switch (packetData[0]) {		        // Only for standard frames
                case 1: packetCorrect = true; break;
                case 3: if (packetLength == PACKET_SIZE_MODULE_3) packetCorrect = true; break;
                case 10:if (packetLength == PACKET_SIZE_MODULE_10) packetCorrect = true;break;
                case 13:if (packetLength == PACKET_SIZE_MODULE_13) packetCorrect = true;break;
                case 14:if (packetLength == PACKET_SIZE_MODULE_14) packetCorrect = true;break;
                default : System.out.println("Wrong data format : module["+packetData[0]+"]");
            }
        }
        if (!packetCorrect) {
            mongoDBController.saveNotice("UDP", "Niekompletny pakiet. Modul ["+packetData[0]+"] odebrano ["+packetLength+"]");
            System.out.println("UDP : Niekompletny pakiet. Modul ["+packetData[0]+"] odebrano ["+packetLength+"]");
        }
        return packetCorrect;
    }

    public void UDPSend(byte[] packetData) {
        DatagramPacket dp;
        try {
            //Prepare broadcast address
            byte[] broadcastAddress = InetAddress.getLocalHost().getAddress();
            broadcastAddress[3] = (byte)0xff;
            dp = new DatagramPacket(packetData, packetData.length, InetAddress.getByAddress(broadcastAddress), localPort);
            datagramSocket.send(dp);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public class UDPListener implements Runnable {
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while (true) {
                buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, BUFFER_SIZE);
                packetData = new int[BUFFER_SIZE];
                try {
                    datagramSocket.receive(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (packet.getLength() > 0) {
                    for (int i=0; i<packet.getLength(); i++)
                        packetData[i] = (packet.getData()[i] & 0xff);				// 0xFF to change values to unsigned int

                    System.out.print("UDP=");
                    for (int i=0; i<packet.getLength(); i++)
                        System.out.print("["+packetData[i] + "]");
                    System.out.println();
                }
                if (packetDataCorrect(packetData, packet.getLength())) {
                    try {
                        mongoDBController.saveUDPFrame(packetData);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void sendTimeSynchro() {
        Date actual = new Date();
        int minutes = actual.getMinutes();
        if (minutes != timeSynchroLast) {
            timeSynchroLast = minutes;

            byte[] buf = new byte[10];
            buf[0] = 1;
            buf[1] = 0;
            buf[2] = 0;
            buf[3] = (byte) (actual.getYear()-100);
            buf[4] = (byte) (actual.getMonth()+1);
            buf[5] = (byte) actual.getDate();
            buf[6] = (byte) actual.getDay();
            buf[7] = (byte) actual.getHours();
            buf[8] = (byte) actual.getMinutes();
            buf[9] = (byte) actual.getSeconds();

            UDPSend(buf);
        }
    }

    private void sendData(int senderTyp, int receiverTyp, int receiverNo, int byteNo, int newValue) {
        byte[] buffer = new byte[5];

        buffer[0] = (byte)senderTyp;
        buffer[1] = (byte)receiverTyp;
        buffer[2] = (byte)receiverNo;
        buffer[3] = (byte)byteNo;
        buffer[4] = (byte)newValue;
        UDPSend(buffer);
    }

    //send Heating Module NV
    private void sendHeatingNV() {
        int newValue;

        newValue = (((module_heating.isNVCheapTariffOnly()? 1:0 ) << 2) | ((module_heating.isNVHeatingActivated()? 1:0 ) << 1) | ((module_heating.isNVWaterSuperheat()? 1:0 ) << 0));
        sendData(moduleMain, module_heating.getModuleType(), 0, 3, newValue);

        newValue = (int)(module_heating.getNVReqTempBufferCO()*2);
        sendData(moduleMain, module_heating.getModuleType(), 0, 6, newValue);

        newValue = (int)(module_heating.getNVReqTempBufferCWU()*2);
        sendData(moduleMain, module_heating.getModuleType(), 0, 7, newValue);

        newValue = (int)(module_heating.getNVHeatPumpAlarmTemp());
        sendData(moduleMain, module_heating.getModuleType(), 0, 21, newValue);
    }

    //send Comfort Module NV
    private void sendComfortNV() {
        Module_Comfort.Zone[] zone = module_comfort.getZone();
        sendData(moduleMain, module_comfort.getModuleType(), 0, 5, (int)(zone[0].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 9, (int)(zone[1].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 13, (int)(zone[2].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 17, (int)(zone[3].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 21, (int)(zone[4].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 25, (int)(zone[5].NVReqTemp*2));
        sendData(moduleMain, module_comfort.getModuleType(), 0, 29, (int)(zone[6].NVReqTemp*2));
    }

    //send Vent Module NV
    private void sendVentNV() {
        int[] hours = module_vent.getNVHour();

        sendData(moduleMain, module_vent.getModuleType(), 0, 4, hours[0]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 5, hours[1]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 6, hours[2]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 7, hours[3]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 8, hours[4]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 9, hours[5]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 10, hours[6]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 11, hours[7]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 12, hours[8]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 13, hours[9]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 14, hours[10]);
        sendData(moduleMain, module_vent.getModuleType(), 0, 15, hours[11]);
    }

    //send Vent Module NV
    private void sendVent2NV() {
        int[] hours = module_vent2.getNVHour();

        sendData(moduleMain, module_vent2.getModuleType(), 0, 1, hours[0]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 2, hours[1]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 3, hours[2]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 4, hours[3]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 5, hours[4]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 6, hours[5]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 7, hours[6]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 8, hours[7]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 9, hours[8]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 10, hours[9]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 11, hours[10]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 12, hours[11]);
        sendData(moduleMain, module_vent2.getModuleType(), 0, 33, (int)module_vent2.getNVpressureDiff()/10);
     }


    public class EventRunBackground implements Runnable {
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sendTimeSynchro();
                if ((module_heating.isReqUpdateValues()) && (!module_heating.isAllUpToDate())) sendHeatingNV();
                if ((module_comfort.isReqUpdateValues()) && (!module_comfort.isAllUpToDate())) sendComfortNV();
                if ((module_vent.isReqUpdateValues()) && (!module_vent.isAllUpToDate())) sendVentNV();
                if ((module_vent2.isReqUpdateValues()) && (!module_vent2.isAllUpToDate())) sendVent2NV();


            } while (true);
        }
    }
}

