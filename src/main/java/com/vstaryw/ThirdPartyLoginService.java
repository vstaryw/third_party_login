package com.vstaryw;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.vstaryw.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;

/**
 * 验证第三方登录token的合法性
 */
public class ThirdPartyLoginService {
    Logger log = LoggerFactory.getLogger(ThirdPartyLoginService.class);

    private LRUCache<String,String> lruCache = new LRUCache<>(1000);

    /**
     * 总入口，进行安全验证
     * @param thirdPartyUser
     * @return
     */
    public boolean checkThirdToken(ThirdPartyUser thirdPartyUser){
        Integer clientType = thirdPartyUser.getClientType();
        boolean isPass = false;
        if(clientType != null){
            int type = clientType.intValue();
            String openId = thirdPartyUser.getOpenId();
            String accessToken = thirdPartyUser.getAccessToken();
            String cacheKey = openId + "-" +type;
            String cacheVal = lruCache.get(cacheKey);
            if(!Strings.isNullOrEmpty(cacheVal)){
                log.info("client type is :{},openid is :{} exist,cache size is :{}",type,openId,lruCache.size());
               return true;
            }
            try {
                if (EnumLoginType.QQ.getLoginTypeCode() == type){
                    isPass = checkQQToken(thirdPartyUser);
                } else if (EnumLoginType.WECHAT.getLoginTypeCode() == type){
                    isPass = checkWechatToken(accessToken, openId);
                } else if (EnumLoginType.SINA_WEIBO.getLoginTypeCode() == type){
                    isPass = checkSinaToken(accessToken);
                } else if (EnumLoginType.FACEBOOK.getLoginTypeCode() == type){
                    isPass = checkFacebookToken(accessToken);
                } else if (EnumLoginType.TWITTER.getLoginTypeCode() == type){
                    isPass = checkTwitterToken(accessToken,thirdPartyUser.getAccessTokenSecret());
                } else if (EnumLoginType.GOOGLE.getLoginTypeCode() == type){
                    isPass = checkGoogleToken(accessToken);
                }
            }catch (Exception e){
                log.error("ThirdPartyLoginService checkThirdToken is error :"+e.getMessage(),e);
            }
            if(isPass){
                lruCache.put(cacheKey,"1");
            }
        }
        return isPass;
    }

    /**
     * 检测qq的登录
     * @param thirdPartyUser
     * @return
     * @throws Exception
     */
    private boolean checkQQToken(ThirdPartyUser thirdPartyUser) throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append("appid=").append(CommonConsts.QQ_APP_ID).append("&")
                    .append("format=json&")
                    .append("openid=").append(thirdPartyUser.getOpenId()).append("&")
                    .append("openkey=").append(thirdPartyUser.getAccessToken()).append("&")
                    .append("pf=").append(thirdPartyUser.getPf()).append("&")
                    .append("userip=").append(thirdPartyUser.getUserIp());
        String combineParam = sb.toString();
        String encodeParam = URLEncoder.encode(combineParam,"utf-8");
        encodeParam = "GET&%2Fv3%2Fuser%2Fget_info&"+encodeParam;
        String key = CommonConsts.QQ_APP_KEY+"&";
        byte[] data = CryptalUtil.HmacSHA1Encrypt(encodeParam, key);
        String sig = Base64.getEncoder().encodeToString(data);
        String requestUrl = CommonConsts.QQ_URL + "?"+combineParam+"&sig="+URLEncoder.encode(sig,"utf-8");
        String result = HttpClientUtil.doGet(requestUrl);
        log.info("qq token validate result : {}",result);
        if(!Strings.isNullOrEmpty(result)){
            JSONObject jsonObject = JSON.parseObject(result);
            if(jsonObject != null && jsonObject.getInteger("ret") == 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 检测微信的登录
     * @param accessToken
     * @param openId
     * @return
     */
    private boolean checkWechatToken(String accessToken,String openId){
        String url = String.format(CommonConsts.WECHAT_URL,accessToken,openId);
        String result = HttpClientUtil.doGet(url);
        if(!Strings.isNullOrEmpty(result)){
            JSONObject jsonObject = JSON.parseObject(result);
            if(jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") == 0){
                return true;
            }
        }
        return false;
    }

    /**
     * 检测新浪的登录
     * @param accessToken
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean checkSinaToken(String accessToken) throws UnsupportedEncodingException {
        Map<String,Object>  paramMap = Maps.newHashMap();
        paramMap.put("access_token",accessToken);
        String result = HttpClientUtil.doPost(CommonConsts.SINA_URL, paramMap);
        if(!Strings.isNullOrEmpty(result)){
            JSONObject jsonObject = JSON.parseObject(result);
            String uid = jsonObject.getString("uid");
            if(!Strings.isNullOrEmpty(uid)){
                return true;
            }
        }
        return false;
    }

    /**
     * 检测facebook的登录
     * @param accessToken
     * @return
     */
    private boolean checkFacebookToken(String accessToken){
        String url = CommonConsts.FB_URL + "?input_token=" + accessToken + "&access_token=" + CommonConsts.FB_APP_ID
                + "%7C" + CommonConsts.FB_APP_KEY;
        String result = HttpClientUtil.doGet(url);
        if(!Strings.isNullOrEmpty(result)){
            JSONObject jsonObject = JSON.parseObject(result);
            String dataKey = "data";
            if(jsonObject.containsKey(dataKey)){
                JSONObject dataObj = jsonObject.getJSONObject(dataKey);
                String app_id = dataObj.getString("app_id");
                if(app_id.equals(CommonConsts.FB_APP_ID)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测twitter的登录
     * @param accessToken
     * @return
     */
    private boolean checkTwitterToken(String accessToken,String accessTokenSecret){
        String oauth_nonce = RandomUtil.getRandomString(42);
        long oauth_timestamp = System.currentTimeMillis() / 1000;
        String signingKey = CommonConsts.TW_OAUTH_CONSUMER_SECRET + "&" + accessTokenSecret;
        StringBuilder signatrueBaseStr = new StringBuilder();
        String oauth_signature = null;
        StringBuilder paramStr = new StringBuilder();
        paramStr.append("oauth_consumer_key=").append(CommonConsts.TW_OAUTH_CONSUMER_KEY)
                .append("&oauth_nonce=").append(oauth_nonce)
                .append("&oauth_signature_method=HMAC-SHA1")
                .append("&oauth_timestamp=").append(oauth_timestamp)
                .append("&oauth_token=").append(accessToken)
                .append("&oauth_version=1.0");
        try {
            signatrueBaseStr.append("GET&").append(URLEncoder.encode(CommonConsts.TW_URL,"UTF-8"))
                    .append("&").append(URLEncoder.encode(paramStr.toString(),"UTF-8"));
            oauth_signature = CryptalUtil.hash_hmac(signatrueBaseStr.toString(), signingKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Map<String,Object> headMap = Maps.newHashMap();
            StringBuilder headParam = new StringBuilder();
            headParam.append("OAuth oauth_consumer_key=\"").append(CommonConsts.TW_OAUTH_CONSUMER_KEY).append("\", ")
                    .append("oauth_nonce=\"").append(oauth_nonce).append("\", ")
                    .append("oauth_signature=\"").append(URLEncoder.encode(oauth_signature,"UTF-8")).append("\", ")
                    .append("oauth_signature_method=\"HMAC-SHA1\", ")
                    .append("oauth_timestamp=\"").append(oauth_timestamp).append("\", ")
                    .append("oauth_token=\"").append(accessToken).append("\", ")
                    .append("oauth_version=\"1.0\"");
            headMap.put("Authorization",headParam.toString());
            Map<String,Object> paramMap = Maps.newHashMap();
            String result = HttpClientUtil.doGet(CommonConsts.TW_URL, headMap,paramMap);
            if(!Strings.isNullOrEmpty(result)){
                JSONObject jsonObject = JSON.parseObject(result);
                if(jsonObject.containsKey("id")){
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("checkTwitterToken is error : ",e);
        }
        return false;
    }

    /**
     * 检测google的登录
     * @param accessToken
     * @return
     */
    private boolean checkGoogleToken(String accessToken){
        String url = String.format(CommonConsts.GOOGLE_OAUTH_URL,accessToken);
        String result = HttpClientUtil.doGet(url);
        log.info("google token check result is : {} ",result);
        if(!Strings.isNullOrEmpty(result)){
            JSONObject jsonObject = JSON.parseObject(result);
            String dataKey = "aud";
            if(jsonObject.containsKey(dataKey)){
                String aud = jsonObject.getString(dataKey);
                if(CommonConsts.GOOGLE_APP_ID.equals(aud)){
                    return true;
                }
            }
        }
        return false;
    }
}
