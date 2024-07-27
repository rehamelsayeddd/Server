package servertictactoe;

import database.TicTacToeDataBase;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;

public class PlayersHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private TicTacToeDataBase tic;
    private static Map<String, PlayersHandler> playersMap = new HashMap<>();

    public PlayersHandler(Socket socket) {
        this.socket = socket;
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
            tic = TicTacToeDataBase.getDataBase();
            new Thread(this).start();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String message = input.readLine();
            JSONObject data = new JSONObject(message);
            this.username = data.getString("username");
            playersMap.put(username, this);
            tic.addUser(username);

            sendActiveUsers();

            while (true) {
                message = input.readLine();
                data = new JSONObject(message);
                String command = data.getString("command");
                if (command.equals("CHALLENGE")) {
                    String opponent = data.getString("opponent");
                    PlayersHandler opponentHandler = playersMap.get(opponent);
                    if (opponentHandler != null) {
                        JSONObject challengeRequest = new JSONObject();
                        challengeRequest.put("command", "CHALLENGE");
                        challengeRequest.put("opponent", username);
                        opponentHandler.output.println(challengeRequest.toString());
                    }
                } else if (command.equals("MOVE")) {
                    String opponent = data.getString("opponent");
                    int row = data.getInt("row");
                    int col = data.getInt("col");
                    char player = data.getString("player").charAt(0);

                    PlayersHandler opponentHandler = playersMap.get(opponent);
                    if (opponentHandler != null) {
                        JSONObject moveData = new JSONObject();
                        moveData.put("command", "MOVE");
                        moveData.put("row", row);
                        moveData.put("col", col);
                        moveData.put("player", player);
                        opponentHandler.output.println(moveData.toString());
                    }
                }
            }
        } catch (IOException | SQLException | JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                tic.removeUser(username);
                playersMap.remove(username);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendActiveUsers() throws SQLException, IOException, JSONException {
        ResultSet rs = tic.getActivePlayers();
        StringBuilder userList = new StringBuilder();
        while (rs.next()) {
            userList.append(rs.getString("username")).append(",");
        }
        JSONObject response = new JSONObject();
        response.put("activeUsers", userList.toString());
        output.println(response.toString());
    }
}
