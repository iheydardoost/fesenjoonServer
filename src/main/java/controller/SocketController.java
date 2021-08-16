package controller;

import main.LoopHandler;
import main.Main;
import model.config.ServerSocketConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Random;

public class SocketController implements Runnable{
    private ServerSocket serverSocket;
    private ServerSocketConfig serverSocketConfig;
    private LoopHandler loopHandler;
    private final LinkedList<ClientHandler> clients;
    private static final Random random = new Random();

    public SocketController() {
        clients = new LinkedList<>();
    }

    public void initConnection(){
        serverSocketConfig = Main.getMainController().getConfigLoader().getServerSocketConfig();
        try {
            this.serverSocket =
                    new ServerSocket(serverSocketConfig.getPort(),50, serverSocketConfig.getIpAddress());
        } catch (IOException e){
            //e.printStackTrace();
            LogHandler.logger.error("could not run Server on address "
                    + serverSocketConfig.getIpAddress().getHostAddress()
                    + " and port " + serverSocketConfig.getPort());
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

    public ClientHandler getClientByID(long userID){
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

    public boolean isUserOnline(long userID){
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                if (ch.getUserID() == userID)
                    return true;
            }
        }
        return false;
    }
}