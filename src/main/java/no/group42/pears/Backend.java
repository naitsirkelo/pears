package no.group42.pears;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class to launch Spring application which enables API endpoint calls.
 * System deployed to Heroku: https://google-passes.herokuapp.com.
 */
@SpringBootApplication
public class Backend {

    public static void main(String[] args) {
        SpringApplication.run(Backend.class, args);
        //demoFatJwt();
    }

    // TESTING LINK
    private static final String SAVE_LINK = "https://pay.google.com/gp/v/save/"; // Save link that uses JWT. See https://developers.google.com/pay/passes/guides/get-started/implementing-the-api/save-to-google-pay#add-link-to-email

    public static void demoFatJwt() {
        Jwt jwt = new Jwt();
        String fatJwt = jwt.makeJwt("Ola Nordmann", "NT99999999", "987654321", "9000", "Veldedighet AS");


        if (fatJwt != null) {
            System.out.println(String.format("jwt:\n%s\n", fatJwt));
            System.out.println(String.format("save link:\n%s%s\n", SAVE_LINK, fatJwt));
        }
    }
}

