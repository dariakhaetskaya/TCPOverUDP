package ru.nsu.fit.daria.tcp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import static ru.nsu.fit.daria.tcp.utils.Util.*;

public class TCP_ClientSocket extends TCP_Socket implements Runnable{
    private int seqNumber;
    private int lastAcknowledgedPacket;
    HashMap<TCP_Packet, Integer> packetsSent;
    int serverPort;
    int receiverPort;
    DatagramSocket datagramSocket;
    private final int timeout;

    public TCP_ClientSocket(double lossPercent, String ip, int serverPort, int receiverPort) throws SocketException {
        super(lossPercent, ip);
        seqNumber = randomizer.nextInt(1000);
        lastAcknowledgedPacket = -1;
        this.serverPort = serverPort;
        this.receiverPort = receiverPort;
        datagramSocket = new DatagramSocket(receiverPort);
        packetsSent = new HashMap<>();
        datagramSocket.setSoTimeout(1000);
        timeout = 1000;
    }

    private int getNewClientSeqNumber(){
        return seqNumber++;
    }

    private int getSeqNumber(){
        return seqNumber;
    }

    public void send(TCP_Packet packet) throws IOException, ClassNotFoundException {
        packetsSent.put(packet, timeout);
        sendPacketOverUDP(datagramSocket, serverPort, packet);
    }

    public void connect() {
        try{
            TCP_Packet SYN = new TCP_Packet(true, false, getNewClientSeqNumber(), 0, 0);
            sendPacketOverUDP(datagramSocket, serverPort, SYN);
            TCP_Packet SYN_ACK = receivePacketOverUDP(datagramSocket);
            if (SYN_ACK.ackNumber != seqNumber || !SYN_ACK.syn || !SYN_ACK.ack){
                throw new ConnectException();
            } else {
                TCP_Packet ACK = new TCP_Packet(false, true, getNewClientSeqNumber(), SYN_ACK.seqNumber+1, 0);
                sendPacketOverUDP(datagramSocket, serverPort, ACK);
                System.out.println("Connection established");
                lastAcknowledgedPacket = getSeqNumber();
            }

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to connect");
        }
    }

    public TCP_Packet receive() throws IOException, ClassNotFoundException {
        long startingTime = System.currentTimeMillis();
        long currentTime;
        while (true) {
            TCP_Packet packet = receivePacketOverUDP(datagramSocket);
            if (packet != null) {
                int ackNum = packet.ackNumber;
                if (ackNum > lastAcknowledgedPacket) {
                    packetsSent.entrySet().removeIf(tuple -> tuple.getKey().seqNumber < ackNum);
                    lastAcknowledgedPacket = ackNum;
                    return packet;

                }
            } else {
                currentTime = System.currentTimeMillis() - startingTime;
                for (TCP_Packet currentPacket : packetsSent.keySet()) {
                    if (currentTime >= timeout) {
                        send(currentPacket);
                    }
                }
            }
        }
    }

    @Override
    public void run(){
        connect();
        try {
            String helloStr = "hello world!";
            TCP_Packet hello = new TCP_Packet(false, false, getNewClientSeqNumber(), 0, helloStr.length(), helloStr);
            send(hello);
            System.out.println(ANSI_YELLOW + "Client sent: " + hello + ANSI_RESET);
            TCP_Packet helloAck = receive();
            System.out.println(ANSI_YELLOW + "Client received: " + helloAck + ANSI_RESET);

            String msg  = "in god we trust";
            TCP_Packet pckt = new TCP_Packet(false, false, getNewClientSeqNumber(), helloAck.seqNumber, msg.length(), msg);
            System.out.println(ANSI_YELLOW + "Client sent: " + pckt + ANSI_RESET);
            send(pckt);
            TCP_Packet pcktAck = receive();
            System.out.println(ANSI_YELLOW + "Client received: " + pcktAck + ANSI_RESET);
            System.out.println("helloAck"+pcktAck);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
