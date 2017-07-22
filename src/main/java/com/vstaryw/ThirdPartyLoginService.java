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
                if (EnumLoginType.QQ.getLoginTypeCode() == type) {
                    isPass = checkQQToken(thirdPartyUser);
                } else if (EnumLoginType.WECHAT.getLoginTypeCode() == type) {
                    isPass = checkWechatToken(accessToken, openId);
                } else if (EnumLoginType.SINA_WEIBO.getLoginTypeCode() == type) {
                    isPass = checkSinaToken(accessToken);
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
}
