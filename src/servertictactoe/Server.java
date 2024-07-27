package servertictactoe;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {

    private static Server server;
    private ServerSocket serverSocket;
    private Map<Integer, PlayersHandler> players = new HashMap<>();
    private int currentPlayerId = 0;
    private static final int PORT = 9081;

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
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server IP: " + Inet4Address.getLocalHost().getHostAddress());

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    int playerId = currentPlayerId++;
                    PlayersHandler playerHandler = new PlayersHandler(socket, playerId, this);
                    players.put(playerId, playerHandler);
                    new Thread(playerHandler).start();

                    if (players.size() == 2) {
                        // Notify players that the game has started
                        notifyPlayers("Game Started");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error accepting client connection", ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Server exception", ex);
        }
    }

    public synchronized void broadcastMove(int index, String player) {
        try {
            JSONObject move = new JSONObject();
            move.put("query", "MOVE");
            move.put("index", index);
            move.put("player", player);
            
            // Send move to all connected players
            for (PlayersHandler playerHandler : players.values()) {
                playerHandler.sendMessage(move.toString());
            }
        } catch (JSONException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void notifyPlayers(String message) {
        try {
            JSONObject notification = new JSONObject();
            notification.put("query", message);
            
            // Notify all players
            for (PlayersHandler playerHandler : players.values()) {
                playerHandler.sendMessage(notification.toString());
            }
        } catch (JSONException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error closing server", ex);
        }
    }
}