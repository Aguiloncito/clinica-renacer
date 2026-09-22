package main.java.org.renacer.clinica.renacer.config;

/**
 * @author PC
 */
public class Credentials {

    public static final String URL_DB = System.getenv("URL_DB") + "/clinica_renacer_in4bm";
    public static final String USER_DB = System.getenv("USER_DB");
    public static final String PASS_DB = System.getenv("PASS_DB");

}

