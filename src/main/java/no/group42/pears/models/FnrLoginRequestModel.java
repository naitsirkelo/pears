package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides NTController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 *  Used in the login process and within the /login/fnr API endpoint.
 */
public class FnrLoginRequestModel {
    @NotNull @NotEmpty
    private String personId;

    public String getPersonId() {
        return personId;
    }
}
