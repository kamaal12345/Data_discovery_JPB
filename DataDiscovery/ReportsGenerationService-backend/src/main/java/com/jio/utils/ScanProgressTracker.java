package com.jio.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ScanProgressTracker {
    private static final ConcurrentMap<Long, ScanProgress> progressMap = new ConcurrentHashMap<>();

    public static void initProgress(Long scanId, long totalFiles) {
        progressMap.put(scanId, new ScanProgress(totalFiles));
    }

    public static void updateProgress(Long scanId, long scannedFiles) {
        ScanProgress sp = progressMap.get(scanId);
        if (sp != null) {
            sp.setScannedFiles(scannedFiles);
        }
    }

    public static ScanProgress getProgress(Long scanId) {
        return progressMap.getOrDefault(scanId, new ScanProgress(0));
    }

    public static void removeProgress(Long scanId) {
        progressMap.remove(scanId);
    }

    public static class ScanProgress {
        private final long totalFiles;
        private volatile long scannedFiles;

        public ScanProgress(long totalFiles) {
            this.totalFiles = totalFiles;
            this.scannedFiles = 0;
        }

        public long getTotalFiles() {
            return totalFiles;
        }

        public long getScannedFiles() {
            return scannedFiles;
        }

        public void setScannedFiles(long scannedFiles) {
            this.scannedFiles = scannedFiles;
        }

        public int getPercentage() {
            if (totalFiles == 0)
                return 0;
            return (int)((scannedFiles * 100) / totalFiles);
        }
    }
}
