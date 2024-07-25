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
import org.json.JSONException;

public class PlayersHandler implements Runnable {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    public  TicTacToeDataBase tic;

    public PlayersHandler(Socket socket) {
        this.socket = socket;
        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run()  {
        try {
            String message = input.readLine();
            JSONObject data = new JSONObject(message);
            this.username = data.getString("username");
            //.addUser(username);

            // Send active users list to client
            sendActiveUsers();

            while (true) {
                message = input.readLine();
                data = new JSONObject(message);
                String command = data.getString("command");
                if (command.equals("CHALLENGE")) {
                    String opponent = data.getString("opponent");
                    // Handle the challenge request
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }catch(JSONException j){} 
//        finally {
//            try {
//                tic=TicTacToeDataBase.getDataBase();
//                tic.removeUser(username);
//                socket.close();
//            }catch (IOException | SQLException e ) {
//                e.printStackTrace();
//            }
//        }
    }

    private void sendActiveUsers() throws SQLException, IOException,JSONException {
       tic=TicTacToeDataBase.getDataBase();
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
