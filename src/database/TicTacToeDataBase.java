/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.derby.jdbc.ClientDriver; 
/**
 *
 * @author Mai Ibrahem
 */
public class TicTacToeDataBase {
    private static TicTacToeDataBase instanceData; // Singleton instance
    private Connection con;               // Database connection
    private ResultSet rs;                 // Result set for queries
    private PreparedStatement pst;        // Prepared statement for queries
    
    public TicTacToeDataBase() throws SQLException {
        DriverManager.registerDriver(new ClientDriver());
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/TicTacToeDataBase", "root", "root");
    }

    
    public synchronized void SignUp(String email, String username, String password) throws SQLException {
        
        pst = con.prepareStatement("insert into player(email, username, password) values(?, ?, ?)");
        pst.setString(1, email);
        pst.setString(2, username);
        pst.setString(3, password);
        pst.executeUpdate();
        
    }
    
    public synchronized String checkRegister(String email, String username) {
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
    public static synchronized TicTacToeDataBase getInstance() throws SQLException {
        if (instanceData == null) {
            instanceData = new TicTacToeDataBase();
        }
        return instanceData;
    }
}
