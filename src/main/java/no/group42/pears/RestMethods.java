package no.group42.pears;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.walletobjects.Walletobjects;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Class instantiated as a singleton, giving the function of making and getting OAuth token,
 * and getting Wallet objects by HttpTransport.
 */
public class RestMethods {
    private static HttpTransport httpTransport = null;
    private static RestMethods restMethods = new RestMethods();

    private RestMethods() {
        // Create an httpTransport which will be used for the REST call
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Getting RestMethods as singleton. Returning same object, or creating new
     * for the first time if not already existing.
     *
     * @return  RestMethods object as singleton instance
     */
    public static RestMethods getInstance() {
        if (restMethods == null) {
            restMethods = new RestMethods();
        }
        return restMethods;
    }


    /**
     * Preparing server-to-server authorized API call with OAuth 2.0
     *
     * @return Service Account credential for OAuth 2.0 signed JWT grants.
     */
    private GoogleCredentials makeOauthCredential() {
        Config config = Config.getInstance();
        GoogleCredentials credentials = null;

        try {
            credentials = GoogleCredentials
                    .fromStream(new FileInputStream(config.getServiceAccountFile()))
                    .createScoped(config.getScopes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return credentials;
    }


    /**
     * Gets an Oauth access token that can be used to authorize API requests
     *
     * @return An AccessToken ready to use for API requests
     */
    public AccessToken getOauthAccessToken() {
        GoogleCredentials credential = this.makeOauthCredential();

        try {
            credential.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return credential.getAccessToken();
    }


    /**
     * Creates and returns a Walletobjects object that can be used to make
     * requests towards Google Pay
     *
     * @return A Walletobjects instance
     */
    public Walletobjects getWalletobjects() {
        GoogleCredentials credentials = this.makeOauthCredential();
        HttpRequestInitializer initializer = new HttpCredentialsAdapter(credentials);

        return new Walletobjects.Builder(httpTransport, new GsonFactory(), initializer)
                .build();
    }


}