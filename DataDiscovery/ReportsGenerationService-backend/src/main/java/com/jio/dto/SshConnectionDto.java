package com.jio.dto;

import java.util.List;

import lombok.Data;

@Data
public class SshConnectionDto {
    private List<String> host;
    private int port;
    private String username;
    private String password;
}
