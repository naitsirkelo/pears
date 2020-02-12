package no.group42.pears;

import com.google.api.client.json.GenericJson;
import com.google.api.services.walletobjects.Walletobjects;
import com.google.api.services.walletobjects.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.crypto.RsaSHA256Signer;
import org.joda.time.Instant;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static no.group42.pears.Util.buildObjectIdString;
import static no.group42.pears.Util.createBarcodeValue;

/**
 * Object class for defining JSON Web Token (JWT). Utilized in saving the Google Pay card to a phone.
 */
public class Jwt {
    // TODO find out how to use setLevel() and Level
    private static final Logger LOGGER = Logger.getLogger(Jwt.class.getName());

    private String audience;
    private String type;
    private List<String> origins;
    private Instant iat;
    private JsonObject payload;
    private RsaSHA256Signer signer;

    private static String LOY_CLA = "loyaltyClasses";
    private static String LOY_OBJ = "loyaltyObjects";

    /**
     *
     */
    // TODO javadoc
    public Jwt() {
        Config config = Config.getInstance();

        this.audience = config.getAudience();
        this.type = config.getJwtType();
        String iss = config.getServiceAccountEmailAddress();
        this.origins = config.getOrigins();
        this.iat = new Instant(
                Calendar.getInstance().getTimeInMillis() - 5000L);
        this.payload = new JsonObject();

        try {
            this.signer = new RsaSHA256Signer(iss,
                    null, config.getServiceAccountPrivateKey());
        } catch (InvalidKeyException e) {
            LOGGER.log(Level.FINE, "Invalid key", e);
        }
    }

    /**
     *
     * @param resourcePayload JSONElement
     */
    public void addLoyaltyObject(JsonElement resourcePayload) {
        if (this.payload.get(LOY_OBJ) == null) {
            JsonArray loyaltyObjects = new JsonArray();
            this.payload.add(LOY_OBJ, loyaltyObjects);
        }

        JsonArray newPayload = (JsonArray) this.payload.get(LOY_OBJ);
        newPayload.add(resourcePayload);
        this.payload.add(LOY_OBJ, newPayload);
    }

    /**
     *
     * @return String of unsigned JSON web token
     */
    public JsonToken generateUnsignedJwt() {
        JsonToken token = new JsonToken(this.signer);
        Gson gson = new Gson();

        token.setAudience(this.audience);
        token.setParam("typ", this.type);
        token.setIssuedAt(this.iat);
        token.getPayloadAsJsonObject().add("payload", this.payload);
        token.getPayloadAsJsonObject().add("origins", new Gson().toJsonTree(this.origins));

        return token;
    }

    /**
     *
     * @return  String of signed JSON web token
     */
    public String generateSignedJwt() {
        JsonToken jwtToSign = this.generateUnsignedJwt();
        String signedJwt = null;

        try {
            signedJwt = jwtToSign.serializeAndSign();
        } catch (SignatureException e) {
            LOGGER.log(Level.FINE, "Error signing JWT", e);
        }

        return signedJwt;
    }

    /**
     * Generates a signed JWT (the JWT is fat type)
     *
     * @return String signedJwt - a signed JWT
     */
    public String makeJwt(String playerName, String playerId, String accountNr, String balance, String charityReceiver) {
        // Create the loyalty card object resource for the user
        GenericJson objectResourcePayload = ResourceDefinitions.makeLoyaltyObjectResource(playerName, playerId, accountNr, balance, charityReceiver);

        // Use gson to turn the class/object instances into JSON representation
        Gson gson = new Gson();

        // Add the loyalty card information to the JWT
        this.addLoyaltyObject(gson.toJsonTree(objectResourcePayload));

        //Updates pass if loyalty object already exists. If not, does nothing
        updatePass(playerName, playerId, accountNr, balance, charityReceiver);

        // Sign JSON to make signed JWT
        return this.generateSignedJwt();
    }


    /**
     * Used to update passes that already exists.
     * Without this we are unable to update already existing passes
     *
     * @param playerName player full playerName
     * @param playerId player playerId, NT + phone number
     * @param cardNumber players 9 digit player card number
     * @param balance current player account balance
     * @param charityReceiver players desired charity receiver, aka "Grasrotandel"
     */
    public void updatePass(String playerName, String playerId, String cardNumber, String balance, String charityReceiver) {
        Walletobjects client = RestMethods.getInstance().getWalletobjects();

        LoyaltyObject s = new LoyaltyObject()
                .setLoyaltyPoints(new LoyaltyPoints().setBalance(new LoyaltyPointsBalance().setString(balance)).setLabel("NOK"))
                .setAccountName(playerName)
                .setBarcode((new Barcode()).setType("ITF_14").setValue(createBarcodeValue(cardNumber))
                .setAlternateText(createBarcodeValue(cardNumber)))
                .setTextModulesData(new ArrayList<>());

        if(!charityReceiver.equals("")) {
            s.setSecondaryLoyaltyPoints(new LoyaltyPoints().setBalance(new LoyaltyPointsBalance().setString(charityReceiver)).setLabel("Grasrotandel"));
        } else {
            s.setSecondaryLoyaltyPoints(new LoyaltyPoints().setBalance(new LoyaltyPointsBalance().setString("Not available")).setLabel("Grasrotandel"));
        }

        // Tries to update loyalty object. If non existent, does nothing
        try {
            client.loyaltyobject().patch(buildObjectIdString(playerId), s).execute();
        } catch (IOException ignored) {
        }
    }
}