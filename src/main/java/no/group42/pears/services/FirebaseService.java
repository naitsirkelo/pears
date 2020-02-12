package no.group42.pears.services;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import no.group42.pears.models.PlayerRequestModel;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;


@Service
public class FirebaseService {
    private Firestore db;

    public FirebaseService() throws Exception {
        initDb();
    }


    /**
     * Initializing database settings with provided Firebase link and private key in JSON file.
     * @throws IOException i
     */
    private void initDb() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream serviceAccount = classLoader.getResourceAsStream("privatekey.json");
        assert serviceAccount != null;
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setDatabaseUrl("https://zeta-flare-265209.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);

        this.db = FirestoreClient.getFirestore();
    }


    /**
     * Used to add a new player record to the database,
     * For example: session cookie.
     *
     * @param playerRequestModel
     * @return 200 if ok, 400 if record did not get added to db
     * @throws ExecutionException Thrown if future snapshot can not be taken
     * @throws InterruptedException Thrown if future snapshot can not be taken
     */
    public int addCookie(PlayerRequestModel playerRequestModel) throws ExecutionException, InterruptedException {
        String id = playerRequestModel.getPlayerId();
        CollectionReference colRef = db.collection("passes");
        colRef.document(id).set(playerRequestModel);

        //Try to receive the newly created document
        DocumentReference docRef = colRef.document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        return document.exists() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST;
    }


    /**
     * Delete a cookie from database
     *
     * @param playerId Players id, NT + phone number
     * @return 200 if successfully deleted, 400 if unsuccessful
     * @throws ExecutionException Thrown if future snapshot can not be taken
     * @throws InterruptedException Thrown if future snapshot can not be taken
     */
    public int removeCookie(String playerId) throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection("passes").document(playerId);
        docRef.delete();
        //Try to receive the deleted document
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        return document.exists() ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_OK;

    }


    /**
     * Accesses database under current playerId to retrieve the stored cookie. Used when purchasing
     * lottery coupons, and using the cookie to check for authorized user.
     *
     * @param playerId  Unique player ID: NT + phone number
     * @return Stored cookie for authorizing placement of bet
     * @throws ExecutionException Thrown if future snapshot can not be taken
     * @throws InterruptedException Thrown if future snapshot can not be taken
     */
    public String getCookie(String playerId) throws ExecutionException, InterruptedException {
        // Accessing Firebase collection and getting document containing playerId and cookie.
        DocumentReference docRef = db.collection("passes").document(playerId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        // If the document exists: Return the stored cookie
        return document.exists() ? (String) document.get("cookie") : "Couldn't get cookie!";
    }


}
