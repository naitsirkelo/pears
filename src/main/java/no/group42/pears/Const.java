package no.group42.pears;

/**
 * Reoccurring String constants used in NTController and PassesController
 */
public class Const {

    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int PHONE_NO_LENGTH = 8;

    // LOTTO_ID = 1 by default
    public static final int VIKINGLOTTO_ID = 18;
    public static final int EUROJACKPOT_ID = 16;

    public static final int LOTTO_ROW = 5;
    public static final int VIKINGLOTTO_ROW = 6;
    public static final int EUROJACKPOT_ROW = 25;

    //(7 * 1-34)
    public static final int LOTTO_LENGTH = 7;
    public static final int LOTTO_MAX = 34;
    public static final int LOTTO_EXTRA = 0;
    public static final int LOTTO_EXTRA_MAX = 0;

    //(6 * 1-48) + (1 * 1-8)
    public static final int VIKINGLOTTO_LENGTH = 6;
    public static final int VIKINGLOTTO_MAX = 48;
    public static final int VIKINGLOTTO_EXTRA = 1;
    public static final int VIKINGLOTTO_EXTRA_MAX = 8;

    //(5 * 1-50) + (2 * 1-10)
    public static final int EUROJACKPOT_LENGTH = 5;
    public static final int EUROJACKPOT_MAX = 50;
    public static final int EUROJACKPOT_EXTRA = 2;
    public static final int EUROJACKPOT_EXTRA_MAX = 10;

    public static final String PAYMENT_METHOD = "PLAYERACCOUNT";
    public static final String PLAY_METHOD = "NORMAL_PLAY";
    public static final String CHARITY_RECEIVER = "charityReceiver";
    public static final String BALANCE = "balance";


    public static String CUSTOMER_BALANCE = "customer/v1/balance";
    public static String NT_PROD_URL = "https://www.norsk-tipping.no:443/mobile-channel/api/";

    /* Error messages */
    public static final String BALANCE_UPDATE_FAIL = "Could not update balance.";
    public static final String REMOVE_FROM_DB_FAIL = "Could not remove item from database.";

    /* Success messages */
    public static final String BALANCE_UPDATE_SUCCESS = "Balance updated.";
    public static final String REMOVE_FROM_DB_SUCCESS = "Item removed from database successfully";



}
