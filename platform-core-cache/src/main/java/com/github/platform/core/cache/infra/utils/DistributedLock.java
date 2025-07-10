package com.github.platform.core.cache.infra.utils;

import com.github.platform.core.cache.infra.service.ICacheService;

/**
 * 分布式锁
 * 使用示例：
```html
 try (DistributedLock lock = new DistributedLock(cacheService,lockKey, 30)) {
     if (!lock.isAcquired()) {
         log.warn("重复执行: lockKey={}, ",lockKey);
         return;
     }
     // 执行任务
 }
 ```
 * @Author: yxkong
 * @Date: 2025/6/17
 * @version: 1.0
 */
public class DistributedLock implements AutoCloseable{
    private final String key;
    private final String lockId;
    private final ICacheService cacheService;
    private boolean acquired;

    public DistributedLock(ICacheService cacheService, String key, int expireSeconds) {
        this.cacheService = cacheService;
        this.key = key;
        this.lockId = cacheService.acquireLock(key, expireSeconds);
        this.acquired = lockId != null;
    }

    public boolean isAcquired() {
        return acquired;
    }

    @Override
    public void close() {
        if (acquired) {
            cacheService.releaseLock(key, lockId);
        }
    }
}
