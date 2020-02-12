package no.group42.pears;

import com.google.api.services.walletobjects.model.*;

import java.util.ArrayList;
import java.util.List;

import static no.group42.pears.Util.createBarcodeValue;


/**
 * Contains functions used to create loyalty object/class resources
 */
public class ResourceDefinitions {
    private static Config config = Config.getInstance();

    private ResourceDefinitions() {}

    /**
     * Define a Loyalty Object
     *
     * @param playerName The real playerName of the pass holder
     * @param cardNumber The Norsk Tipping account number of the pass holder
     * @return The payload representing a loyalty object resource
     */
    public static LoyaltyObject makeLoyaltyObjectResource(String playerName, String playerId, String cardNumber, String balance, String charityReceiver) {
        // TODO get saldo from norsk tipping
        String classId = String.format("%s.%s", config.getIssuerId(), config.getLoyaltyClass());
        String objectId = String.format("%s.%s", config.getIssuerId(), playerId);
        List<TextModuleData> textModuleDataList = new ArrayList<>();

        return new LoyaltyObject()
                // Required
                .setId(objectId).setClassId(classId).setState("active")
                // Optional properties
                .setAccountId(cardNumber).setAccountName(playerName)
                // TODO figure out barcode stuff
                .setBarcode((new Barcode()).setType("ITF_14").setValue(createBarcodeValue(cardNumber))
                        .setAlternateText(createBarcodeValue(cardNumber)))
                .setLoyaltyPoints((new LoyaltyPoints()).setBalance((new LoyaltyPointsBalance()).setString(balance))
                        .setLabel("NOK"))
                .setTextModulesData(textModuleDataList)
                .setSmartTapRedemptionValue(playerId);
    }


}