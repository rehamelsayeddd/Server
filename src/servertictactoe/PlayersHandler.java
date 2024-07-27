package servertictactoe;

import database.TicTacToeDataBase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayersHandler extends Thread {

    private Server server;
    private BufferedReader br;
    private PrintStream ps;
    private Socket currentSocket;
    private static List<PlayersHandler> PlayersList = new ArrayList<>();
    private String clientData, query;
    private StringTokenizer token;

    public PlayersHandler(Socket socket) {
        server = Server.getServer(); // Singleton object
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ps = new PrintStream(socket.getOutputStream());
            currentSocket = socket;
            this.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public void run() {
        while (currentSocket.isConnected()) {
            try {
                clientData = br.readLine();
                System.out.println("Received from client: " + clientData);
                if (clientData != null) {
                    JSONObject json = new JSONObject(clientData);
                    String query = json.optString("query");
                    switch (query) {
                        
                         case "SignUp":
                                handleSignUp(clientData);
                                break;
                        
                        default:
                            JSONObject unknownQueryResponse = new JSONObject();
                            unknownQueryResponse.put("response", "Unknown query");
                            ps.println(unknownQueryResponse.toString());
                            System.out.println("Unknown query");
                            break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Closing connection");
                try {
                    currentSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            } catch (JSONException j) {
                j.printStackTrace();
            }
        }
       
    }

    private void handleSignUp(String jsonData) {
        try {
            // Parse the JSON data
            JSONObject jsonObject = new JSONObject(jsonData);
            String username = jsonObject.getString("username");
            String email = jsonObject.getString("email");
            String password = jsonObject.getString("password");

            System.out.println("SignUp Request - Username: " + username + ", Email: " + email);
            TicTacToeDataBase tic = TicTacToeDataBase.getInstance();
            tic.SignUp(email, username, password);

            // Print for debugging
            System.out.println("SignUp Request - Username: " + username + ", Email: " + email);

            // Use the singleton instance of TicTacToeDataBase
            TicTacToeDataBase tico = TicTacToeDataBase.getInstance();

            // Sign up the user
            tic.SignUp(email, username, password);

            // Send a response to the client

            ps.println("SignUp response");
            System.out.println("SignUp");

        } catch (SQLException ex) {
            Logger.getLogger(PlayersHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (org.json.JSONException ex) {
            ps.println("Invalid JSON data");
            System.out.println("Invalid JSON data");
        }
    }
}
