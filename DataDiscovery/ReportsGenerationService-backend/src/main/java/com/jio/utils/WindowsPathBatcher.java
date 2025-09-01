package com.jio.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import io.cloudsoft.winrm4j.winrm.WinRmTool;
import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

/**
 * WindowsPathBatcher fetches files recursively from a base directory on a
 * remote Windows machine, filtered by max size, excludes, allowed extensions.
 * Returns batches of file paths for incremental processing.
 */
public class WindowsPathBatcher implements Iterable<List<String>> {

	private final WinRmTool winRmTool;
	private final String powershellCmd;
	private final int batchSize;
	private final AtomicBoolean shouldStopSignal;

	public WindowsPathBatcher(WinRmTool winRmTool, String basePath, long maxBytes, Set<String> excludes,
			Set<String> allowedExts, int batchSize, AtomicBoolean shouldStopSignal) {
		this.winRmTool = winRmTool;
		this.batchSize = batchSize;
		this.shouldStopSignal = shouldStopSignal;

		String excludeFilter = excludes.stream().map(ex -> String.format("-not ($_.FullName -like '*\\%s\\*')", ex))
				.collect(Collectors.joining(" -and "));

		String extFilter = allowedExts.stream().map(ext -> String.format("$_.Extension -eq '.%s'", ext))
				.collect(Collectors.joining(" -or "));

		this.powershellCmd = String.format("Get-ChildItem -Path '%s' -Recurse -File | "
				+ "Where-Object { $_.Length -lt %d } | " + "Where-Object { %s } | " + "Where-Object { %s } | "
				+ "Select-Object -ExpandProperty FullName", basePath, maxBytes, excludeFilter, extFilter);

		System.out.println("Initialized WindowsPathBatcher PowerShell Command: " + powershellCmd);
	}

	@Override
	public Iterator<List<String>> iterator() {
		return new Iterator<>() {
			private final BufferedReader reader = new BufferedReader(
					new InputStreamReader(runWinRm(powershellCmd), StandardCharsets.UTF_8));
			private final List<String> cache = new ArrayList<>();
			private boolean done = false;
			private boolean first = false;
			private int batchCount = 0;

			@Override
			public boolean hasNext() {
				if (done && cache.isEmpty())
					return false;
				if (!cache.isEmpty())
					return true;
				fillBatch();
				if (!first && cache.isEmpty()) {
					done = true;
					System.out.println("ℹ️ No matching files found during remote Windows path scan.");
					return false;
				}
				first = true;
				return !cache.isEmpty();
			}

			@Override
			public List<String> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				batchCount++;
				System.out.println("Processing batch #" + batchCount + ", size: " + cache.size());
				List<String> batch = new ArrayList<>(cache);
				cache.clear();
				return batch;
			}

			private void fillBatch() {
				try {
					String line;
					while (cache.size() < batchSize && (line = reader.readLine()) != null) {
						if (shouldStopSignal.get()) {
							System.out.println("🛑 Stop signal received — stopping Windows batch fill.");
							done = true;
							break;
						}
						if (!line.trim().isEmpty()) {
							cache.add(line.trim());
						}
					}
				} catch (Exception e) {
					System.err.println("❌ Error reading WinRM stream: " + e.getMessage());
					done = true;
				}
				if (cache.isEmpty() && !shouldStopSignal.get()) {
					done = true;
					System.out.println("✅ No more files to process — stopping Windows iteration.");
				}
			}

			private InputStream runWinRm(String cmd) {
				try {
					WinRmToolResponse resp = winRmTool.executePs(cmd);
					if (resp.getStatusCode() == 0) {
						return new ByteArrayInputStream(resp.getStdOut().getBytes(StandardCharsets.UTF_8));
					} else {
						throw new RuntimeException("WinRM command failed: " + resp.getStdErr());
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to execute WinRM command", e);
				}
			}
		};
	}

	public List<String> getAllFilePaths() {
		System.out.println("Start scanning...");
		List<String> paths = new ArrayList<>();
		int total = 0;
		for (List<String> batch : this) {
			paths.addAll(batch);
			total += batch.size();
		}
		System.out.println("Completed scan. Total found: " + total);
		return paths;
	}
}
