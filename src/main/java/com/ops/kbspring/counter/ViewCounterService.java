package com.ops.kbspring.counter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
  public class ViewCounterService {
  
      private final StringRedisTemplate redis;
      public ViewCounterService(StringRedisTemplate redis) { this.redis = redis; }
      
      // returns the new count after incrementing — atomic, no race
      public Long recordView(String pageId) {
          return redis.opsForValue().increment("views:" + pageId);
      }   
      
      public Long currentViews(String pageId) {
          String v = redis.opsForValue().get("views:" + pageId);
          return v == null ? 0L : Long.parseLong(v);
      }
  }
