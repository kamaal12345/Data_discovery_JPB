package com.jio.utils;

import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class SSHConnector {

	public Session connect(String host, int port, String username, String password) throws JSchException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		JSch jsch = new JSch();
		Session session = jsch.getSession(username, host.trim(), port);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

		session.setServerAliveInterval(10_000); // 10 seconds
		session.setServerAliveCountMax(3);

		session.connect(10_000);
		return session;
	}

	public Session connectWithRetry(String host, int port, String username, String password, int maxRetries)
			throws JSchException, InterruptedException {
		int attempts = 0;
		while (attempts < maxRetries) {
			try {
				return connect(host, port, username, password);
			} catch (JSchException e) {
				attempts++;
				if (attempts >= maxRetries) {
					throw e;
				}
				Thread.sleep(2000); // wait 2s before retry
			}
		}
		throw new JSchException("Unable to connect after retries");
	}

	public void disconnect(Session session) {
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
	}
}
