package com.jaketv.jaketvapp.util;

public class Constant {

    //public static String URL = "http://jaketv.tv/jaketv/jaketv_webservice/service.php";
    public static String URL = "jake:jake@staging.jaketv.tv/jaketv/jaketv_webservice/service.php";

    public static String network_error = "please check your network connectivity.";
    public static String AppName = "JakeTV";


    public static class SHRED_PR {
        public static final String SHARE_PREF = AppName + "_preferences";
        public static final String KEY_IS_LOGGEDIN = "is_loggedin";
        public static final String KEY_USERID = "userid";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_FNAME = "fname";
        public static final String KEY_LNAME = "lname";
        public static final String KEY_EMAIL = "email";

        public static final String KEY_RELOAD = "reload";
        public static final String KEY_ENABLE_MAILCHIMP = "enableMailChimp";
        public static final String KEY_REGISTEREDDATE = "registeredDate";
    }
}
