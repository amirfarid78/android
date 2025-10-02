package com.coheser.app;

public class Constants {

    public static final String BASE_URL = "https://www.snaplive.online/";
    public static final String API_KEY = "156c4675-9608-4591-b2ec-xxxxxxx";


    public static final String API_URL = "https://apis.argear.io/";
    public static final String API_KEY_ARGEAR = "89b5cfc4a22ce323e26aef7e";
    public static final String SECRET_KEY = "4d2f037f6afdbe5fb731dd2573d4953471ae3fcb3fd23c5df01a72e0d2b5fcb6";
    public static final String AUTH_KEY = "U2FsdGVkX19lHRBma8VsK4Rbn4IHdwvburl5jfqdynRgwRvXRPjlnfGPixSAEn2PcpE+gUGzlAd5AbDibwuspA==";

   
    public static final String privacy_policy="";
    public static final String terms_conditions="";


    // if you want a user can't share a video from your com then you have to set this value to true
    public static final boolean IS_SECURE_INFO = false;


    public final static String AD_COLONY_BANNER_ID = "vzdb67721f381948c292";
    public final static String AD_COLONY_INTERSTITIAL_ID = "vza31e4740f6e94f5d9b";


    public static final boolean IS_DEMO_APP = false;
    // if you show the ad on after every specific video count
    public static final int SHOW_AD_ON_EVERY=100;


    // if you want a video thumnail image show rather then a video gif then set the below value to false.
    public static final boolean IS_SHOW_GIF = false;

    // if you want to disbale all the toasts in the com
    public static final boolean IS_TOAST_ENABLE = true;
    //video description char limit during posting the video
    public final static int VIDEO_DESCRIPTION_CHAR_LIMIT = 250;
    // Username char limit during signup and edit the account
    public static final int USERNAME_CHAR_LIMIT = 30;
    // user profile bio char limit during edit the profile
    public static final int BIO_CHAR_LIMIT = 150;
    public static final String productShowingCurrency = "$";

    public static final String productSellingCurrency ="\uD83D\uDFE1";
    public static final boolean IsProductPriceInCoin=true;

    public static final String CURRENCY = "$";
    // Make product ids of different prices on google play console and place that ids in it.
    public static final String COINS0 = "100";
    public static final String PRICE0 = CURRENCY + "1";
    public static final String Product_ID0 = "android.test.purchased";
    public static final String COINS1 = "500";
    public static final String PRICE1 = CURRENCY + "5";
    public static final String Product_ID1 = "android.test.purchased";
    public static final String COINS2 = "2000";
    public static final String PRICE2 = CURRENCY + "20";
    public static final String Product_ID2 = "android.test.purchased";
    public static final String COINS3 = "5000";
    public static final String PRICE3 = CURRENCY + "50";
    public static final String Product_ID3 = "android.test.purchased";
    public static final String COINS4 = "10000";
    public static final String PRICE4 = CURRENCY + "100";
    public static final String Product_ID4 = "android.test.purchased";
    public static final int ALL_IMAGE_DEFAULT_SIZE = 500;

    public static int PkBattleTime = 300000;
    // maximum time to record the video for now it is 30 sec
    public static int MAX_RECORDING_DURATION = 900000;
    public static int RECORDING_DURATION = 30000;
    // minimum time of recode a video for now it is 5 sec
    public static int MIN_TIME_RECORDING = 0;
    // max photo allowed for photo video upload
    public static int MAX_PICS_ALLOWED_FOR_VIDEO = 5;
    //max time for photo videos
    public static int MAX_TIME_FOR_VIDEO_PICS = 10;
    // minimum trim chunk time span of a video for now it is 5 sec
    public static int MIN_TRIM_TIME = 5;
    // maximum trim chunk time span of a video for now it is 30 sec
    public static int MAX_TRIM_TIME = 30;

    public static float mapZoomLevel =17;

    // The tag name you want to print all the log
    public static String tag = "snaplive_";

    public static String alertUniCode= "⚠\uFE0F ";

}

