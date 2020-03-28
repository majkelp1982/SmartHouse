package pl.pomazanka.SmartHouse.backend.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
public class UDPController {

    //Wired classes
    @Autowired
    MongoDBController mongoDBController;

    // UDP variables
    private int timeSynchroLast = 100;
    private static DatagramSocket datagramSocket;
    private static final int BUFFER_SIZE = 128;
    private static byte[] buffer;
    private static int[] packetData;
    int localPort = 6000;
    private static final int PACKET_SIZE_MODULE_3 = 16;					    // length of UDP data from module 3 "wentylacja"
    private static final int PACKET_SIZE_MODULE_3_DIAG = 99;				// length of UDP diagnose from module 3 "wentylacja"
    private static final int PACKET_SIZE_MODULE_10 = 31;					// length of UDP data from module 10 "komfort"
    private static final int PACKET_SIZE_MODULE_10_DIAG = 7;				// length of UDP diagnose from module 10 "komfort"
    private static final int PACKET_SIZE_MODULE_14 = 21;					// length of UDP data from module 14 "Ogrzewanie2"
    private static final int PACKET_SIZE_MODULE_14_DIAG = 8;				// length of UDP diagnose from module 14 "Ogrzewanie2"

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
                case 14 :if (packetLength == PACKET_SIZE_MODULE_14_DIAG)    packetCorrect = true; break;
                default : System.out.println("Wrong data format : module["+packetData[0]+"]");
            }
        }
        else {
            switch (packetData[0]) {		        // Only for standard frames
                case 1: packetCorrect = true; break;
                case 3: if (packetLength == PACKET_SIZE_MODULE_3) packetCorrect = true; break;
                case 10:if (packetLength == PACKET_SIZE_MODULE_10) packetCorrect = true;break;
                case 14:if (packetLength == PACKET_SIZE_MODULE_14) packetCorrect = true;break;
                default : System.out.println("Wrong data format : module["+packetData[0]+"]");
            }
        }
        if (!packetCorrect) mongoDBController.saveNotice("UDP", "Niekompletny pakiet. Modul ["+packetData[0]+"] odebrano ["+packetLength+"]");
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

    public class EventRunBackground implements Runnable {
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            do {
                sendTimeSynchro();
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }
}

