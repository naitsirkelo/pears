package no.group42.pears;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.logging.Logger;


/**
 * Util class containing useful methods for the project
 */
public class Util {

    /**
     * The barcode on the physical player cards that you can use in stores follow the following format:
     * 000 + player card number + 2x control digits.
     *
     * The two control digits are calculated by using the CRC-CCITT method.
     *
     * This function adds the three leading zeros and calculates and adds the two correct controll digits
     * to the card number.
     *
     * @param cardNumber The 9 digit, unprocessed card number received from NT api
     * @return Barcode string in format 000 + cardNumber + 2x Control digits, valid per ITF14 spec
     */
    public static String createBarcodeValue(String cardNumber) {

        //Add leading zeros to card number per spec
        String barcode = "000" + cardNumber;

        /*Following code is copied from https://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java.html */

        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        byte[] bytes = barcode.getBytes();

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;

        //By using Modulo 100, we get the two check digits we need
        crc = crc % 100;

        return barcode + crc;

        /* ********************************************************* */

    }

    /**
     * Getting the unique string of issuer ID concatinated with unique user ID.
     *
     * @param customerId    Unique user ID
     * @return  Singleton config instance.
     */
    public static String buildObjectIdString(String customerId) {
        return Config.getInstance().getIssuerId() + "." + customerId;
    }


    /**
     * Use to create a HttpHeaders object with setContent = application/json
     * and setAccept = application/json
     *
     * @return newly created HttpHeaders object
     */
    public static HttpHeaders newJsonHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }


    /**
     * Function for creating an HttpEntity request
     * @param requestBody Desired request body
     * @param headers Any headers wanted in request
     * @return HttpEntity containing request body and headers
     */
    public static HttpEntity makeRequest(String requestBody, HttpHeaders headers) {
        return new HttpEntity<>(requestBody, headers);
    }


    /**
     * Used to place an id tag in front of phone number to be used as an id in database
     * ex. 99887766 -> NT99887766
     *
     * @param phoneNumber phone number in string format, without country code
     * @return generated id
     */
    public static String createDatabaseId(String phoneNumber) {
        return "NT" + phoneNumber;
    }


    /**
     * Prints JSON objects nicely. For testing purposes.
     *
     * @param json Object to print
     */
    public static void printJson(JSONObject json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(String.valueOf(json));
        System.out.println(gson.toJson(je));
    }


    static public String getStringResponse(HttpURLConnection http) {

        String resp = "";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            resp = response.toString();

        } catch (IOException e) {
            Logger.getLogger(e.getMessage());
        }
        return resp;
    }

}
