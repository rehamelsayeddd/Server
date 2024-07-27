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
                System.out.println("Received: " + clientData);
                if (clientData != null) {
                    // Tokenize using "####" as delimiter
                    token = new StringTokenizer(clientData, "####");
                    if (token.hasMoreTokens()) {
                        query = token.nextToken();
                        switch (query) {
                            case "SignIn":
                                ps.println("SignIn response");
                                System.out.println("SignIn");
                                break;
                            case "SignUp":
                                handleSignUp(token.nextToken());
                                break;
                            case "playerlist":
                                ps.println("playerlist response");
                                System.out.println("playerlist");
                                break;
                            case "request":
                                ps.println("request response");
                                System.out.println("request");
                                break;
                            case "accept":
                                ps.println("accept response");
                                System.out.println("accept");
                                break;
                            case "decline":
                                ps.println("decline response");
                                System.out.println("decline");
                                break;
                            case "withdraw":
                                ps.println("withdraw response");
                                System.out.println("withdraw");
                                break;
                            case "gameTic":
                                ps.println("gameTic response");
                                System.out.println("gameTic");
                                break;
                            case "finishgameTic":
                                ps.println("finishgameTic response");
                                System.out.println("finishgameTic");
                                break;
                            case "updateScore":
                                ps.println("updateScore response");
                                System.out.println("updateScore");
                                break;
                            case "available":
                                ps.println("available response");
                                System.out.println("available");
                                break;
                            case "logout":
                                ps.println("logout response");
                                System.out.println("logout");
                                break;
                            default:
                                ps.println("Unknown query");
                                System.out.println("Unknown query: " + query);
                                break;
                        }
                    } else {
                        ps.println("Invalid input format");
                        System.out.println("Invalid input format");
                    }
                }
            } catch (IOException ex) {
                System.out.println("Closing connection");
                this.stop();
            }
        }
        if (currentSocket.isClosed()) {
            System.out.println("Connection closed");
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
