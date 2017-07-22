package com.vstaryw.common;

/**
 * 第三方登录类型
 */
public enum EnumLoginType {
    QQ(1,"QQ登录"),
    WECHAT(2,"微信登录"),
    SINA_WEIBO(3,"新浪微博登录");
    private EnumLoginType(){

    }

    private EnumLoginType(int loginTypeCode, String loginTypeName){
        this.loginTypeCode = loginTypeCode;
        this.loginTypeName = loginTypeName;
    }

    private int loginTypeCode;

    private String loginTypeName;

    public int getLoginTypeCode() {
        return loginTypeCode;
    }

    public void setLoginTypeCode(int loginTypeCode) {
        this.loginTypeCode = loginTypeCode;
    }

    public String getLoginTypeName() {
        return loginTypeName;
    }

    public void setLoginTypeName(String loginTypeName) {
        this.loginTypeName = loginTypeName;
    }
}
