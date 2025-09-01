package com.jio.entity;

import com.jio.utils.WinRMConnector;

import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class WinRMConnectionChecker {

    public static class ConnectionResult {
        public String host;
        public boolean success;
        public String message;

        public ConnectionResult(String host, boolean success, String message) {
            this.host = host;
            this.success = success;
            this.message = message;
        }
    }

    private final WinRMConnector connector = new WinRMConnector();

    public List<ConnectionResult> checkConnections(List<String> hosts, String username, String password) {
        List<ConnectionResult> results = new ArrayList<>();

        for (String host : hosts) {
            try {
                WinRmTool client = connector.connect(host, username, password);

                // Use a universal command that works in both CMD and PowerShell
                WinRmToolResponse response = client.executeCommand("hostname");
//                WinRmToolResponse response = client.executeCommand("dir C:\\");


                if (response.getStatusCode() == 0) {
                    results.add(new ConnectionResult(host, true, response.getStdOut().trim()));
                } else {
                    results.add(new ConnectionResult(host, false,
                            "Error: " + response.getStdErr().trim()));
                }

            } catch (Exception e) {
                results.add(new ConnectionResult(host, false, "Connection failed: " + e.getMessage()));
            }
        }

        return results;
    }

}
