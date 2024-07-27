package servertictactoe;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayersHandler implements Runnable {

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final Server server;
    private final int playerId;
    private final String playerSymbol;

    public PlayersHandler(Socket socket, int playerId, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.playerId = playerId;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Determine player symbol based on playerId
        this.playerSymbol = (playerId == 1) ? "X" : "O"; // Player 1 is "X", Player 2 is "O"
    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                System.out.println("Received from player " + playerId + ": " + line);
                JSONObject json = new JSONObject(line);
                String query = json.optString("query");
                if ("MOVE".equals(query)) {
                    int index = json.getInt("index");
                    String player = json.getString("player");
                    
                    // Broadcast the move to other player
                    server.broadcastMove(index, player);
                    
                    // Ensure Player 2 responds with "O"
                    if (playerId == 1 && player.equals("X")) {
                        // This ensures Player 2 always responds with "O"
                        server.broadcastMove(index, "O");
                    }
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
}
