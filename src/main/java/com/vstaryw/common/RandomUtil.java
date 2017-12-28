package com.vstaryw.common;

import java.util.Random;

/**
 * @author vstaryw
 * Created on 2017/7/7 0007.
 */
public class RandomUtil {

    private final static String STR="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static Random random = new Random();

    public static String getRandomString(int len){
        StringBuilder n = new StringBuilder();
        for (int  r = 0; len > r; ++r){
            n.append(STR.charAt(random.nextInt(STR.length())));
        }
        return n.toString();
    }

    public static String getRandNum(int len) {
        int num = random.nextInt((int) (Math.pow(10,len) -1));
        String numStr = String.format("%0"+len+"d", num);
        return numStr;
    }
}
