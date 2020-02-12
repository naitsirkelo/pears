package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides NTController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 *  Used in getCustomerInfo() and within the /customer/info API endpoint.
 */
public class PlayerRequestModel {
    @NotNull @NotEmpty
    private String playerId;
    @NotNull @NotEmpty
    private String cookie;

    public PlayerRequestModel(String playerId, String cookie) {
        this.playerId = playerId;
        this.cookie = cookie;
    }

    public String getCookie() {
        return cookie;
    }

    public String getPlayerId() {
        return playerId;
    }
}
