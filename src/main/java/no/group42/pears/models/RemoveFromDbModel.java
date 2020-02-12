package no.group42.pears.models;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides PassesController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 *  Used in removeSessionCookieFromDb
 */
public class RemoveFromDbModel {
    @NotEmpty @NotNull
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }
}
