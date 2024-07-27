package servertictactoe;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {

    private static Server server;
    private ServerSocket serverSocket;
    private List<PlayersHandler> players = new ArrayList<>();
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
                    PlayersHandler playerHandler = new PlayersHandler(socket, this);
                    players.add(playerHandler);
                    new Thread(playerHandler).start();
                    checkAndPairPlayers();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Error accepting client connection", ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Server exception", ex);
        }
    }

    private void checkAndPairPlayers() {
        // Pair players
        if (players.size() % 2 == 0) {
            int lastIndex = players.size() - 1;
            PlayersHandler player1 = players.get(lastIndex - 1);
            PlayersHandler player2 = players.get(lastIndex);

            player1.setOpponent(player2);
            player2.setOpponent(player1);

            // Notify players that the game has started
            notifyPlayers("Game Started", player1, player2);

            // Notify player1 that it's their turn
            notifyPlayerTurn(player1);
        }
    }

    public synchronized void broadcastMove(int index, String player, PlayersHandler sender) {
        try {
            JSONObject move = new JSONObject();
            move.put("query", "MOVE");
            move.put("index", index);
            move.put("player", player);

            if (sender.getOpponent() != null) {
                sender.getOpponent().sendMessage(move.toString());
            }

            // Notify the next player to make a move
            notifyPlayerTurn(sender.getOpponent());
        } catch (JSONException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void notifyPlayers(String message, PlayersHandler player1, PlayersHandler player2) {
        try {
            JSONObject notification = new JSONObject();
            notification.put("query", message);

            player1.sendMessage(notification.toString());
            player2.sendMessage(notification.toString());
        } catch (JSONException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void notifyPlayerTurn(PlayersHandler player) {
        try {
            if (player != null) {
                JSONObject turnNotification = new JSONObject();
                turnNotification.put("query", "YOUR_TURN");
                turnNotification.put("symbol", player.getPlayerSymbol());
                player.sendMessage(turnNotification.toString());
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
