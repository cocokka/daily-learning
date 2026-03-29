package com.demo.dl.cache.controller;

import com.demo.dl.cache.model.User;
import com.demo.dl.cache.service.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  public Map<String, Object> getUserById(@PathVariable Long id) {
    long startTime = System.currentTimeMillis();
    User user = userService.getUserById(id);
    long costTime = System.currentTimeMillis() - startTime;

    Map<String, Object> result = new HashMap<>();
    result.put("data", user);
    result.put("costTime", costTime + "ms");
    return result;
  }

  @GetMapping("/username/{username}")
  public Map<String, Object> getUserByUsername(@PathVariable String username) {
    long startTime = System.currentTimeMillis();
    User user = userService.getUserByUsername(username);
    long costTime = System.currentTimeMillis() - startTime;

    Map<String, Object> result = new HashMap<>();
    result.put("data", user);
    result.put("costTime", costTime + "ms");
    return result;
  }

  @PutMapping
  public User updateUser(@RequestBody User user) {
    return userService.updateUser(user);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    Map<String, String> result = new HashMap<>();
    result.put("message", "删除成功");
    return result;
  }

  @GetMapping("/stats")
  public Map<String, String> getCacheStats() {
    Map<String, String> result = new HashMap<>();
    result.put("cacheStats", userService.getCacheStats());
    return result;
  }
}
