package no.group42.pears;

import com.google.api.client.util.PemReader;
import com.google.api.client.util.SecurityUtils;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;


/**
 * Defines constants used in Controller classes for making loyalty objects, accessing service account,
 * connecting with either Norsk Tipping production or testing, defining JWT type and scopes, and
 * retrieving REST template for requests.
 */
public class Config {

    private static Config config = new Config();
    private String serviceAccountEmailAddress;
    private String serviceAccountFile;
    private String issuerId;
    private List<String> origins;
    private String audience;
    private String jwtType;
    private List<String> scopes;
    private RSAPrivateKey serviceAccountPrivateKey;
    private String loyaltyClass;
    private String NTProdUrl;   // Production call to Norsk Tipping
    private RestTemplate restTemplate;


    private Config() {
        // Identifiers of Service account
        this.serviceAccountEmailAddress = "test-643@zeta-flare-265209.iam.gserviceaccount.com";
        this.serviceAccountFile = String.format("src/main/resources/%s", "privatekey.json");
        this.NTProdUrl = "https://www.norsk-tipping.no:443/mobile-channel/api/";

        // Identifier of Google Pay API for Passes Merchant Center
        this.issuerId = "3388000000001450143";

        // List of origins for save to phone button. Used for JWT
        //// See https://developers.google.com/pay/passes/reference/s2w-reference
        this.origins = new ArrayList<>();
        this.origins.add("https://zeta-flare-265209.firebaseapp.com");
        this.origins.add("https://zeta-flare-265209.web.app");
        this.origins.add("http://localhost:3000");


        // Constants that are application agnostic. Used for JWT
        this.loyaltyClass = "NT_PC";
        this.audience = "google";
        this.jwtType = "savetowallet";
        this.scopes =  new ArrayList<String>() {
            private static final long serialVersionUID = 1L;
            {
                add("https://www.googleapis.com/auth/wallet_object.issuer");
            }
        };

        this.restTemplate = new RestTemplate();

        // Load the private key as a java RSAPrivateKey object from service account file
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(this.serviceAccountFile)));
            JSONObject privateKeyJson = new JSONObject(content);
            String privateKeyPkcs8 = (String) privateKeyJson.get("private_key");
            Reader reader = new StringReader(privateKeyPkcs8);
            PemReader.Section section = PemReader.readFirstSectionAndClose(reader, "PRIVATE KEY");
            byte[] bytes = section.getBase64DecodedBytes();
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory keyFactory = SecurityUtils.getRsaKeyFactory();
            this.serviceAccountPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Getting Config as singleton. Returning same object, or creating new for the first time if not already existing.
     *
     * @return  Config object as singleton instance
     */
    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }


    public String getServiceAccountEmailAddress() {
        return this.serviceAccountEmailAddress;
    }

    public RSAPrivateKey getServiceAccountPrivateKey() {
        return this.serviceAccountPrivateKey;
    }

    public String getServiceAccountFile() {
        return this.serviceAccountFile;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public List<String> getOrigins() {
        return this.origins;
    }

    public String getAudience() {
        return this.audience;
    }

    public String getJwtType() {
        return this.jwtType;
    }

    public List<String> getScopes() {
        return this.scopes;
    }

    public String getLoyaltyClass() {
        return this.loyaltyClass;
    }

    public String getNTProdUrl() {
        return NTProdUrl;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

}