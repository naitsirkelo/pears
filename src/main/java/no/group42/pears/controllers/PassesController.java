package no.group42.pears.controllers;

import com.google.api.services.walletobjects.Walletobjects;
import no.group42.pears.RestMethods;
import no.group42.pears.models.BalanceRequestModel;
import no.group42.pears.models.RemoveFromDbModel;
import no.group42.pears.services.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static no.group42.pears.Const.*;
import static no.group42.pears.Util.newJsonHeader;


/**
 * Controller class with responsibility of communicating updates to stored Google Pay cards.
 */
@RestController
@RequestMapping("/api/passes")
public class PassesController {

    @Autowired
    static
    FirebaseService firebaseService;

    /**
     * Used to update the balance visible on player pass
     * @param balanceRequestModel Request body must contain playerId, balance
     * @param httpServletResponse HttpServletResponse
     * @return empty string + status 200 if OK, status 400 if balance was not updated
     * @throws IOException Thrown when cant send error response
     */
    @PostMapping(value = "/update/balance")
    public static void updateBalance(@Valid @RequestBody BalanceRequestModel balanceRequestModel, HttpServletResponse httpServletResponse)
            throws IOException, ExecutionException, InterruptedException {

        Walletobjects client = RestMethods.getInstance().getWalletobjects();

        String url = NT_PROD_URL + CUSTOMER_BALANCE;

        HttpHeaders headers = newJsonHeader();
        headers.add("cookie", firebaseService.getCookie(balanceRequestModel.getPlayerId()));
    }


    /**
     * Removes a player from database.
     * @param removeFromDbModel Request body must contain playerId
     * @param response HttpServletResponse
     * @return empty body if successful, HttpServlet error if unsuccessful
     * @throws ExecutionException e
     * @throws InterruptedException i
     * @throws IOException i
     */
    @PostMapping(value = "/remove")
    public String removeSessionCookieFromDb(@Valid @RequestBody RemoveFromDbModel removeFromDbModel, HttpServletResponse response)
            throws ExecutionException, InterruptedException, IOException {
        if(firebaseService.removeCookie(removeFromDbModel.getPlayerId()) != OK) {
            response.sendError(BAD_REQUEST, REMOVE_FROM_DB_FAIL);
        }
        return REMOVE_FROM_DB_SUCCESS;
    }


}
