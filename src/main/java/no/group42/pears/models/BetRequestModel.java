package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Provides NTController with the required object structure, used as @RequestBody, that is automatically
 * serialized into JSON and passed back into the HttpResponse object.
 * Used in placeBet() and within the /NT/bets API endpoint.
 */
public class BetRequestModel {

    @NotNull @NotEmpty
    String gameId;
    @NotNull @NotEmpty
    String playerId;
    @NotNull @NotEmpty
    String rows;

    // Repeating weeks may be chosen for the Lotto bet.
    @NotNull @NotEmpty
    String weeks;

    public BetRequestModel(String gameId, String playerId, String rows, String weeks) {

        this.gameId = gameId;
        this.playerId = playerId;
        this.rows = rows;
        this.weeks = weeks;
    }

    public String getGameId() {

        return this.gameId;
    }

    public String getPlayerId() {

        return this.playerId;
    }

    public String getRows() {

        return this.rows;
    }

    public String getWeeks() {

        return this.weeks;
    }

}