package ru.nsu.fit.daria.tcp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        TCP_ClientSocket clientSocket = new TCP_ClientSocket(0.8, "localhost", 2000, 2004);
        TCP_ServerSocket serverSocket = new TCP_ServerSocket(0, "localhost", 2000, 2004);

        Thread client = new Thread(clientSocket);
        Thread server = new Thread(serverSocket);
        client.start();
        server.start();

    }
}
