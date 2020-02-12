package no.group42.pears.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Provides NTController with the required object structure, used as @RequestBody, that is automatically
 *  serialized into JSON and passed back into the HttpResponse object.
 *  Used in loginOtp() and within the /login/fnr/{otp} API endpoint.
 */
public class OtpLoginRequestModel {
    @NotNull @NotEmpty
    private String cookie;

    public String getCookie() {
        return cookie;
    }
}
