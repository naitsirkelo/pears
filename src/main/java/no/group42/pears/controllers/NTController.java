package no.group42.pears.controllers;

import com.google.api.services.walletobjects.Walletobjects;
import com.google.api.services.walletobjects.model.LoyaltyObject;
import com.google.api.services.walletobjects.model.LoyaltyPoints;
import com.google.api.services.walletobjects.model.LoyaltyPointsBalance;
import no.group42.pears.Config;
import no.group42.pears.RestMethods;
import no.group42.pears.models.BetRequestModel;
import no.group42.pears.models.FnrLoginRequestModel;
import no.group42.pears.models.OtpLoginRequestModel;
import no.group42.pears.models.PlayerRequestModel;
import no.group42.pears.services.FirebaseService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static no.group42.pears.Const.*;
import static no.group42.pears.Util.*;


/**
 * Class containing functions for communicating with Norsk Tipping and transmitting requests from Firebase frontend.
 * Main API endpoint is api/NT/.
 */
@RestController
@RequestMapping("/api/NT")
public class NTController {

    @Autowired
    FirebaseService firebaseService;

    /**
     * After using the /login/fnr endpoint, this endpoint can be used to validate your session
     * cookie so that user can make other calls and place bets.
     *
     * @param otpLoginRequestModel Request must contain a field called "cookie"
     * @param otp                  The otp receiver on sms should be passed in url
     * @return returns the status response that came from NTs api
     */
    @PostMapping(value = "/login/fnr/{otp}")
    public String loginOtp(@Valid @RequestBody OtpLoginRequestModel otpLoginRequestModel, @PathVariable("otp") String otp, HttpServletResponse httpServletResponse) throws IOException {

        String url = Config.getInstance().getNTProdUrl() + "login/v1/fnr/" + otp;
        String cookie = otpLoginRequestModel.getCookie();

        HttpHeaders headers = newJsonHeader();
        headers.add("cookie", cookie);

        //POST request to NT api containing the OTP and session cookie, this will result in
        //a successful login, and the session cookie can now be used to make other calls
        HttpEntity<String> response = Config.getInstance()
                .getRestTemplate()
                .exchange(url, HttpMethod.POST, makeRequest("", headers), String.class);

        url = NT_PROD_URL + "customer/v1?sysGenAlias=false&refreshAlias=false";

        HttpEntity<String> resp = Config.getInstance()
                .getRestTemplate()
                .exchange(url, HttpMethod.GET, makeRequest("", headers), String.class);

        //Get the customer from JSON response
        JSONObject customer = new JSONObject(resp.getBody())
                .getJSONObject("data")
                .getJSONObject("result")
                .getJSONObject("customer");

        //Get the phone number
        JSONObject contactInfo = customer.getJSONObject("contactInfo");
        String phone = contactInfo.getString("phoneMobile");

        //Remove land code
        phone = phone.substring(phone.length() - PHONE_NO_LENGTH);


        //Add the session cookie to database for use in future calls to NT api
        try {
            firebaseService.addCookie(new PlayerRequestModel(createDatabaseId(phone), cookie));
        } catch (ExecutionException | InterruptedException e) {
            httpServletResponse.sendError(BAD_REQUEST, "Failed to contact database.");
        }

        return response.getBody();
    }


    /**
     * After signing in using a one time password and personId, this endpoint can be used
     * to extract customers name, player card number, account balance and charity receiver.
     * <p>
     * Request to endpoint must contain a field "cookie" containing the validated cookie in
     * order to extract the information
     *
     * @param otpLoginRequestModel Request body must contain a field "cookie"
     * @param httpServletResponse  Used to send errors in response
     * @return Json object containing fields name, balance, cardNumber and charityReceiver
     */
    @PostMapping(value = "/customer/info")
    public String getCustomerInfo(@Valid @RequestBody OtpLoginRequestModel otpLoginRequestModel, HttpServletResponse httpServletResponse) {

        String url = Config.getInstance().getNTProdUrl() + "customer/v1?sysGenAlias=false&refreshAlias=false";
        String cookie = otpLoginRequestModel.getCookie();

        HttpHeaders headers = newJsonHeader();
        headers.add("cookie", cookie);

        HttpEntity<String> resp = Config.getInstance()
                .getRestTemplate()
                .exchange(url, HttpMethod.GET, makeRequest("", headers), String.class);

        //Get the different parts of the JSON response
        JSONObject jsonRes = new JSONObject(resp.getBody());
        JSONObject data = jsonRes.getJSONObject("data").getJSONObject("result");
        JSONObject customer = data.getJSONObject("customer");

        //Get the phone number
        JSONObject contactInfo = customer.getJSONObject("contactInfo");
        String phone = contactInfo.getString("phoneMobile");

        //Remove land code
        phone = phone.substring(phone.length() - PHONE_NO_LENGTH);

        //Get player name
        String firstName = customer.getString("firstName");
        String lastName = customer.getString("lastName");
        String playerName = firstName + " " + lastName;

        //Get player card number
        JSONArray playerCards = data.getJSONArray("playerCards");
        //Get the last issued player card registered on user
        JSONObject playerCard = playerCards.getJSONObject(playerCards.length() - 1);
        String cardNumber = playerCard.getString("cardNumber");

        //Get account balance
        JSONObject accountBalance = data.getJSONObject("accountBalance");
        String balance = Integer.toString(accountBalance.getInt("balance"));

        //Get charity receiver, can be non existent
        String charityReceiverName = "";
        if (data.has(CHARITY_RECEIVER)) {
            JSONObject charityReceiver = data.getJSONObject(CHARITY_RECEIVER);
            charityReceiverName = charityReceiver.getString("name");
        }

        //Create JSON response
        httpServletResponse.setStatus(OK);
        jsonRes = new JSONObject();
        jsonRes.put("playerName", playerName);
        jsonRes.put("balance", balance);
        jsonRes.put("cardNumber", cardNumber);
        jsonRes.put(CHARITY_RECEIVER, charityReceiverName);
        jsonRes.put("playerId", createDatabaseId(phone));

        return jsonRes.toString();
    }


    /**
     * Creates a POST request to Norsk Tippings /api/login/vi/fnr endpoint.
     * Norsk Tipping send a One Time Password to the phone number mapped to given personId
     *
     * @param fnrLoginRequestModel Needs to have a json body containing field "personId"
     * @return JSON body containing field "cookie"
     */
    @PostMapping(value = "/login/fnr")
    public String loginFnr(@Valid @RequestBody FnrLoginRequestModel fnrLoginRequestModel) {

        //Create the url to make call to
        String url = Config.getInstance().getNTProdUrl() + "login/v1/fnr";

        JSONObject jsonReq = new JSONObject()
                .put("personId", fnrLoginRequestModel.getPersonId());

        HttpEntity<String> response = Config.getInstance()
                .getRestTemplate()
                .exchange(url, HttpMethod.POST, makeRequest(jsonReq.toString(), newJsonHeader()), String.class);

        //Cookie called JSESSIONID is extracted
        String cookie = response.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        return new JSONObject(response.getBody())
                .put("cookie", cookie)
                .toString();
    }


    /**
     * Function called by api/NT/bets endpoint. A bet is placed from the authorized user account,
     * given an authorized cookie. Correct JSON body is retrieved based on the requested game from the client.
     *
     * @param betRequestModel Needs to correspond to the JSON body given by BetRequestModel
     * @return JSON Object as a string response, to be parsed by client application
     */
    @PostMapping(value = "/bets")
    public String placeBet(@Valid @RequestBody BetRequestModel betRequestModel) throws ExecutionException, InterruptedException {

        // Defining constant config String pointing to Norsk Tipping production API.
        String url = NT_PROD_URL + "bets";

        String playerId = betRequestModel.getPlayerId();

        // Initializing header and retrieving stored cookie from Firebase
        HttpHeaders headers = newJsonHeader();
        headers.add("cookie", firebaseService.getCookie(playerId));

        // Create JSON object with structured betting data.
        JSONObject lotteryJson = generateLotteryJson(betRequestModel);

        HttpEntity<String> request = new HttpEntity<>(lotteryJson.toString(), headers);

        ResponseEntity<String> response = Config.getInstance().
                getRestTemplate().exchange(url, HttpMethod.POST, request, String.class);

        // Check if response of request was OK, and in that case update balance.
        if (response.getStatusCode() == HttpStatus.OK) {

            // TODO: Handle status String returned from updateBalance(...)
            String r = updateBalance(playerId);
        }

        return response.getBody();
    }


    /**
     * Initializing and structuring the JSON body to be able to place a bet for the authorized user, based on the
     * decided gameId given by the BetRequestModel.
     *
     * @param betRequestModel Validated class model from front-end.
     * @return JSONObject with a given data structure to place a bet.
     */
    public static JSONObject generateLotteryJson(@Valid BetRequestModel betRequestModel) {

        int rows = Integer.parseInt(betRequestModel.getRows());
        String gameId = betRequestModel.getGameId();

        JSONObject json = new JSONObject();
        JSONArray wagerItems = new JSONArray();
        JSONObject item = new JSONObject();

        item.put("gameId", gameId);

        int rowPrice;
        List<String> numberRows;

        // Defining price per row and generating the lottery coupon based on the game ID provided.
        switch (Integer.parseInt(gameId)) {

            case VIKINGLOTTO_ID:    // 18
                rowPrice = VIKINGLOTTO_ROW;
                numberRows = generateCoupon(rows,
                        VIKINGLOTTO_MAX,
                        VIKINGLOTTO_LENGTH,
                        VIKINGLOTTO_EXTRA,
                        VIKINGLOTTO_EXTRA_MAX);
                break;

            case EUROJACKPOT_ID:    // 16
                rowPrice = EUROJACKPOT_ROW;
                numberRows = generateCoupon(rows,
                        EUROJACKPOT_MAX,
                        EUROJACKPOT_LENGTH,
                        EUROJACKPOT_EXTRA,
                        EUROJACKPOT_EXTRA_MAX);
                break;

            default:    // LOTTO_ID = 1
                rowPrice = LOTTO_ROW;
                numberRows = generateCoupon(rows,
                        LOTTO_MAX,
                        LOTTO_LENGTH,
                        LOTTO_EXTRA,
                        LOTTO_EXTRA_MAX);

                // Number of times the bet placed will be used with the same coupon.
                // Default is 1 x amount of weeks chosen.
                item.put("multiplier", 1);

                break;
        }

        int weeks = Integer.parseInt(betRequestModel.getWeeks());
        // Number of weeks this coupon will be registered for.
        item.put("numberOfDraws", weeks);

        // Structuring JSON object and placing values in 'wagerItems'.
        item.put("selections", new JSONArray(numberRows));

        item.put("rowPrice", rowPrice);
        item.put("couponPrice", rowPrice * rows * weeks);

        // Define bet to be placed as normal, not quick pick, favourite numbers etc.
        item.put("playMethod", PLAY_METHOD);

        // Defining play system to be used with NT - 0 as default.
        item.put("playSystem", 0);

        wagerItems.put(item);
        json.put("wagerItems", wagerItems);

        JSONObject paymentInfo = new JSONObject();
        // Define the way the bet will be paid for. Getting funds directly from player account
        paymentInfo.put("paymentMethod", PAYMENT_METHOD);

        json.put("paymentInfo", paymentInfo);

        return json;
    }


    /**
     * Generating the coupon of lottery numbers to be stored within 'selections' of the JSON body.
     *
     * @param rows        Number of rows to generate within the coupon.
     * @param max         Random int will be generated between 1 and 'max'.
     * @param rowLength   Length of each lottery row.
     * @param extraLength Length of extra set of numbers. Checks if > 0.
     * @param maxExtra    Each random int of extra will be generated between 1 and 'maxExtra'.
     * @return ArrayList of Strings with generated lottery rows.
     */
    public static List<String> generateCoupon(int rows, int max, int rowLength, int extraLength, int maxExtra) {

        ArrayList<String> coupon = new ArrayList<>();

        // Loop through the amount of rows specified and make a new array.
        for (int i = 0; i < rows; i++) {

            ArrayList<Integer> row = new ArrayList<>();

            // Generating unique lottery numbers and adding them to the row within the length specified.
            while (row.size() < rowLength) {
                int r = new Random().nextInt(max) + 1;
                if (!row.contains(r)) {
                    row.add(r);
                }
            }

            // Sorting row of numbers in an ascending order.
            Collections.sort(row);

            // Checking if the game type requires an extra set of numbers.
            if (extraLength > 0) {
                // Adding additional numbers based on the requirement.
                ArrayList<Integer> extraNumbers = new ArrayList<>();
                while (extraNumbers.size() < extraLength) {
                    int r = new Random().nextInt(maxExtra) + 1;
                    if (!extraNumbers.contains(r)) {
                        extraNumbers.add(r);
                    }
                }

                // Sort the extra numbers and add them to the current row.
                Collections.sort(extraNumbers);
                row.addAll(extraNumbers);
            }

            // Join the array with ';' as a delimiter and push the row to the coupon.
            coupon.add(row.stream().map(String::valueOf).collect(Collectors.joining(";")));
        }
        return coupon;
    }


    /**
     * Used to update the balance visible on player pass
     *
     * @return empty string + status 200 if OK, status 400 if balance was not updated
     * @throws ExecutionException   Thrown when cant send error response
     * @throws InterruptedException Throws when update is interrupted
     */
    public String updateBalance(String playerId) throws ExecutionException, InterruptedException {

        Walletobjects client = RestMethods.getInstance().getWalletobjects();

        String url = NT_PROD_URL + CUSTOMER_BALANCE;

        HttpHeaders headers = newJsonHeader();
        headers.add("cookie", firebaseService.getCookie(playerId));

        HttpEntity<String> response = Config.getInstance()
                .getRestTemplate()
                .exchange(url, HttpMethod.GET, makeRequest("", headers), String.class);

        JSONObject resp = new JSONObject(response.getBody());
        String balance = Integer.toString(resp.getJSONObject("data").getInt(BALANCE));
        try {
            //Update balance in user pass
            LoyaltyObject s = new LoyaltyObject()
                    .setLoyaltyPoints(new LoyaltyPoints()
                            .setBalance(new LoyaltyPointsBalance().setString(balance))
                            .setLabel("kr"));

            // Using patch() instead of update() allows us to update the pass without retrieving the object firsts
            client.loyaltyobject()
                    .patch(buildObjectIdString(playerId), s)
                    .execute();
            return BALANCE_UPDATE_SUCCESS;

        } catch (IOException e) {
            return BALANCE_UPDATE_FAIL;
        }
    }


}
