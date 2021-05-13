package ru.nsu.fit.daria.tcp;
import java.io.Serializable;

public class TCP_Packet implements Serializable {
    public boolean ack;
    public boolean syn;
    public int seqNumber;
    public int ackNumber;
    public int length;
    String data;

    public TCP_Packet(boolean syn, boolean ack, int seqNumber, int ackNumber, int headerLength){
        this.syn = syn;
        this.ack = ack;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.length = headerLength;
    }

    public TCP_Packet(boolean syn, boolean ack, int seqNumber, int ackNumber, int headerLength, String data){
        this.syn = syn;
        this.ack = ack;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.length = headerLength;
        this.data = data;
    }

    @Override
    public String toString(){
        return " Packet [ACK=" + ack + ", SYN=" + syn + ", seqNumber=" + seqNumber + ", ackNumber=" + ackNumber
                + ", length=" + length + "]";
    }

}
