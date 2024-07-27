package servertictactoe;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

public class PlayersHandler implements Runnable {

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final Server server;
    private PlayersHandler opponent;
    private String playerSymbol;
    private boolean myTurn;

    public PlayersHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void setOpponent(PlayersHandler opponent) {
        this.opponent = opponent;
        if (this.opponent != null) {
            try {
                this.playerSymbol = "X";
                opponent.playerSymbol = "O";
                // Notify players of their symbols
                this.sendMessage(new JSONObject().put("query", "SYMBOL").put("symbol", this.playerSymbol).toString());
                this.opponent.sendMessage(new JSONObject().put("query", "SYMBOL").put("symbol", this.opponent.playerSymbol).toString());
            } catch (JSONException ex) {
                Logger.getLogger(PlayersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void closeSocket(Socket socket) {
    try {
        socket.close();
    } catch (IOException ex) {
        Logger.getLogger(PlayersHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    public String getPlayerSymbol() {
        return playerSymbol;
    }

    public PlayersHandler getOpponent() {
        return opponent;
    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                System.out.println("Received from player: " + line);
                JSONObject json = new JSONObject(line);
                String query = json.optString("query");
                switch (query) {
                    case "MOVE":
                        int index = json.getInt("index");
                        String player = json.getString("player");

                        // Broadcast the move to the opponent
                        server.broadcastMove(index, player, this);
                        break;

                    case "YOUR_TURN":
                        myTurn = true;
                        break;

                    default:
                        // Handle unknown query
                        System.out.println("Unknown query received: " + query);
                        break;
                }
            }
        } catch (IOException | org.json.JSONException ex) {
            Logger.getLogger(PlayersHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(PlayersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }
}
