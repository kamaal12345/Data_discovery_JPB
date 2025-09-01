package com.jio.entity;
import com.jcraft.jsch.*;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SSHConnectionChecker {

    public static class ConnectionResult {
        public String host;
        public boolean success;
        public String message;

        public ConnectionResult(String host, boolean success, String message) {
            this.host = host;
            this.success = success;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("Host: %-15s | Success: %-5s | Message: %s", host, success, message);
        }
    }

    public List<ConnectionResult> checkConnections(List<String> hosts, int port, String username, String password) {
        List<ConnectionResult> results = new ArrayList<>();
        JSch jsch = new JSch();

        for (String host : hosts) {
            Session session = null;
            try {
                session = jsch.getSession(username, host, port);
                session.setPassword(password);

                // Avoid asking for key confirmation
                session.setConfig("StrictHostKeyChecking", "no");

                session.connect(5000);  // 5 second timeout
                results.add(new ConnectionResult(host, true, "Connection successful"));

            } catch (JSchException e) {
                results.add(new ConnectionResult(host, false, "Connection failed: " + e.getMessage()));
            } finally {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }

        return results;
    }
}
