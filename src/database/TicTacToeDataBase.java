package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDriver;  // Ensure this import is correct and the library is included in your project

/**
 *
 * @author Abdul-Rahman
 */
public class TicTacToeDataBase {
    private static TicTacToeDataBase instanceData; // Singleton instance
    private Connection con;               // Database connection
    private ResultSet rs;                 // Result set for queries
    private PreparedStatement pst;        // Prepared statement for queries

    // Synchronized getter for result set
    public synchronized ResultSet getResultSet() {
        return rs;
    }

    // Private constructor to initialize database connection
    private TicTacToeDataBase() throws SQLException {
        DriverManager.registerDriver(new ClientDriver());
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/TicTacToeDataBase", "root", "root");
    }

    // Synchronized method to get the singleton instance of the Database
    public synchronized static TicTacToeDataBase getDataBase() throws SQLException {
        if (instanceData == null) {
            instanceData = new TicTacToeDataBase();
        }
        return instanceData;
    }

    // Synchronized method to update the result set with player data
    public synchronized void updateResultSet() {
        try {
            this.pst = con.prepareStatement("Select * from player", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            this.rs = pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Synchronized method to get the count of offline users
    public synchronized int getCountOfOfflineUsers() {
        try {
            this.pst = con.prepareStatement("select count(*) from player where isactive = false", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet r = pst.executeQuery();
            return r.next() ? r.getInt(1) : -1;
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("catch getactive");
        }
        return -1;
    }

    // Synchronized method to get all active players
    public synchronized ResultSet getActivePlayers() {
        try {
            this.pst = con.prepareStatement("Select * from player where isactive = true", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return pst.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("catch getactive");
            return null;
        }
    }

    // Synchronized method to disable the database connection
    public synchronized void disableConnection() throws SQLException {
        changeStateToOffline();
        changeStateToNotPlaying();

        rs.close();
        pst.close();
        con.close();
        instanceData = null;
    }

    // Synchronized method to change all players' state to not playing
    public synchronized void changeStateToNotPlaying() {
        try {
            pst = con.prepareStatement("update player set isPlaying = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, "false");
            pst.executeUpdate();
            updateResultSet();
        } catch (SQLException ex) {
            System.out.println("change state to not playing");
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Synchronized method to change all players' state to offline
    public synchronized void changeStateToOffline() {
        try {
            pst = con.prepareStatement("update player set isActive = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, "false");
            pst.executeUpdate();
            updateResultSet();
        } catch (SQLException ex) {
            System.out.println("change state to offline");
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Synchronized method to log in a player
    public synchronized void login(String email, String password) throws SQLException {
        pst = con.prepareStatement("update player set isActive = ? where email = ? and password = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        pst.setString(1, "true");
        pst.setString(2, email);
        pst.setString(3, password);
        pst.executeUpdate();
        updateResultSet();
    }

    // Synchronized method to sign up a new player
    public synchronized void SignUp(String username, String email, String password) throws SQLException {
        
        pst = con.prepareStatement("insert into player(username, email, password) values(?, ?, ?)");
        pst.setString(1, username);
        pst.setString(2, email);
        pst.setString(3, password);
        pst.executeUpdate();
        login(email, password);
    }

    // Synchronized method to check if a player is already registered
    public synchronized String checkRegister(String username, String email) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        try {
            pstCheck = con.prepareStatement("select * from player where username = ? and email = ?");
            pstCheck.setString(1, username);
            pstCheck.setString(2, email);
            checkRs = pstCheck.executeQuery();
            if (checkRs.next()) {
                return "already signed-up";
            }
        } catch (SQLException ex) {
            System.out.println("here");
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Registered Successfully";
    }

    // Synchronized method to check if a player's login credentials are correct
    public synchronized String checkSignIn(String email, String password) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        if (!checkIsActive(email)) {
            try {
                pstCheck = con.prepareStatement("select * from player where email = ?");
                pstCheck.setString(1, email);
                checkRs = pstCheck.executeQuery();
                if (checkRs.next()) {
                    if (password.equals(checkRs.getString(4))) {
                        return "Logged in successfully";
                    }
                    return "Password is incorrect";
                }
                return "Email is incorrect";
            } catch (SQLException ex) {
                Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
                return "Connection issue, please try again later";
            }
        } else {
            System.out.println("This Email already sign-in " + checkIsActive(email));
            return "This Email is already sign-in";
        }
    }

    // Synchronized method to get a player's score by email
    public synchronized int getScore(String email) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        try {
            pstCheck = con.prepareStatement("select * from player where email = ?");
            pstCheck.setString(1, email);
            checkRs = pstCheck.executeQuery();
            checkRs.next();
            return checkRs.getInt(5);
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    // Synchronized method to update a player's score
    public synchronized void updateScore(String email, int score) {
        try {
            pst = con.prepareStatement("update player set score = ? where email = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setInt(1, score);
            pst.setString(2, email);
            pst.executeUpdate();
            updateResultSet();
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Synchronized method to get a player's email by username
    public synchronized String getEmail(String username) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        try {
            pstCheck = con.prepareStatement("select * from player where username = ?");
            pstCheck.setString(1, username);
            checkRs = pstCheck.executeQuery();
            checkRs.next();
            return checkRs.getString(3);
        } catch (SQLException ex) {
            System.out.println("Invalid Email address");
        }
        return null;
    }

    // Synchronized method to get a player's username by email
    public synchronized String getUserName(String email) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        try {
            pstCheck = con.prepareStatement("select * from player where email = ?");
            pstCheck.setString(1, email);
            checkRs = pstCheck.executeQuery();
            checkRs.next();
            return checkRs.getString(2);
        } catch (SQLException ex) {
            System.out.println("Invalid Email address");
        }
        return null;
    }

    // Synchronized method to set two players as playing
    public synchronized void makePlaying(String player1, String player2) {
        try {
            pst = con.prepareStatement("update player set isPlaying = true where email = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, player1);
            pst.executeUpdate();
            pst = con.prepareStatement("update player set isPlaying = true where email = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pst.setString(1, player2);
            pst.executeUpdate();
            updateResultSet();
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Method to check if a player is active (logged in)
    public Boolean checkIsActive(String email) {
        ResultSet checkRs;
        PreparedStatement pstCheck;

        try {
            pstCheck = con.prepareStatement("select isactive from player where email = ?");
            pstCheck.setString(1, email);
            checkRs = pstCheck.executeQuery();
            checkRs.next();
            return checkRs.getBoolean("isactive");
        } catch (SQLException ex) {
            System.out.println("Invalid Email address");
        }
        return false;
    }

    // Synchronized method to check if a player is playing
    public synchronized boolean checkPlaying(String player) {
        ResultSet checkAv;
        PreparedStatement pstCheckAv;

        try {
            pstCheckAv = con.prepareStatement("select * from player where username = ?");
            pstCheckAv.setString(1, player);
            checkAv = pstCheckAv.executeQuery();
            checkAv.next();
            return checkAv.getBoolean(4);
        } catch (SQLException ex) {
            Logger.getLogger(TicTacToeDataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
