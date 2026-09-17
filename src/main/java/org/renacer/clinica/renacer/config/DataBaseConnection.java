/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.org.renacer.clinica.renacer.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author AGUILON
 */
public class DataBaseConnection {
    //atributos 
    private static Connection connection;
    /*
    el constructor tiene que ser privado, esto para evitar
    que se creen instancias de esta clase
    */
    
    private DataBaseConnection(){}
    //metodo
    public static Connection getConnectionDataBase() throws SQLException{
        if(connection == null || connection.isClosed()){
            connection = DriverManager.getConnection(Credentials.URL_DB, Credentials.USER_DB, Credentials.PASS_DB);
        }
        return connection;
    }
}
