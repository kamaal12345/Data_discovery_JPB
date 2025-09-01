package com.jio.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

public class PathBatcher implements Iterable<List<String>> {
	private final Session session;
	private final String findCmd;
	private final int batchSize;
	private final AtomicBoolean shouldStopSignal;

	public PathBatcher(Session session, String basePath, long maxBytes, Set<String> excludes, int batchSize,
			Set<String> allowedExts,AtomicBoolean shouldStopSignal) {
		this.session = session;
		this.batchSize = batchSize;
		this.shouldStopSignal = new AtomicBoolean();

		Set<String> allExcl = new HashSet<>(excludes);
		String excludeClause = allExcl.stream().map(p -> "-path '" + p + "'").collect(Collectors.joining(" -o "));

		String extClause = allowedExts.stream().map(ext -> "-iname '*." + ext + "'")
				.collect(Collectors.joining(" -o "));

		this.findCmd = String.format("sudo find %s \\( %s \\) -prune -o -type f -size -%sM \\( %s \\) -print", basePath,
				excludeClause, maxBytes / 1024 / 1024, extClause);

		System.out.println("Initialized PathBatcher: " + findCmd);
	}

	@Override
	public Iterator<List<String>> iterator() {
		return new Iterator<>() {
			private final BufferedReader reader = new BufferedReader(new InputStreamReader(runChannel(findCmd)));
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
			        System.out.println("ℹ️ No matching files found during remote path scan.");
			        return false;
			    }
			    first = true;
			    return !cache.isEmpty();
			}

			@Override
			public List<String> next() {
				batchCount++;
				System.out.println("Processing batch #" + batchCount + ", size: " + cache.size());
				List<String> list = new ArrayList<>(cache);
				cache.clear();
				return list;
			}

//			private void fillBatch() {
//				try {
//					String line;
//					while (cache.size() < batchSize && (line = reader.readLine()) != null) {
//						cache.add(line);
//					}
//				} catch (Exception e) {
//					System.err.println("❌ Error in reading stream: " + e.getMessage());
//					done = true;
//				}
//				if (cache.isEmpty()) {
//					done = true;
//					System.out.println("Scan complete. No more files.");
//				}
//			}
			
			private void fillBatch() {
			    try {
			        String line;
			        while (cache.size() < batchSize && (line = reader.readLine()) != null) {
			            if (shouldStopSignal.get()) {
			                System.out.println("🛑 Stop signal received — stopping batch fill.");
			                done = true;
			                break;
			            }
			            cache.add(line);
			        }
			    } catch (Exception e) {
			        System.err.println("❌ Error in reading stream: " + e.getMessage());
			        done = true;
			    }

			    if (cache.isEmpty()) {
			        done = true;
			        System.out.println("✅ No more files to process — stopping iteration.");
			    }
			}


			private InputStream runChannel(String cmd) {
				try {
					System.out.println("Executing remote command...");
					ChannelExec ch = (ChannelExec) session.openChannel("exec");
					ch.setCommand(cmd);
					ch.setInputStream(null);
					InputStream in = ch.getInputStream();
					ch.connect();
					return in;
				} catch (Exception e) {
					System.err.println("SSH exec failed: " + e.getMessage());
					throw new RuntimeException(e);
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
