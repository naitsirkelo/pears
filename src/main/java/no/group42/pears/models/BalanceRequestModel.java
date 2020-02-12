package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides PassesController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 *  Used in updateBalance() and within the /passes API endpoint.
 */
public class BalanceRequestModel {
    @NotNull @NotEmpty
    String playerId;

    /**
     * Constructor for use in updating balance internally, after placing a bet by user.
     * @param playerId String of NT + phone number.
     */
    public BalanceRequestModel(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() { return this.playerId; }

}
