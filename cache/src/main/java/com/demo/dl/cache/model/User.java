package com.demo.dl.cache.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String username;

  private String email;

  private Integer age;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;
}
