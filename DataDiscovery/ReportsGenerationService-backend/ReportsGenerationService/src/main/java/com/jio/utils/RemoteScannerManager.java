package com.jio.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RemoteScannerManager {

    private final Session session;
    private final Set<String> excludes;
    private final Set<String> allowedExts;
    private final long maxFileSizeBytes;
    private final int batchSize;
    private final ExecutorService executor;

    public RemoteScannerManager(Session session,
                                Set<String> excludes,
                                Set<String> allowedExts,
                                long maxFileSizeBytes,
                                int batchSize,
                                int threadCount) {
        this.session = session;
        this.excludes = excludes;
        this.allowedExts = allowedExts;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.batchSize = batchSize;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public List<String> scanBasePath(String basePath) throws Exception {
        List<String> topFolders = listTopLevelFolders(basePath);
        System.out.println("[RemoteScannerManager] Top-level folders: " + topFolders);

        List<Future<List<String>>> futures = new ArrayList<>();

        for (String folder : topFolders) {
            futures.add(executor.submit(() -> scanFolder(folder)));
        }
        futures.add(executor.submit(() -> scanFolder(basePath)));

        List<String> allFiles = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            try {
                allFiles.addAll(future.get());
            } catch (Exception e) {
                System.err.println("[RemoteScannerManager] Error scanning: " + e.getMessage());
            }
        }

        executor.shutdown();
        System.out.println("[RemoteScannerManager] ✅ Scan complete. Total files: " + allFiles.size());
        return allFiles;
    }

    private List<String> listTopLevelFolders(String basePath) throws Exception {
        String excludeClause = excludes.stream()
                .map(e -> "-path '" + basePath + "/" + e + "'")
                .collect(Collectors.joining(" -o "));

        String findCmd = String.format("sudo find %s/* %s -prune -o -type d -print",
                basePath,
                excludeClause.isEmpty() ? "" : "\\( " + excludeClause + " \\)");

        System.out.println("[RemoteScannerManager] Listing folders with: " + findCmd);

        List<String> folders = new ArrayList<>();
        ChannelExec ch = null;

        try {
            ch = (ChannelExec) session.openChannel("exec");
            ch.setCommand(findCmd);
            ch.setInputStream(null);
            InputStream in = ch.getInputStream();
            ch.connect();

            try (Scanner scanner = new Scanner(in)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;
                    boolean isExcluded = excludes.stream()
                            .anyMatch(ex -> line.toLowerCase().contains(ex.toLowerCase()));
                    if (!isExcluded) {
                        folders.add(line);
                    }
                }
            }
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
        }

        return folders;
    }

    private List<String> scanFolder(String folderPath) {
        System.out.println("[RemoteScannerManager] 🔍 Scanning: " + folderPath);
        List<String> files = new ArrayList<>();
        ChannelExec ch = null;

        try {
            String excludeClause = excludes.stream()
                    .map(e -> "-path '*/" + e + "'")
                    .collect(Collectors.joining(" -o "));

            String extClause = allowedExts.stream()
                    .map(ext -> "-iname '*." + ext + "'")
                    .collect(Collectors.joining(" -o "));

            String findCmd = String.format(
                    "sudo find %s %s -prune -o -type f -size -%dc \\( %s \\) -print",
                    folderPath,
                    excludeClause.isEmpty() ? "" : "\\( " + excludeClause + " \\)",
                    maxFileSizeBytes / 1024,
                    extClause
            );

            System.out.println("[RemoteScannerManager] Find command: " + findCmd);

            ch = (ChannelExec) session.openChannel("exec");
            ch.setCommand(findCmd);
            ch.setInputStream(null);
            InputStream in = ch.getInputStream();
            ch.connect();

            try (Scanner scanner = new Scanner(in)) {
                List<String> batch = new ArrayList<>(batchSize);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;

                    batch.add(line);
                    if (batch.size() >= batchSize) {
                        files.addAll(batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    files.addAll(batch);
                }
            }
        } catch (Exception e) {
            System.err.println("[RemoteScannerManager] Error scanning " + folderPath + ": " + e.getMessage());
        } finally {
            if (ch != null && ch.isConnected()) {
                ch.disconnect();
            }
        }

        System.out.println("[RemoteScannerManager] Found in " + folderPath + ": " + files.size());
        return files;
    }
}
