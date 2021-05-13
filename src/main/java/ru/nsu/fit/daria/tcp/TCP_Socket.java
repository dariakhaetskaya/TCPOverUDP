package ru.nsu.fit.daria.tcp;

import java.io.*;
import java.net.*;
import java.util.Random;

public class TCP_Socket {
    private final double lossPercent;
    protected static Random randomizer;
    private InetAddress address;

    public TCP_Socket(double lossPercent, String ip) {
        this.lossPercent = lossPercent;
        randomizer = new Random();
        try {
            this.address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        }
    }

    private byte[] serializePacket(TCP_Packet packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        objectOutputStream.writeObject(packet);
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    protected void sendPacketOverUDP(DatagramSocket datagramSocket, int port, TCP_Packet... packets) throws IOException {
        for (TCP_Packet packet: packets){
            if (packet.syn || randomizer.nextDouble() > lossPercent){
                byte[] byteBuf = serializePacket(packet);
                DatagramPacket datagram = new DatagramPacket(byteBuf, byteBuf.length, address, port);
                datagramSocket.send(datagram);
            } else {
                System.out.println("Droppin: " + packet);
            }
        }
    }

    protected TCP_Packet receivePacketOverUDP(DatagramSocket datagramSocket) throws IOException, ClassNotFoundException {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            datagramSocket.receive(datagram);
        } catch (SocketTimeoutException e) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receiveBuffer);
        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(byteArrayInputStream));
        TCP_Packet packet = (TCP_Packet) objectInputStream.readObject();
        objectInputStream.close();
        return packet;
    }

}
