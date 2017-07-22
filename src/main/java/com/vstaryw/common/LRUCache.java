package com.vstaryw.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将最近成功的openid进行LRU缓存，防止请求第三方服务器太频繁
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private static final long serialVersionUID = 8413350916567990797L;

    private int cacheSize;

    public LRUCache(int cacheSize){
        super(16,0.75f,true);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cacheSize;
    }
}
