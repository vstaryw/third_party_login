package com.vstaryw.common;

/**
 * 定义第三方的验证地址
 */
public class CommonConsts {


    /**
     * qq登录
     */
    public final static String QQ_URL = "http://openapi.tencentyun.com/v3/user/get_info";

    public static final String QQ_APP_ID ="qq app id";

    public static final String QQ_APP_KEY = "qq app key";

    /**
     * 微信登录
     */
    public static final String WECHAT_URL = "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s";

    /**
     * 新浪登录
     */
    public static final String SINA_URL = "https://api.weibo.com/oauth2/get_token_info";

    /**
     * facebook登录
     */
    public static final String FB_APP_ID = "facebook app id";
    public static final String FB_APP_KEY = "facebook app key";
    public static final String FB_URL = "https://graph.facebook.com/debug_token";
    /**
     * twitter登录
     */
    public static final String TW_OAUTH_CONSUMER_KEY = "twitter oauth consumer key";
    public static final String TW_OAUTH_CONSUMER_SECRET = "twitter oauth consumer secret";
    public static final String TW_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    /**
     * google登录
     */
    public static final String GOOGLE_APP_ID = "the google app id";
    public static final String GOOGLE_OAUTH_URL = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=%s";

}
