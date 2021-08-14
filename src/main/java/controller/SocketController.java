package controller;

import main.LoopHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class SocketController implements Runnable{
    private ServerSocket serverSocket;
    private InetAddress ipAddress;
    private int port;
    private final LoopHandler loopHandler;
    private final LinkedList<ClientHandler> clients;
    private static final Random random = new Random();

    public SocketController() {
        clients = new LinkedList<>();
        getSocketConfig();
        try {
            this.serverSocket = new ServerSocket(port,50, ipAddress);
        } catch (IOException e){
            //e.printStackTrace();
            LogHandler.logger.error("could not run Server on address " + ipAddress.getHostAddress() + " and port " + port);
        }
        loopHandler = new LoopHandler(600, this);
        //loopHandler.setNonStop(true);
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            ClientHandler clt = new ClientHandler(socket, random.nextInt());
            synchronized (clients) {
                clients.add(clt);
            }
            LogHandler.logger.info("accepted client connection request, clientID:"
                    + clt.getClientID()
                    + ", userID:"
                    + clt.getUserID());
        } catch (IOException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not accept the client connection request");
        }
    }

    private void getSocketConfig(){
        String path = "src/main/config/server_socket_config.txt";
        File file = new File(path);
        if(file.isFile()) {
            try {
                Scanner scanner = new Scanner(file);
                String[] strings = scanner.nextLine().split(",");
                ipAddress = InetAddress.getByName(strings[0]);
                port = Integer.parseInt(strings[1]);
                LogHandler.logger.info(ipAddress.getHostAddress() + " / port:" + port + " selected.");
                return;
            } catch (FileNotFoundException | UnknownHostException e) {
                //e.printStackTrace();
                LogHandler.logger.error("server socket config file could not be used");
            }
        }
        try {
            ipAddress = InetAddress.getByName("localhost");
            port = 8000;
            LogHandler.logger.info("localhost / port:8000 selected.");
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            LogHandler.logger.error("could not connect to Server via localhost " + " and port " + port);
        }
    }

    public ClientHandler getClient(int clientID){
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getClientID() == clientID)
                    return client;
            }
            return null;
        }
    }

    public ClientHandler getClientByAuth(int authToken){
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getAuthToken() == authToken)
                    return client;
            }
            return null;
        }
    }

    public ClientHandler getClientByID(int userID){
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUserID() == userID)
                    return client;
            }
            return null;
        }
    }

    public void removeClient(ClientHandler clt){
        clients.remove(clt);
    }

    public boolean isUserOnline(int userID){
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                if (ch.getUserID() == userID)
                    return true;
            }
        }
        return false;
    }
}