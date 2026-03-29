package com.demo.dl.cache.service;

import com.demo.dl.cache.model.User;

public interface UserService {

  User getUserById(Long id);

  User getUserByUsername(String username);

  User updateUser(User user);

  void deleteUser(Long id);

  String getCacheStats();
}
