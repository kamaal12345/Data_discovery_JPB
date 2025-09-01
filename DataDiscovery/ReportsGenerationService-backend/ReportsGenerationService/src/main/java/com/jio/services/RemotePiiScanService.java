package com.jio.services;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.jio.convertor.PiiScanRequestConvertor;
import com.jio.customexception.BusinessException;
import com.jio.dto.PiiScanRequestDto;
import com.jio.dto.RemotePiiScanRequestDto;
import com.jio.entity.PiiScanRequest;
import com.jio.entity.PiiScanResult;
import com.jio.repository.PiiScanRequestRepository;
import com.jio.utils.OCRUtils;
import com.jio.utils.PathBatcher;
import com.jio.utils.PiiPatterns;
import com.jio.utils.SSHConnector;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;

@Service
@Slf4j
public class RemotePiiScanService {

	@Autowired
	private PiiScanRequestRepository piiScanRequestRepository;

	@Autowired
	private SSHConnector sshConnector;

	private static final int BATCH_SIZE = 300;

	@Value("${ocr.tessdata.path}")
	private String tessDataPath;

	private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "bmp", "tiff", "gif", "webp");
	private static final Set<String> TEXT_EXTS = Set.of("txt", "csv");
	private static final String PDF_EXT = "pdf";

	private static final Set<String> ALLOWED_EXTS;
	static {
		Set<String> s = new HashSet<>();
		s.addAll(IMAGE_EXTS);
		s.addAll(TEXT_EXTS);
		s.add(PDF_EXT);
		ALLOWED_EXTS = Collections.unmodifiableSet(s);
	}

	private static final Set<String> PROGRAM_FILE_EXTS = Set.of(
			// --- Scripting languages ---
			"sh", "bash", "bat", "cmd", "ps1", "zsh", "fish", "ksh", "csh",

			// --- Python ---
			"py", "pyc", "pyo", "pyw", "ipynb",

			// --- Java ---
			"java", "class", "jar", "war", "ear",

			// --- Kotlin ---
			"kt", "kts",

			// --- Scala ---
			"scala",

			// --- C/C++ ---
			"c", "cpp", "cc", "cxx", "hpp", "hxx", "h++", "ino", "tcc",

			// --- C# / .NET ---
			"cs", "config", "csproj", "sln", "vb", "vbx", "resx",

			// --- Go ---
			"go", "mod", "sum",

			// --- Rust ---
			"rs", "rlib", "toml", "cargo",

			// --- Ruby ---
			"rb", "erb", "gemspec", "rake",

			// --- PHP ---
			"php", "phtml", "php3", "php4", "php5", "phps", "phar",

			// --- JavaScript / TypeScript ---
			"js", "jsx", "ts", "tsx", "mjs", "cjs",

			// --- Swift / Objective-C ---
			"swift", "m", "mm", "h",

			// --- R ---
			"r", "rmd", "rproj",

			// --- Perl ---
			"pl", "pm", "pod", "t",

			// --- Lua ---
			"lua", "luac",

			// --- Dart ---
			"dart",

			// --- Haskell ---
			"hs", "lhs", "cabal",

			// --- Shell config / environment ---
			"env", "rc", "profile", "bashrc", "bash_profile", "zshrc", "aliases",

			// --- Assembly ---
			"asm", "s", "a51", "inc",

			// --- SQL / Database ---
			"sql", "db", "sqlite", "db3", "pgsql", "psql",

			// --- Web files / front-end frameworks ---
			"html", "htm", "css", "scss", "sass", "xml", "json", "yaml", "yml", "vue", "ejs", "hbs",

			// --- Make/build files ---
			"makefile", "cmake", "mk", "gradle", "groovy", "pom", "pom.xml", "build", "ninja",

			// --- Package managers ---
			"package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",

			// --- Executables and binaries ---
			"exe", "dll", "bin", "so", "a", "out", "app", "msi", "dmg", "deb", "rpm", "apk", "pkg", "iso");

	private static final Set<String> LINUX_DEFAULT_EXCLUDES = Set.of(
			// System directories (absolute)

			"/proc", "/sys", "/dev", "/run", "/tmp", "/var/log", "/var/cache", "/var", "/etc", "/boot", "/lib",
			"/lib64", "/usr", "/usr/lib", "/snap", "/media", "/mnt", "/app", "/opt", "oracle", "crashdump", "eclipse",
			"sts", "Temp", "temp", "bin", "Software", "bash_history", "redis",

			// Programming language folders (case-insensitive)
			"java", "JAVA", "Java", "python", "Python", "PYTHON", "go", "golang", "rust", "csharp", "c++", "cpp",
			"ruby", "php", "node", "typescript", "scala", "haskell", "perl",

			// Project/dev directories
			"node_modules", "logs", "log", "target", "main", "src", "dist", "obj", "__pycache__",

			// Tooling & VCS metadata
			".idea", ".vscode", ".angular", ".gradle", ".mvn", ".metadata", ".settings", ".git", ".svn", ".hg", ".m2",
			".npm", ".cache", ".config",

			// Common non-code files
			"md", "readme", "readme.md", "README", "ReadMe", "license", "license.txt", "License.txt", "changelog",
			"changelog.md", "icon", "icons");

	public PiiScanRequestDto scanViaSshOnce(RemotePiiScanRequestDto dto) throws Exception {
		String scanId = UUID.randomUUID().toString().substring(0, 8);
		log.info("🔍 Starting new scan: [{}] Target: {}, Path: {}", scanId, dto.getTargetName(), dto.getFilePath());

		List<PiiScanResult> results = new ArrayList<>();
		int stopAfter = dto.getStopScannAfter() != null ? dto.getStopScannAfter() : Integer.MAX_VALUE;

		List<String> hosts = dto.getConnection().getHost();
		if (hosts == null || hosts.isEmpty()) {
			throw new BusinessException("NO_HOSTS", "No host provided for SSH scan.");
		}

		for (String host : hosts) {
			Session session = null;
			try {
				session = sshConnector.connect(host, dto.getConnection().getPort(), dto.getConnection().getUsername(),
						dto.getConnection().getPassword());
				log.info("✅ SSH connection successful to host {}", host);
			} catch (Exception e) {
				String errorMessage = e.getMessage() != null ? e.getMessage() : "SSH connection failed";
				log.error("❌ SSH connection failed to host {}: {}", host, errorMessage);
				throw new BusinessException("SSH_CONNECTION_FAILED",
						"SSH connection failed to host " + host + ": " + errorMessage);
			} finally {
				if (session != null && session.isConnected()) {
					sshConnector.disconnect(session);
					log.debug("🔌 SSH session disconnected after verification for host {}", host);
				}
			}
		}

		for (String host : dto.getConnection().getHost()) {
			Session session = null;
			try {
				session = sshConnector.connectWithRetry(host, dto.getConnection().getPort(),
						dto.getConnection().getUsername(), dto.getConnection().getPassword(), 3);

				int[] counter = new int[] { 0 };
				scanRemoteSession(dto, session, host, results, counter, stopAfter);

				if (counter[0] >= stopAfter)
					break;

			} catch (Exception e) {
				String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown SSH error";
				log.error("SSH connect failed for host {}: {}", host, e.getMessage());
				results.add(new PiiScanResult("N/A", "SCAN_FAILED", "ERROR: " + errorMessage, host));
			} finally {
				if (session != null) {
					sshConnector.disconnect(session);
				}
			}
		}

		if (results.isEmpty()) {
			log.warn("No valid files found or invalid path provided, targetName={}, filePath={}", dto.getTargetName(),
					dto.getFilePath());
			throw new BusinessException("INVALID_PATH", "No valid files found or invalid path provided.");
		}

		PiiScanRequest saved = saveResults(dto, results);
		return PiiScanRequestConvertor.convertToPiiScanRequestDTO(saved);
	}

	@Transactional
	public PiiScanRequest saveResults(RemotePiiScanRequestDto dto, List<PiiScanResult> results) {
		PiiScanRequest request = PiiScanRequest.builder().serverType(dto.getServerType())
				.targetName(dto.getTargetName()).filePath(dto.getFilePath()).maxFileSize(dto.getMaxFileSize())
				.stopScannAfter(dto.getStopScannAfter()).piiTypes(dto.getPiiTypes())
				.excludePatterns(dto.getExcludePatterns()).createdById(dto.getCreatedById()).build();
		results.forEach(r -> r.setPiiScanRequest(request));
		request.setPiiScanResults(results);
		return piiScanRequestRepository.save(request);
	}

//	private void scanRemoteSession(RemotePiiScanRequestDto dto, Session session, String ip, List<PiiScanResult> results,
//			int[] cnt, int stopAfter) throws Exception {
//
//		long maxSize = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
//		Set<String> excludes = parseExcludes(dto.getExcludePatterns());
//
//		AtomicBoolean shouldStopSignal = new AtomicBoolean(false);
//		PathBatcher batches = new PathBatcher(session, dto.getFilePath(), maxSize, excludes, BATCH_SIZE, ALLOWED_EXTS,
//				shouldStopSignal);
//		String typesCsv = String.join(",", dto.getPiiTypes());
//
//		Iterator<List<String>> batchIterator = batches.iterator();
//
//		// 🧮 Dynamic thread pool size
//		int cpuCores = Runtime.getRuntime().availableProcessors();
//		int minThreads = 10;
//		int maxThreads = 50;
//		int calculated = Math.max(cpuCores * 2, 4); // base value
//		int threadPoolSize = Math.max(minThreads, Math.min(maxThreads, calculated));
//		log.info("🚀 Using thread pool size: {}", threadPoolSize);
//
//		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
//
//		AtomicInteger atomicCounter = new AtomicInteger(cnt[0]);
//
//		outerLoop: while (batchIterator.hasNext() && atomicCounter.get() < stopAfter) {
//			List<String> batch = batchIterator.next();
//			List<Future<?>> futures = new ArrayList<>();
//
//			for (String path : batch) {
//				if (atomicCounter.get() >= stopAfter) {
//					log.info("Stopping scan after reaching limit: {}", atomicCounter.get());
//					shouldStopSignal.set(true);
//					break outerLoop;
//				}
//
//				if (isExcluded(path, excludes)) {
//					log.info("Skipping excluded file, path={}", path);
//					continue;
//				}
//
//				String ext = FilenameUtils.getExtension(path).toLowerCase();
//				String folder = getParent(path);
//
//				if (ext.isBlank()) {
//					log.info("Skipping file with no extension {}", path);
//					continue;
//				}
//
//				if (PROGRAM_FILE_EXTS.contains(ext)) {
//					log.info("Skipping program file extension {}, path={}", ext, path);
//					continue;
//				}
//
//				if (!ALLOWED_EXTS.contains(ext)) {
//					log.info("Skipping unsupported file extension, path={}, ext={}", path, ext);
//					continue;
//				}
//
//				// Submit scan task
//				futures.add(executor.submit(() -> {
//					Session threadSession = null;
//					Path local = null;
//
//					try {
//						threadSession = sshConnector.connect(ip, dto.getConnection().getPort(),
//								dto.getConnection().getUsername(), dto.getConnection().getPassword());
//
//						log.info("🔍 Scanning file {}", path);
//
//						List<String> lsOutput = runCommand(threadSession, "ls -ld " + escape(path));
//						boolean denied = lsOutput.stream()
//								.anyMatch(l -> l.toLowerCase().contains("permission denied") || l.startsWith("ERROR:"));
//						if (denied || lsOutput.isEmpty()) {
//							recordFileLocked(path, folder, results, ip, new int[] { atomicCounter.get() });
//							return;
//						}
//
//						if (TEXT_EXTS.contains(ext)) {
//							List<String> lines = runCommand(threadSession, "cat " + escape(path));
//							int matched = scanLinesWithLimit(path, lines, typesCsv, results, ip,
//									stopAfter - atomicCounter.get());
//							atomicCounter.addAndGet(matched);
//							log.info("✅ Matched lines in text: {}", path);
//						} else {
//							try {
//								local = fetchRemoteFile(threadSession, path);
//								scanFile(local, path, dto.getPiiTypes(), results, ip);
//								atomicCounter.incrementAndGet();
//								log.info("✅ Scanned binary/image/pdf file: {}", path);
//							} finally {
//								if (local != null) {
//									try {
//										Files.deleteIfExists(local);
//									} catch (IOException e) {
//										log.warn("⚠️ Failed to delete temp file {}: {}", local, e.getMessage());
//									}
//								}
//							}
//						}
//					} catch (Exception e) {
//						log.error("❌ Error scanning {} : {}", path, e.getMessage(), e);
//					} finally {
//						if (threadSession != null && threadSession.isConnected()) {
//							threadSession.disconnect();
//						}
//					}
//				}));
//			}
//
//			// Wait for all tasks in batch
//			for (Future<?> future : futures) {
//				try {
//					future.get();
//				} catch (Exception e) {
//					log.error("Error waiting for scan task: {}", e.getMessage());
//				}
//			}
//		}
//
//		executor.shutdown();
//		executor.awaitTermination(10, TimeUnit.MINUTES);
//
//		cnt[0] = atomicCounter.get();
//
//		if (cnt[0] == 0) {
//			log.warn("⚠️ No PII matches found on host {}", ip);
//			results.add(new PiiScanResult("N/A", "NO_FILES_FOUND", "No valid files found on host", ip));
//		}
//	}

	private void scanRemoteSession(RemotePiiScanRequestDto dto, Session session, String ip, List<PiiScanResult> results,
			int[] cnt, int stopAfter) throws Exception {

		long maxSize = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
		Set<String> excludes = parseExcludes(dto.getExcludePatterns());

		AtomicBoolean shouldStopSignal = new AtomicBoolean(false);
		PathBatcher batches = new PathBatcher(session, dto.getFilePath(), maxSize, excludes, BATCH_SIZE, ALLOWED_EXTS,
				shouldStopSignal);
		String typesCsv = String.join(",", dto.getPiiTypes());

		Iterator<List<String>> batchIterator = batches.iterator();

		//  Dynamic thread pool size
		int cpuCores = Runtime.getRuntime().availableProcessors();
		int minThreads = 10;
		int maxThreads = 50;
		int calculated = Math.max(cpuCores * 2, 4);
		int threadPoolSize = Math.max(minThreads, Math.min(maxThreads, calculated));
		log.info("🚀 Using thread pool size: {}", threadPoolSize);

		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		AtomicInteger atomicCounter = new AtomicInteger(cnt[0]);

		outerLoop: while (batchIterator.hasNext() && atomicCounter.get() < stopAfter) {
			List<String> batch = batchIterator.next();
			List<Future<?>> futures = new ArrayList<>();

			for (String path : batch) {
				if (atomicCounter.get() >= stopAfter) {
					log.info("Stopping scan after reaching limit: {}", atomicCounter.get());
					shouldStopSignal.set(true);
					break outerLoop;
				}

				if (isExcluded(path, excludes)) {
					log.info("Skipping excluded file, path={}", path);
					continue;
				}

				String ext = FilenameUtils.getExtension(path).toLowerCase();
				String folder = getParent(path);

				if (ext.isBlank()) {
					log.info("Skipping file with no extension {}", path);
					continue;
				}
				if (PROGRAM_FILE_EXTS.contains(ext)) {
					log.info("Skipping program file extension {}, path={}", ext, path);
					continue;
				}
				if (!ALLOWED_EXTS.contains(ext)) {
					log.info("Skipping unsupported file extension, path={}, ext={}", path, ext);
					continue;
				}

				// Submit scan task
				futures.add(executor.submit(() -> {
					Session threadSession = null;
					Path local = null;
					try {
						threadSession = sshConnector.connect(ip, dto.getConnection().getPort(),
								dto.getConnection().getUsername(), dto.getConnection().getPassword());

						log.info("🔍 Scanning file {}", path);

						List<String> lsOutput = runCommand(threadSession, "ls -ld " + escape(path));
						boolean denied = lsOutput.stream()
								.anyMatch(l -> l.toLowerCase().contains("permission denied") || l.startsWith("ERROR:"));
						if (denied || lsOutput.isEmpty()) {
							recordFileLocked(path, folder, results, ip, new int[] { atomicCounter.get() });
							return;
						}

						if (TEXT_EXTS.contains(ext)) {
							List<String> lines = runCommand(threadSession, "cat " + escape(path));
							int matched = scanLinesWithLimit(path, lines, typesCsv, results, ip,
									stopAfter - atomicCounter.get());
							atomicCounter.addAndGet(matched);
							log.info("✅ Matched lines in text: {}", path);
						} else {
							try {
								local = fetchRemoteFile(threadSession, path);
								scanFile(local, path, dto.getPiiTypes(), results, ip);
								atomicCounter.incrementAndGet();
								log.info("✅ Scanned binary/image/pdf file: {}", path);
							} finally {
								if (local != null) {
									try {
										Files.deleteIfExists(local);
									} catch (IOException e) {
										log.warn("⚠️ Failed to delete temp file {}: {}", local, e.getMessage());
									}
								}
							}
						}
					} catch (Exception e) {
						log.error("❌ Error scanning {} : {}", path, e.getMessage(), e);
					} finally {
						if (threadSession != null && threadSession.isConnected()) {
							threadSession.disconnect();
						}
					}
				}));
			}

			// Wait for all tasks in batch (with timeout)
			for (Future<?> f : futures) {
				try {
					f.get(3, TimeUnit.MINUTES);
				} catch (TimeoutException te) {
					log.warn("⏳ Task timeout exceeded, cancelling...");
					f.cancel(true);
				} catch (Exception e) {
					log.error("Task execution error: {}", e.getMessage());
				}
			}
		}

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.MINUTES);

		cnt[0] = atomicCounter.get();

		if (cnt[0] == 0) {
			log.warn("⚠️ No PII matches found on host {}", ip);
			results.add(new PiiScanResult("N/A", "NO_FILES_FOUND", "No valid files found on host", ip));
		}
	}

	private void addFolderResult(List<PiiScanResult> results, String folder, String reason, String ip, int[] cnt) {
		results.add(new PiiScanResult(folder, reason, "NO_MATCH", ip));
		cnt[0]++;
	}

	private void recordFileLocked(String path, String folder, List<PiiScanResult> results, String ip, int[] cnt) {
		addFolderResult(results, folder, "FILE_LOCKED", ip, cnt);
	}

	private Path fetchRemoteFile(Session session, String remotePath) throws Exception {
		String b64 = runCommandAsString(session, "base64 " + escape(remotePath));
		byte[] data = Base64.getDecoder().decode(b64);
		Path tmp = Files.createTempFile("remote_", "_" + Paths.get(remotePath).getFileName());
		Files.write(tmp, data);
		log.debug("Fetched remote file to temp, remotePath={}", remotePath);
		return tmp;
	}

	private List<String> runCommand(Session session, String cmd) throws Exception {
		cmd = "sudo " + cmd;
		List<String> output = new ArrayList<>();

		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(cmd);
		channel.setInputStream(null);

		InputStream in = channel.getInputStream();
		channel.connect();

		InputStream err = channel.getErrStream();

		try (BufferedReader outReader = new BufferedReader(new InputStreamReader(in));
				BufferedReader errReader = new BufferedReader(new InputStreamReader(err))) {

			String line;
			while ((line = outReader.readLine()) != null) {
				output.add(line);
			}
			while ((line = errReader.readLine()) != null) {
				output.add("ERROR: " + line);
			}

		} finally {
			channel.disconnect();
		}

		return output;
	}

	private String runCommandAsString(Session session, String cmd) throws Exception {
		cmd = "sudo " + cmd;
		log.debug("Running command as string, cmd={}", cmd);
		ChannelExec ch = (ChannelExec) session.openChannel("exec");
		ch.setCommand(cmd);
		InputStream in = ch.getInputStream();
		ch.connect();

		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String l;
			while ((l = br.readLine()) != null)
				sb.append(l);
		} finally {
			ch.disconnect();
		}
		return sb.toString();
	}

//	private int scanLinesWithLimit(String path, List<String> lines, String typesCsv, List<PiiScanResult> results,
//			String ip, int cap) {
//		int added = 0;
//		Set<String> types = typesCsv.isEmpty() ? Collections.emptySet() : Set.of(typesCsv.split(","));
//		for (String line : lines) {
//			for (var en : PiiPatterns.PII_PATTERNS.entrySet()) {
//				if (!types.isEmpty() && !types.contains(en.getKey()))
//					continue;
//				Matcher m = en.getValue().matcher(line);
//				while (m.find() && added < cap) {
//					results.add(new PiiScanResult(path, en.getKey(), m.group(), ip));
//					added++;
//				}
//			}
//		}
//		return added;
//	}
//
//	private void scanFile(Path localFile, String remotePath, List<String> piiTypes, List<PiiScanResult> results,
//			String ip) {
//		StringBuilder textBuilder = new StringBuilder();
//		try {
//			String ext = getExtension(localFile.toString().toLowerCase());
//
//			if ("pdf".equals(ext)) {
//				try (PDDocument doc = PDDocument.load(localFile.toFile())) {
//
//					// Step 1: Try extracting text directly from the PDF
//					PDFTextStripper stripper = new PDFTextStripper();
//					String extractedText = stripper.getText(doc);
//					if (extractedText != null && !extractedText.isBlank()) {
//						textBuilder.append(extractedText.trim()).append(" ");
//					}
//
//					// Step 2: Perform plain OCR
//					String plainOCRText = performPlainOCR(doc);
//					if (plainOCRText != null && !plainOCRText.isBlank()) {
//						textBuilder.append(plainOCRText).append(" ");
//					}
//
//					// Step 3: Perform enhanced OCR only if plain OCR is weak (e.g., < 50 chars)
//					boolean needsEnhancedOCR = (extractedText == null || extractedText.strip().length() < 50)
//							&& (plainOCRText == null || plainOCRText.strip().length() < 50);
//
//					if (needsEnhancedOCR) {
//						String enhancedOCRText = performOCRWithOpenCV(doc);
//						if (enhancedOCRText != null && !enhancedOCRText.isBlank()) {
//							textBuilder.append(enhancedOCRText).append(" ");
//						}
//					}
//				}
//
//			} else if (isImageExt(ext)) {
//				String plainImgOCR = performPlainOCROnImage(localFile.toFile());
//				if (plainImgOCR != null && !plainImgOCR.isBlank()) {
//					textBuilder.append(plainImgOCR).append(" ");
//				}
//
//				if (plainImgOCR == null || plainImgOCR.strip().length() < 50) {
//					String processedImgOCR = OCRUtils.performOCRWithPreprocessing(localFile.toFile(), tessDataPath);
//					if (processedImgOCR != null && !processedImgOCR.isBlank()) {
//						textBuilder.append(processedImgOCR).append(" ");
//					}
//				}
//
//			}
//
//			// Clean and process final text
//			String finalText = textBuilder.toString().replaceAll("[\\n\\r]+", " ").trim();
//
//			if (!finalText.isBlank()) {
//				for (var entry : PiiPatterns.PII_PATTERNS.entrySet()) {
//					if (piiTypes.isEmpty() || piiTypes.contains(entry.getKey())) {
//						Matcher matcher = entry.getValue().matcher(finalText);
//						while (matcher.find()) {
//							String match = matcher.group();
//							results.add(new PiiScanResult(remotePath, entry.getKey(), match, ip));
//						}
//					}
//				}
//			}
//
//		} catch (Exception e) {
//			log.error("❌ scanFile error: " + localFile + ": " + e.getMessage());
//		}
//	}

	// ✅ Account/Demat/CIF related keywords for context filtering
	private static final Set<String> MICR_CONTEXT_KEYWORDS = Set.of("micr", "cheque", "branch code", "bank code");

	private static final Set<String> ACCOUNT_CONTEXT_KEYWORDS = Set.of("account", "acct", "a/c", "bank", "account no",
			"ac no", "acc no", "account number", "cif", "cif number", "customer information file", "demat",
			"dematerialized", "loan account", "fd account", "fixed deposit", "savings account", "current account",
			"customer id");

	private static final Map<String, List<String>> DOC_KEYWORDS = Map.of("aadhaar",
			List.of("government of india", "aadhaar", "enrolment no", "uidai"), "pan",
			List.of("permanent account number", "income tax department"), "bank",
			List.of("bank", "ifsc", "micr", "branch code", "customer name", "loan account", "fd account", "customer id",
					"demat account", "account no", "account number", "cif number"),
			"passport", List.of("republic of india", "भारत गणराज्य"), "driving_license",
			List.of("indian union driving licence", "rta", "non transport", "transport vehicle"), "voter_id",
			List.of("election commission of india", "epic no", "identity card"));

	// Normalize the match for dedupe
	private static String normalizeForType(String piiType, String raw) {
		String s = raw.trim();
		switch (piiType) {
		case "aadhaar":
		case "account_number":
		case "cif_number":
		case "demat_account":
		case "credit_card":
		case "debit_card":
		case "micr":
		case "ckyc":
			return s.replaceAll("[\\s-]", "");
		case "pan":
		case "voter":
		case "passport":
		case "dl":
		case "vehicle_number":
		case "ifsc":
		case "customer_id":
		case "loan_account":
		case "fd_account":
			return s.toUpperCase().replaceAll("[^A-Z0-9]", "");
		default:
			return s;
		}
	}

	private static String dedupeKey(String piiType, String match) {
		return piiType + "|" + normalizeForType(piiType, match);
	}

	private boolean hasContext(String text, String match, Set<String> keywords) {
		String lowerText = text.toLowerCase();
		String lowerMatch = match.toLowerCase();
		int idx = lowerText.indexOf(lowerMatch);
		if (idx == -1)
			return false;

		int start = Math.max(0, idx - 30);
		int end = Math.min(lowerText.length(), idx + lowerMatch.length() + 30);
		String context = lowerText.substring(start, end);

		for (String keyword : keywords) {
			if (context.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private int scanLinesWithLimit(String path, List<String> lines, String typesCsv, List<PiiScanResult> results,
			String ip, int cap) {
		int added = 0;
		Set<String> types = typesCsv == null || typesCsv.isBlank() ? Collections.emptySet()
				: new HashSet<>(Arrays.asList(typesCsv.toLowerCase().split(",")));

		Set<String> seen = new HashSet<>();

		for (String line : lines) {
			for (var en : PiiPatterns.PII_PATTERNS.entrySet()) {
				String piiType = en.getKey();
				if (!types.isEmpty() && !types.contains(piiType))
					continue;

				Matcher m = en.getValue().matcher(line);
				while (m.find() && added < cap) {
					String rawMatch = m.group().trim();

					if (piiType.equals("micr") && !hasContext(line, rawMatch, MICR_CONTEXT_KEYWORDS))
						continue;

					if ((piiType.equals("account_number") || piiType.equals("demat_account")
							|| piiType.equals("cif_number") || piiType.equals("loan_account")
							|| piiType.equals("fd_account") || piiType.equals("customer_id"))
							&& !hasContext(line, rawMatch, ACCOUNT_CONTEXT_KEYWORDS))
						continue;

					Predicate<String> validator = PiiPatterns.PII_VALIDATORS.get(piiType);
					if (validator != null && !validator.test(rawMatch))
						continue;

					String key = dedupeKey(piiType, rawMatch);
					if (!seen.add(key))
						continue;

					results.add(new PiiScanResult(path, piiType, rawMatch, ip));
					added++;
				}
			}
		}
		return added;
	}

	private void scanFile(Path localFile, String remotePath, List<String> piiTypes, List<PiiScanResult> results,
			String ip) {
		StringBuilder textBuilder = new StringBuilder();
		Set<String> seen = new HashSet<>();

		try {
			String ext = getExtension(localFile.toString().toLowerCase());
			boolean isImageOrPdf = isImageExt(ext) || "pdf".equals(ext);

			// --- 1️⃣ Extract text from file ---
			if ("pdf".equals(ext)) {
				try (PDDocument doc = PDDocument.load(localFile.toFile())) {
					String extractedText = new PDFTextStripper().getText(doc);
					if (extractedText != null && !extractedText.isBlank()) {
						textBuilder.append(extractedText.trim()).append(" ");
					}
					String plainOCRText = performPlainOCR(doc);
					if (plainOCRText != null && !plainOCRText.isBlank()) {
						textBuilder.append(plainOCRText).append(" ");
					}
					boolean needsEnhancedOCR = (extractedText == null || extractedText.strip().length() < 50)
							&& (plainOCRText == null || plainOCRText.strip().length() < 50);
					if (needsEnhancedOCR) {
						String enhancedOCRText = performOCRWithOpenCV(doc);
						if (enhancedOCRText != null && !enhancedOCRText.isBlank()) {
							textBuilder.append(enhancedOCRText).append(" ");
						}
					}
				}
			} else if (isImageExt(ext)) {
				String plainImgOCR = performPlainOCROnImage(localFile.toFile());
				if (plainImgOCR != null && !plainImgOCR.isBlank()) {
					textBuilder.append(plainImgOCR).append(" ");
				}
				if (plainImgOCR == null || plainImgOCR.strip().length() < 50) {
					String processedImgOCR = OCRUtils.performOCRWithPreprocessing(localFile.toFile(), tessDataPath);
					if (processedImgOCR != null && !processedImgOCR.isBlank()) {
						textBuilder.append(processedImgOCR).append(" ");
					}
				}
			} else {
				// For text files
				List<String> lines = Files.readAllLines(localFile);
				for (String line : lines) {
					textBuilder.append(line).append(" ");
				}
			}

			String finalText = textBuilder.toString().replaceAll("[\\n\\r]+", " ").trim();
			if (finalText.isBlank())
				return;

			// --- 2️⃣ If Image/PDF → Try keyword-based document type detection ---
			if (isImageOrPdf) {
				Map<String, Integer> keywordMatchCounts = new HashMap<>();
				int totalKeywordMatches = 0;

				for (var keywordEntry : DOC_KEYWORDS.entrySet()) {
					String docType = keywordEntry.getKey();
					int count = 0;
					for (String keyword : keywordEntry.getValue()) {
						if (finalText.toLowerCase().contains(keyword.toLowerCase())) {
							count++;
						}
					}
					if (count > 0) {
						keywordMatchCounts.put(docType, count);
						totalKeywordMatches += count;
					}
				}

				if (!keywordMatchCounts.isEmpty()) {
					String dominantType = Collections.max(keywordMatchCounts.entrySet(), Map.Entry.comparingByValue())
							.getKey();
					int dominantCount = keywordMatchCounts.get(dominantType);

					// ✅ If dominant type ≥90% → scan only that type
					if ((dominantCount / (double) totalKeywordMatches) >= 0.9) {
						Pattern pattern = PiiPatterns.PII_PATTERNS.get(dominantType);
						if (pattern != null) {
							Matcher matcher = pattern.matcher(finalText);
							while (matcher.find()) {
								String rawMatch = matcher.group().trim();
								String key = dedupeKey(dominantType, rawMatch);
								if (!seen.add(key))
									continue;
								results.add(new PiiScanResult(remotePath, dominantType, rawMatch, ip));
							}
						}
						return; // Stop here
					}
				}
			}

			// --- 3️⃣ Fallback: Scan all PII patterns ---
			for (var entry : PiiPatterns.PII_PATTERNS.entrySet()) {
				String piiType = entry.getKey();
				if (!piiTypes.isEmpty() && !piiTypes.contains(piiType))
					continue;

				Matcher matcher = entry.getValue().matcher(finalText);
				while (matcher.find()) {
					String rawMatch = matcher.group().trim();

					if (piiType.equals("micr") && !hasContext(finalText, rawMatch, MICR_CONTEXT_KEYWORDS))
						continue;
					if ((piiType.equals("account_number") || piiType.equals("demat_account")
							|| piiType.equals("cif_number") || piiType.equals("loan_account")
							|| piiType.equals("fd_account") || piiType.equals("customer_id"))
							&& !hasContext(finalText, rawMatch, ACCOUNT_CONTEXT_KEYWORDS))
						continue;

					Predicate<String> validator = PiiPatterns.PII_VALIDATORS.get(piiType);
					if (validator != null && !validator.test(rawMatch))
						continue;

					String key = dedupeKey(piiType, rawMatch);
					if (!seen.add(key))
						continue;

					results.add(new PiiScanResult(remotePath, piiType, rawMatch, ip));
				}
			}

		} catch (Exception e) {
			log.error("❌ scanFile error: " + localFile + ": " + e.getMessage());
		}
	}

	private String performPlainOCR(PDDocument doc) {
		StringBuilder ocrText = new StringBuilder();
		try {
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			Tesseract tesseract = new Tesseract();
			tesseract.setDatapath(tessDataPath);
			tesseract.setLanguage("eng");

			int totalPages = doc.getNumberOfPages();
			int maxPagesToProcess = Math.min(totalPages, 4); // Limit to 4 pages max

			for (int page = 0; page < maxPagesToProcess; ++page) {
				try {
					log.info("📄 [PlainOCR] Processing PDF page {}/{}", page + 1, totalPages);
					BufferedImage pageImage = pdfRenderer.renderImageWithDPI(page, 300);
					String pageText = tesseract.doOCR(pageImage);
					ocrText.append(pageText).append("\n");
				} catch (Exception e) {
					log.error("❌ Plain OCR failed on page {}: {}", page + 1, e.getMessage());
				}
			}

		} catch (Exception e) {
			log.error("OCR Error in performPlainOCR: {}", e.getMessage());
		}

		return ocrText.toString();
	}

	private String performPlainOCROnImage(File imageFile) {
		StringBuilder ocrText = new StringBuilder();
		try {
			BufferedImage image = safeReadImage(imageFile);
			if (image == null) {
				log.warn("⚠️ Image unreadable or unsupported: {}", imageFile.getName());
				return "";
			}

			Tesseract tesseract = new Tesseract();
			tesseract.setDatapath(tessDataPath);
			tesseract.setLanguage("eng");

			String pageText = tesseract.doOCR(image);
			ocrText.append(pageText).append("\n");

		} catch (Exception e) {
			log.error("OCR Error in performPlainOCROnImage: {}", e.getMessage());
		}
		return ocrText.toString();
	}

	private String performOCRWithOpenCV(PDDocument doc) {
		StringBuilder ocrText = new StringBuilder();
		try {
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			int totalPages = doc.getNumberOfPages();
			int maxPagesToProcess = Math.min(totalPages, 4); // Limit to 4 pages

			for (int page = 0; page < maxPagesToProcess; ++page) {
				try {
					log.info("📄 [EnhancedOCR] Processing PDF page {}/{}", page + 1, totalPages);
					BufferedImage pageImage = pdfRenderer.renderImageWithDPI(page, 400);

					File tempImageFile = File.createTempFile("ocr_page_" + page, ".png");
					ImageIO.write(pageImage, "png", tempImageFile);

					String pageText = OCRUtils.performOCRWithPreprocessing(tempImageFile, tessDataPath);
					ocrText.append(pageText).append("\n");

					if (!tempImageFile.delete()) {
						log.warn("⚠️ Could not delete temp image: {}", tempImageFile.getAbsolutePath());
					}
				} catch (Exception e) {
					log.error("❌ Enhanced OCR failed on page {}: {}", page + 1, e.getMessage());
				}
			}

		} catch (Exception e) {
			log.error("OCR Error in performOCRWithOpenCV: {}", e.getMessage());
		}

		return ocrText.toString();
	}

	private String getExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot > 0 && lastDot < filename.length() - 1) {
			return filename.substring(lastDot + 1).toLowerCase();
		}
		return "";
	}

	private boolean isImageExt(String ext) {
		return IMAGE_EXTS.contains(ext);
	}

	private String escape(String s) {
		return "'" + s.replace("'", "'\\''") + "'";
	}

	private static final Set<Character> ILLEGAL_CHARS_LINUX = Set.of(':', '*', '?', '"', '<', '>', '|');

	public static boolean hasIllegalChars(String path) {
		for (char c : path.toCharArray()) {
			if (ILLEGAL_CHARS_LINUX.contains(c)) {
				return true;
			}
		}
		return false;
	}

	private boolean isExcluded(String path, Set<String> excludes) {
		if (hasIllegalChars(path))
			return true;
		return excludes.stream().anyMatch(path::contains);
	}

	private String getParent(String path) {
		int lastSlash = path.lastIndexOf('/');
		return lastSlash > 0 ? path.substring(0, lastSlash) : "/";
	}

	private Set<String> parseExcludes(String s) {
		Set<String> excludes = new HashSet<>(LINUX_DEFAULT_EXCLUDES);

		if (s != null && !s.isBlank()) {
			excludes.addAll(Set.of(s.split(",")));
		}

		return excludes;
	}

	private BufferedImage safeReadImage(File imageFile) {
		try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (!readers.hasNext()) {
				log.warn("❌ No ImageReader found for file: {}", imageFile.getAbsolutePath());
				return null;
			}

			ImageReader reader = readers.next();
			reader.setInput(iis, true);
			log.info("📸 Detected image format '{}' for file: {}", reader.getFormatName(), imageFile.getName());
			return reader.read(0);

		} catch (IOException e) {
			log.error("❌ Error reading image {}: {}", imageFile.getAbsolutePath(), e.getMessage());
			return null;
		}
	}

}