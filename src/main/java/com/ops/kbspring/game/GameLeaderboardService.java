package com.ops.kbspring.game;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
  import org.springframework.data.redis.core.ZSetOperations;
  import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
  
  @Service
  public class GameLeaderboardService {
      private final  StringRedisTemplate redis;
      private static final String KEY = "leaderboard:game1";
      public GameLeaderboardService(StringRedisTemplate redis) { this.redis = redis; }
      
      // set/replace a player's score  -> ZADD
      public void submitScore(String player, double score) {
          redis.opsForZSet().add(KEY, player, score);
      }   
      
      // add points to an existing score -> ZINCRBY (atomic)
      public Double addPoints(String player, double points) {
          return redis.opsForZSet().incrementScore(KEY, player, points);
      }   
      
      // top N, highest first -> ZREVRANGE WITHSCORES
      public Set<ZSetOperations.TypedTuple<String>> topPlayers(int n) {
          return redis.opsForZSet().reverseRangeWithScores(KEY, 0, n - 1);
      }   
      
      // a player's rank (0-based, highest score = rank 0) -> ZREVRANK
      public Long rankOf(String player) {
          return redis.opsForZSet().reverseRank(KEY, player);
      }

      public void hashOff(){
          redis.opsForHash().put("user:1", "name", "Alice");
          redis.opsForHash().put("user:1", "lastSeen", Instant.now().toString());
          redis.opsForHash().increment("user:1", "loginCount", 1);   // HINCRBY — atomic, one field
          Map<Object,Object> all = redis.opsForHash().entries("user:1"); // HGETALL

          redis.opsForSet().add("user:1:follows", "2", "3", "5");
          redis.opsForSet().add("user:9:follows", "3", "5", "8");

          Set<String> mutual = redis.opsForSet().intersect("user:1:follows", "user:9:follows");

          redis.opsForSet().add("post:42:tags", "redis", "java");   // tags
          Boolean isMember = redis.opsForSet().isMember("post:42:tags", "redis");

          redis.opsForValue().setBit("dau:2026-07-08", 1005, true);
          Long activeToday = redis.execute((RedisCallback<Long>) c ->
                  c.stringCommands().bitCount("dau:2026-07-08".getBytes()));

          redis.opsForHyperLogLog().add("unique:visitors", "ip1", "ip2", "ip1"); // PFADD (dedups)
          Long approxUniques = redis.opsForHyperLogLog().size("unique:visitors"); // PFCOUNT

          var geo = redis.opsForGeo();
          geo.add("stores", new Point(-0.1276, 51.5072), "london-store");   // GEOADD (lon, lat)
          geo.add("stores", new Point(-0.1420, 51.5010), "westminster-store");

          // stores within 3km of a point — GEOSEARCH
          var results = geo.search("stores",
                  new Circle(new Point(-0.1300, 51.5050), new Distance(3, Metrics.KILOMETERS)));
      }
  }   
