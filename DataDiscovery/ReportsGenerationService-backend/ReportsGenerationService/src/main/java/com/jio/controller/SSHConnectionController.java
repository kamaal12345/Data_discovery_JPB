package com.jio.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jio.dto.SshConnectionDto;
import com.jio.entity.SSHConnectionChecker;
import com.jio.entity.SSHConnectionChecker.ConnectionResult;

import java.util.List;

@RestController
@RequestMapping("/api/ssh")
public class SSHConnectionController {

    @Autowired
    private SSHConnectionChecker checker;

    @PostMapping("/test-connections")
    public List<ConnectionResult> testSSHConnections(@RequestBody SshConnectionDto request) {
        return checker.checkConnections(
            request.getHost(),
            request.getPort(),
            request.getUsername(),
            request.getPassword()
        );
    }
}
