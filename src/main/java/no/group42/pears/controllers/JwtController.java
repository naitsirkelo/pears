package no.group42.pears.controllers;

import no.group42.pears.Jwt;
import no.group42.pears.models.JwtRequestModel;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * Controller class with responsibility of receiving api/jwt endpoints calls and creating the JSON Web Token.
 */
@RestController
@RequestMapping("/api/jwt")
public class JwtController {

    /**
     * Creates a JSON Web Token representing a Google Pay loyalty pass
     *
     * @param jwtRequestModel The Jwt object containing the request body
     * @return A Json string with the field "jwt" which contains the
     *  JWT representing the Google Pay pass
     */
    @PostMapping("/create")
    public String createJwt(@Valid @RequestBody JwtRequestModel jwtRequestModel) {

        String name = jwtRequestModel.getPlayerName();
        String id = jwtRequestModel.getPlayerId();
        String charityReceiver = jwtRequestModel.getCharityReceiver();
        String balance = jwtRequestModel.getBalance();
        String cardNumber = jwtRequestModel.getCardNumber();

        return new JSONObject().put("jwt", new Jwt().makeJwt(name, id, cardNumber, balance, charityReceiver)).toString();
    }

}
