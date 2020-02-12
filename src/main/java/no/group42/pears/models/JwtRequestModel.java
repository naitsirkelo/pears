package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides JwtController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 */
public class JwtRequestModel {
    @NotNull @NotEmpty
    private String playerName;
    @NotNull @NotEmpty
    private String cardNumber;
    @NotNull @NotEmpty
    private String playerId; //Format is NT + phone number
    @NotNull @NotEmpty
    private String balance;
    @NotNull
    private String charityReceiver;



    public String getPlayerName() { return playerName; }

    public String getPlayerId() {
        return playerId;
    }

    public String getBalance() {
        return balance;
    }

    public String getCharityReceiver() {
        return charityReceiver;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}
