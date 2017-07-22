package com.vstaryw.common;

import java.io.Serializable;


/**
 * 第三方登录公共参数
 */
public class ThirdPartyUser implements Serializable {

    private static final long serialVersionUID = 7839913004896201156L;

    private String openId;

    private String accessToken;

    private String pf;

    private String userIp;

    private Integer clientType;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }
}
