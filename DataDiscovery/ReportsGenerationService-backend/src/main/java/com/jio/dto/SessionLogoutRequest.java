package com.jio.dto;

import lombok.Data;

@Data
public class SessionLogoutRequest {
  private String username;
  private String previousToken;
}
