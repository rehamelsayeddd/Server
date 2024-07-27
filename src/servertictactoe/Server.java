package servertictactoe;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static Server server;
    private ServerSocket serverSocket;
    private Thread listener;

    private Server() {
    }

    public static Server getServer() {
        if (server == null) {
            server = new Server();
        }
        return server;
    }

    public void initServer() {
        try {
            serverSocket = new ServerSocket(9081);
            System.out.println("Server IP: " + Inet4Address.getLocalHost().getHostAddress());

            listener = new Thread(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        new PlayersHandler(socket);
                    } catch (IOException ex) {
                        System.out.println("Problem in connecting players");
                    }
                }
            });
            listener.start();
        } catch (IOException ex) {
            System.out.println("Server exception");
            ex.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    //     Server server = Server.getServer();
    //     server.initServer();
    // }
}
