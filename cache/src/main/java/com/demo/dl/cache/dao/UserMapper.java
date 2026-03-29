package com.demo.dl.cache.dao;

import com.demo.dl.cache.model.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

  @Select("SELECT * FROM user WHERE id = #{id}")
  @Results(
      id = "userMap",
      value = {
        @Result(column = "id", property = "id"),
        @Result(column = "username", property = "username"),
        @Result(column = "email", property = "email"),
        @Result(column = "age", property = "age"),
        @Result(column = "create_time", property = "createTime"),
        @Result(column = "update_time", property = "updateTime")
      })
  User selectById(Long id);

  @Select("SELECT * FROM user WHERE username = #{username}")
  @ResultMap("userMap")
  User selectByUsername(String username);

  @Insert("INSERT INTO user(username, email, age) VALUES(#{username}, #{email}, #{age})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(User user);

  @Update(
      "UPDATE user SET username=#{username}, email=#{email}, age=#{age}, "
          + "update_time=NOW() WHERE id=#{id}")
  int update(User user);

  @Delete("DELETE FROM user WHERE id=#{id}")
  int deleteById(Long id);
}
