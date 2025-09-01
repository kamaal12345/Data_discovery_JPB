package com.jio.controller;

import com.jio.dto.SshConnectionDto;
import com.jio.entity.WinRMConnectionChecker;
import com.jio.entity.WinRMConnectionChecker.ConnectionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/winrm")
public class WinRMConnectionController {

    @Autowired
    private WinRMConnectionChecker checker;

    @PostMapping("/test-connections")
    public List<ConnectionResult> testWinRMConnections(@RequestBody SshConnectionDto request) {
        return checker.checkConnections(
            request.getHost(),   // List<String>
            request.getUsername(),
            request.getPassword()
        );
    }
}
