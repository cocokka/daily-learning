package com.demo.dl.cache.service.impl;

import com.demo.dl.cache.annotation.TwoLevelCache;
import com.demo.dl.cache.config.TwoLevelCacheManager;
import com.demo.dl.cache.dao.UserMapper;
import com.demo.dl.cache.model.User;
import com.demo.dl.cache.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

  private final UserMapper userMapper;
  private final TwoLevelCacheManager cacheManager;

  @Override
  @TwoLevelCache(cacheName = "user", key = "'user:id:' + #id", localExpire = 60, redisExpire = 300)
  public User getUserById(Long id) {
    log.info("查询数据库: getUserById({})", id);
    return userMapper.selectById(id);
  }

  @Override
  @TwoLevelCache(
      cacheName = "user",
      key = "'user:username:' + #username",
      localExpire = 60,
      redisExpire = 300)
  public User getUserByUsername(String username) {
    log.info("查询数据库: getUserByUsername({})", username);
    return userMapper.selectByUsername(username);
  }

  @Override
  @Transactional
  public User updateUser(User user) {
    log.info("更新用户: {}", user);
    int updated = userMapper.update(user);
    if (updated == 1) {
      User latestUser = userMapper.selectById(user.getId());
      // 清除缓存
      cacheManager.evict("user", "user:id:" + latestUser.getId());
      if (latestUser.getUsername() != null) {
        cacheManager.evict("user", "user:username:" + latestUser.getUsername());
      }

      return latestUser;
    }
    return user;
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    log.info("删除用户: {}", id);
    User user = userMapper.selectById(id);
    if (user != null) {
      // 先清除缓存
      cacheManager.evict("user", "user:id:" + id);
      if (user.getUsername() != null) {
        cacheManager.evict("user", "user:username:" + user.getUsername());
      }
      userMapper.deleteById(id);
    }
  }

  @Override
  public String getCacheStats() {
    return cacheManager.getLocalCacheStats("user");
  }
}
