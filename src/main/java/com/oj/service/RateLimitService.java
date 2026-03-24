package com.oj.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.oj.config.OjProperties;

@Service
public class RateLimitService {
    private final OjProperties properties;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitService(OjProperties properties) {
        this.properties = properties;
    }

    public void checkLogin(String key) {
        int limit = properties.getLimits().getLoginPerMinute();
        if (!allow("login:" + key, limit)) {
            throw new RateLimitExceededException("Too many login attempts");
        }
    }

    public void checkSubmit(String key) {
        int limit = properties.getLimits().getSubmitPerMinute();
        if (!allow("submit:" + key, limit)) {
            throw new RateLimitExceededException("Too many submissions");
        }
    }

    private boolean allow(String key, int limitPerMinute) {//限制某个操作key每分钟能执行的次数限制
        if (limitPerMinute <= 0) {
            return true;
        }
        long window = System.currentTimeMillis() / 60000L;//一分组为60000毫秒 在钟表上的单独一分钟内 window值不变
        Counter counter = counters.computeIfAbsent(key, k -> new Counter(window, 0));
        synchronized (counter) {//加锁 以免线程冲突
            if (counter.window != window) {//若对不上 则说明这一分钟过去了 到了下一分钟
                counter.window = window;
                counter.count = 0;
            }
            if (counter.count >= limitPerMinute) {
                return false;
            }
            counter.count++;
            return true;
        }
    }

    private static class Counter {
        private long window;
        private int count;

        private Counter(long window, int count) {
            this.window = window;
            this.count = count;
        }
    }
}
