package com.jio.utils;

import org.springframework.stereotype.Service;
import io.cloudsoft.winrm4j.winrm.WinRmTool;

@Service
public class WinRMConnector {

    public WinRmTool connect(String host, String username, String password) {
        return WinRmTool.Builder.builder(host, username, password)
                .useHttps(false)
                .port(5985)
                .disableCertificateChecks(true)
                .build();
    }

 
    public void disconnect(WinRmTool tool) {
        tool = null;
    }
}
