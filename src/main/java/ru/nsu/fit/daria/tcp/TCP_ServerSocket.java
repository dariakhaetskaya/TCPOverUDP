package ru.nsu.fit.daria.tcp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import static ru.nsu.fit.daria.tcp.utils.Util.*;

public class TCP_ServerSocket extends TCP_Socket implements Runnable {
    private int serverSeqNumber;
    private int lastAcknowledgedPacket;
    private final ArrayList<TCP_Packet> packetsReceived;
    private final DatagramSocket datagramSocket;
    private final int receiverPort;

    public TCP_ServerSocket(double lossPercent, String ip,  int serverPort, int receiverPort) throws SocketException {
        super(lossPercent, ip);
        serverSeqNumber = 0;
        this.receiverPort = receiverPort;
        datagramSocket = new DatagramSocket(serverPort);
        packetsReceived = new ArrayList<>(0);
        lastAcknowledgedPacket = -1;
    }

    private int getNewServerSeqNumber(){
        return serverSeqNumber++;
    }

    private int getServerSeqNumber(){
        return serverSeqNumber;
    }

    private void sendAck(TCP_Packet packet) throws IOException {
        TCP_Packet ACK = new TCP_Packet(false, true, getNewServerSeqNumber(), packet.seqNumber+1, 0);
        sendPacketOverUDP(datagramSocket, receiverPort, ACK);
        lastAcknowledgedPacket = packet.seqNumber;
    }

    public void accept() throws IOException, ClassNotFoundException {
        TCP_Packet SYN = receivePacketOverUDP(datagramSocket);
        if (SYN.syn && !SYN.ack){
            lastAcknowledgedPacket = SYN.seqNumber+1;
            TCP_Packet SYN_ACK = new TCP_Packet(true, true, getNewServerSeqNumber(), SYN.seqNumber+1, 0);
            sendPacketOverUDP(datagramSocket, receiverPort, SYN_ACK);
            receivePacketOverUDP(datagramSocket);
        }

    }

    public TCP_Packet receive() throws IOException, ClassNotFoundException {
        while (true) {
            TCP_Packet packet = receivePacketOverUDP(datagramSocket);
            if (packet != null) {
                if (packet.seqNumber != lastAcknowledgedPacket + 1){
                    TCP_Packet duplicatedAck = new TCP_Packet(false,true, getServerSeqNumber(), lastAcknowledgedPacket +1,0);
                    sendPacketOverUDP(datagramSocket, receiverPort, duplicatedAck);
                    System.out.println("Ack Sent Again: " + duplicatedAck);
                } else {
                    int savedAck = lastAcknowledgedPacket;
                    boolean duplicate = false;

                    for (TCP_Packet tcp_packet : packetsReceived) {
                        if (packet.seqNumber == tcp_packet.seqNumber) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate){
                        packetsReceived.add(packet);
                    }

                    while (true) {
                        TCP_Packet x = null;
                        for (TCP_Packet tcp_packet : packetsReceived) {
                            if (tcp_packet.seqNumber == savedAck) {
                                x = tcp_packet;
                                break;
                            }
                        }
                        if (x == null) {
                            break;
                        }  else {
                            savedAck = x.seqNumber + x.length;
                            packetsReceived.remove(x);
                        }
                    }

                    if (savedAck != lastAcknowledgedPacket){
                        TCP_Packet newAck = new TCP_Packet(false,true, getServerSeqNumber(), lastAcknowledgedPacket +2,0);
                        sendPacketOverUDP(datagramSocket, receiverPort, newAck);
                        System.out.println("Ack sent" + newAck);
                    }

                    if (!duplicate){
                        packetsReceived.add(packet);
                        return packet;
                    }
                }
            }
        }
    }

    @Override
    public void run(){
        try {
            accept();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            TCP_Packet hello = receive();
            System.out.println(ANSI_GREEN + "Server got:" + hello.data + ANSI_RESET);
            sendAck(hello);

            TCP_Packet msg = receive();
            System.out.println(ANSI_GREEN + "Server got:" + msg.data + ANSI_RESET);
            sendAck(msg);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

