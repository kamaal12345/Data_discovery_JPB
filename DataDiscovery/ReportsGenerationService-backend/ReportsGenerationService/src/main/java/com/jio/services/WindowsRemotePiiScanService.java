package com.jio.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

import com.jio.convertor.PiiScanRequestConvertor;
import com.jio.customexception.BusinessException;
import com.jio.dto.PiiScanRequestDto;
import com.jio.dto.RemotePiiScanRequestDto;
import com.jio.entity.PiiScanRequest;
import com.jio.entity.PiiScanResult;
import com.jio.repository.PiiScanRequestRepository;
import com.jio.utils.OCRUtils;
import com.jio.utils.PiiPatterns;
import com.jio.utils.WinRMConnector;
import com.jio.utils.WindowsPathBatcher;

import io.cloudsoft.winrm4j.winrm.WinRmTool;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;

@Service
@Slf4j
public class WindowsRemotePiiScanService {

	@Autowired
	private PiiScanRequestRepository piiScanRequestRepository;

	@Autowired
	private WinRMConnector winRmConnector;

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
			"exe", "dll", "bin", "so", "a", "out", "app", "msi", "dmg", "deb", "rpm", "apk", "pkg", "img", "iso");

	private static final Set<String> WINDOWS_DEFAULT_EXCLUDES = Set.of(
			// Windows system & user dirs
			"$Recycle.Bin", "Program Files", "Program Files (x86)", "ProgramData", "AppData", "Windows",
			"Documents and Settings", "Python313", "Public", "eclipse", "sts", "Temp", "temp", "OpenSSH-Win64",
			"PerfLogs","Software",

			// Programming language folders
			"Java", "Python", "Go", "Rust", "CSharp", "C++", "CPP", "Ruby", "PHP", "Node", "TypeScript", "Scala",
			"Perl",

			// Dev/build/tool folders
			"node_modules", "logs", "log", "build", "out", "target", "main", "src", "dist", "bin", "obj", "venv", "env",
			"__pycache__", "readme", "readme.md", "README", "ReadMe", "license", "license.txt", "License.txt",
			"LICENSE.txt", "changelog", "changelog.md", "icon", "icons");

	public PiiScanRequestDto scanViaWinRmOnce(RemotePiiScanRequestDto dto) throws Exception {
		String scanId = UUID.randomUUID().toString().substring(0, 8);
		log.info("🔍 Starting new Windows scan: [{}] Target: {}, Path: {}", scanId, dto.getTargetName(),
				dto.getFilePath());

		List<PiiScanResult> results = new ArrayList<>();
		int stopAfter = dto.getStopScannAfter() != null ? dto.getStopScannAfter() : Integer.MAX_VALUE;

		List<String> hosts = dto.getConnection().getHost();
		if (hosts == null || hosts.isEmpty()) {
			throw new BusinessException("NO_HOSTS", "No host provided for WinRM scan.");
		}

		for (String host : hosts) {
			WinRmTool tool = null;
			try {
				tool = winRmConnector.connect(host, dto.getConnection().getUsername(),
						dto.getConnection().getPassword());
				log.info("✅ WinRM connection successful to host {}", host);

				int[] counter = new int[] { 0 };
				scanWinRmSession(dto, tool, host, results, counter, stopAfter);

				if (counter[0] >= stopAfter)
					break;

			} catch (Exception e) {
				String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown WinRM error";
				log.error("WinRM connect failed for host {}: {}", host, errorMessage);
				results.add(new PiiScanResult("N/A", "SCAN_FAILED", "ERROR: " + errorMessage, host));
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

//	private void scanWinRmSession(RemotePiiScanRequestDto dto, WinRmTool tool, String host, List<PiiScanResult> results,
//			int[] counter, int stopAfter) throws Exception {
//
//		long maxSize = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
//		Set<String> excludes = parseExcludes(dto.getExcludePatterns());
//
//		WindowsPathBatcher batcher = new WindowsPathBatcher(tool, dto.getFilePath(), maxSize, excludes, ALLOWED_EXTS,
//				BATCH_SIZE);
//		String typesCsv = String.join(",", dto.getPiiTypes());
//		
//	    List<String> drivesToScan;
//	    if ("/".equals(dto.getFilePath().trim())) {
//	        String ps = "powershell -Command \"Get-Volume | Where-Object { $_.DriveLetter -and $_.FileSystem } | " +
//	                    "Select-Object -ExpandProperty DriveLetter\"";
//	        var resp = tool.executeCommand(ps);
//	        if (resp.getStatusCode() == 0) {
//	            drivesToScan = Arrays.stream(resp.getStdOut().split("\\r?\\n"))
//	                                  .map(String::trim)
//	                                  .filter(s -> !s.isEmpty())
//	                                  .map(s -> s + ":\\" )
//	                                  .collect(Collectors.toList());
//	            log.info("Scanning all drives: {}", drivesToScan);
//	        } else {
//	            throw new RuntimeException("Drive enumeration failed: " + resp.getStdErr());
//	        }
//	    } else {
//	        drivesToScan = List.of(dto.getFilePath());
//	    }
//
//		// Thread pool size - tune as needed (8 threads example)
//		int threadPoolSize = 10;
//		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
//
//		// Thread-safe counter and results list
//		AtomicInteger atomicCounter = new AtomicInteger(0);
//		List<PiiScanResult> synchronizedResults = Collections.synchronizedList(results);
//
//		// Tolerance: 10% of stopAfter
//		int tolerance = (int) (stopAfter * 0.1);
//
//		List<Future<?>> futures = new ArrayList<>();
//
//		outerLoop: for (List<String> batch : batcher) {
//			for (String path : batch) {
//				if (atomicCounter.get() >= stopAfter + tolerance) {
//					log.info("Stopping scan after reaching limit with tolerance: {}", atomicCounter.get());
//					break outerLoop;
//				}
//
//				if (isExcluded(path, excludes)) {
//					log.info("Skipping excluded file, path={}", path);
//					continue;
//				}
//
//				String ext = FilenameUtils.getExtension(path).toLowerCase();
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
//				// Submit scanning task to executor
//				Future<?> future = executor.submit(() -> {
//					try {
//						// Check counter again inside thread to avoid race condition
//						int currentCount = atomicCounter.incrementAndGet();
//						if (currentCount > stopAfter + tolerance) {
//							// Exceeded limit, skip processing
//							atomicCounter.decrementAndGet(); // rollback counter increment
//							return;
//						}
//
//						log.info("Scanning file {}", path);
//
//						if (TEXT_EXTS.contains(ext)) {
//							// Fetch lines remotely and scan
//							List<String> lines = winRmFetchTextLines(tool, path);
//							int matchedCount = scanLinesWithLimit(path, lines, typesCsv, synchronizedResults, host,
//									stopAfter - atomicCounter.get());
//							atomicCounter.addAndGet(matchedCount - 1); // -1 because we already incremented once
//							log.info("Matched lines in text: {}", path);
//						} else {
//							// Fetch binary file locally and scan
//							File local = winRmFetchFileAsTemp(tool, path);
//							scanFile(local.toPath(), path, dto.getPiiTypes(), synchronizedResults, host);
//							local.delete();
//							log.info("Scanned binary/image/pdf file: {}", path);
//						}
//					} catch (Exception e) {
//						log.error("Error scanning {} : {}", path, e.getMessage());
//					}
//				});
//
//				futures.add(future);
//			}
//		}
//
//		// Wait for all tasks to finish
//		for (Future<?> f : futures) {
//			try {
//				f.get();
//			} catch (Exception e) {
//				log.error("Error waiting for scan task: {}", e.getMessage());
//			}
//		}
//
//		// Shutdown the executor
//		executor.shutdown();
//		executor.awaitTermination(10, TimeUnit.MINUTES);
//	}

	private void scanWinRmSession(RemotePiiScanRequestDto dto, WinRmTool tool, String host, 
	        List<PiiScanResult> results, int[] counter, int stopAfter) throws Exception {

	    long maxSize = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
	    Set<String> excludes = parseExcludes(dto.getExcludePatterns());

	    List<String> drivesToScan;
	    if ("/".equals(dto.getFilePath().trim())) {
	        String ps = "powershell -Command \"Get-Volume | Where-Object { $_.DriveLetter -and $_.FileSystem } | " +
	                "Select-Object -ExpandProperty DriveLetter\"";
	        var resp = tool.executeCommand(ps);
	        if (resp.getStatusCode() == 0) {
	            drivesToScan = Arrays.stream(resp.getStdOut().split("\\r?\\n"))
	                    .map(String::trim)
	                    .filter(s -> !s.isEmpty())
	                    .map(s -> s + ":\\")
	                    .collect(Collectors.toList());
	            log.info("📀 Scanning all drives: {}", drivesToScan);
	        } else {
	            throw new RuntimeException("Drive enumeration failed: " + resp.getStdErr());
	        }
	    } else {
	        drivesToScan = List.of(dto.getFilePath());
	    }

	    // Thread pool sizing
	    int cpuCores = Runtime.getRuntime().availableProcessors();
	    int minThreads = 10;
	    int maxThreads = 50;
	    int calculated = Math.max(cpuCores * 2, drivesToScan.size() * 2);
	    int threadPoolSize = Math.max(minThreads, Math.min(maxThreads, calculated));
	    log.info("🚀 Using thread pool size: {}", threadPoolSize);

	    ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

	    String typesCsv = String.join(",", dto.getPiiTypes());
	    AtomicInteger atomicCounter = new AtomicInteger(0);
	    List<PiiScanResult> synchronizedResults = Collections.synchronizedList(results);
	    int tolerance = (int) (stopAfter * 0.1);

	    // Shared stop flag for batcher
	    AtomicBoolean shouldStopSignal = new AtomicBoolean(false);

	    List<Future<?>> futures = new ArrayList<>();

	    for (String drivePath : drivesToScan) {
	        log.info("📁 Starting scan on drive: {}", drivePath);
	        WindowsPathBatcher batcher = new WindowsPathBatcher(
	                tool, drivePath, maxSize, excludes, ALLOWED_EXTS, BATCH_SIZE, shouldStopSignal
	        );

	        outerLoop:
	        for (List<String> batch : batcher) {
	            for (String path : batch) {
	                if (atomicCounter.get() >= stopAfter + tolerance) {
	                    log.info("🚫 Reached limit — sending stop signal to batcher.");
	                    shouldStopSignal.set(true);
	                    break outerLoop;
	                }

	                if (isExcluded(path, excludes)) {
	                    log.info("⛔ Skipping excluded file, path={}", path);
	                    continue;
	                }

	                String ext = FilenameUtils.getExtension(path).toLowerCase();
	                if (ext.isBlank()) {
	                    log.info("⛔ Skipping file with no extension: {}", path);
	                    continue;
	                }

	                if (PROGRAM_FILE_EXTS.contains(ext)) {
	                    log.info("⛔ Skipping program file extension {}, path={}", ext, path);
	                    continue;
	                }

	                if (!ALLOWED_EXTS.contains(ext)) {
	                    log.info("⛔ Skipping unsupported file extension, path={}, ext={}", path, ext);
	                    continue;
	                }

	                Future<?> future = executor.submit(() -> {
	                    try {
	                        int currentCount = atomicCounter.incrementAndGet();
	                        if (currentCount > stopAfter + tolerance) {
	                            atomicCounter.decrementAndGet();
	                            shouldStopSignal.set(true);
	                            return;
	                        }

	                        log.info("🔎 Scanning file: {}", path);

	                        if (TEXT_EXTS.contains(ext)) {
	                            List<String> lines = winRmFetchTextLines(tool, path);
	                            int matchedCount = scanLinesWithLimit(path, lines, typesCsv, synchronizedResults, host,
	                                    stopAfter - atomicCounter.get());
	                            atomicCounter.addAndGet(matchedCount - 1);
	                            log.info("✅ Matched lines in text: {}", path);
	                        } else {
	                            File local = winRmFetchFileAsTemp(tool, path);
	                            scanFile(local.toPath(), path, dto.getPiiTypes(), synchronizedResults, host);
	                            local.delete();
	                            log.info("✅ Scanned binary/image/pdf file: {}", path);
	                        }
	                    } catch (Exception e) {
	                        log.error("❌ Error scanning {} : {}", path, e.getMessage());
	                    }
	                });

	                futures.add(future);
	            }
	        }
	    }

	    for (Future<?> f : futures) {
	        try {
	            f.get();
	        } catch (Exception e) {
	            log.error("❌ Error waiting for scan task: {}", e.getMessage());
	        }
	    }

	    executor.shutdown();
	    if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
	        log.warn("⚠️ Executor did not terminate within timeout, forcing shutdown.");
	        executor.shutdownNow();
	    }
	}


	@Transactional
	public PiiScanRequest saveResults(RemotePiiScanRequestDto dto, List<PiiScanResult> results) {
		PiiScanRequest request = PiiScanRequest.builder().serverType(dto.getServerType()) .targetName(dto.getTargetName()).filePath(dto.getFilePath())
				.maxFileSize(dto.getMaxFileSize()).stopScannAfter(dto.getStopScannAfter()).piiTypes(dto.getPiiTypes())
				.excludePatterns(dto.getExcludePatterns()).createdById(dto.getCreatedById()).build();
		results.forEach(r -> r.setPiiScanRequest(request));
		request.setPiiScanResults(results);
		return piiScanRequestRepository.save(request);
	}

	private Set<String> parseExcludes(String excludePatternsCsv) {
		Set<String> excludes = new HashSet<>(WINDOWS_DEFAULT_EXCLUDES);

		if (excludePatternsCsv != null && !excludePatternsCsv.isBlank()) {
			String[] userExcludes = excludePatternsCsv.split(",");
			for (String ex : userExcludes) {
				if (!ex.isBlank()) {
					excludes.add(ex.trim());
				}
			}
		}
		return excludes;
	}

//	private int scanLinesWithLimit(String path, List<String> lines, String typesCsv, List<PiiScanResult> results,
//			String ip, int cap) {
//		int added = 0;
//		Set<String> types = typesCsv.isEmpty() ? Collections.emptySet() : Set.of(typesCsv.split(","));
//
//		for (String line : lines) {
//			for (var entry : PiiPatterns.PII_PATTERNS.entrySet()) {
//				String piiType = entry.getKey();
//
//				if (!types.isEmpty() && !types.contains(piiType)) {
//					continue;
//				}
//
//				Matcher matcher = entry.getValue().matcher(line);
//				while (matcher.find() && added < cap) {
//					results.add(new PiiScanResult(path, piiType, matcher.group(), ip));
//					added++;
//				}
//			}
//			if (added >= cap)
//				break;
//		}
//		return added;
//	}

	// 2. Fetch text file content
	private List<String> winRmFetchTextLines(WinRmTool tool, String path) {
		var resp = tool.executeCommand("powershell -Command \"Get-Content -Path '" + path + "'\"");

		if (resp.getStatusCode() != 0) {
			log.error("Failed to fetch file content: {}", resp.getStdErr());
			return Collections.emptyList();
		}

		return Arrays.stream(resp.getStdOut().split("\\r?\\n")).map(String::trim).filter(line -> !line.isEmpty())
				.collect(Collectors.toList());
	}

	// 3. Fetch binary (PDF/image) as temporary local file
	private File winRmFetchFileAsTemp(WinRmTool tool, String path) throws IOException {

		String escapedPath = path.replace("'", "''");

		String psCommand = "[Convert]::ToBase64String([IO.File]::ReadAllBytes('" + escapedPath + "'))";
		String fullCommand = "powershell -Command \"" + psCommand + "\"";

		var resp = tool.executeCommand(fullCommand);

		String base64 = resp.getStdOut().replaceAll("\\s+", "");
		byte[] fileData = Base64.getDecoder().decode(base64);

		String fileName = Paths.get(path).getFileName().toString();
		String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".tmp";
		File tempFile = File.createTempFile("winrm_", suffix);
		Files.write(tempFile.toPath(), fileData);

		log.debug("✅ File downloaded from {}: {} bytes", path, fileData.length);

		return tempFile;
	}

	// ✅ Account/Demat/CIF related keywords for context filtering
	private static final Set<String> MICR_CONTEXT_KEYWORDS = Set.of("micr", "cheque", "branch code", "bank code");

    private static final Set<String> ACCOUNT_CONTEXT_KEYWORDS = Set.of(
            "account", "acct", "a/c", "bank", "account no", "ac no", "acc no", "account number",
            "cif", "cif number", "customer information file", "demat", "dematerialized",
            "loan account", "fd account", "fixed deposit", "savings account", "current account",
            "customer id"
        );

        private static final Map<String, List<String>> DOC_KEYWORDS = Map.of(
            "aadhaar", List.of("government of india", "aadhaar", "enrolment no", "uidai"),
            "pan", List.of("permanent account number", "income tax department"),
            "bank", List.of(
                "bank", "ifsc", "micr", "branch code", "customer name",
                "loan account", "fd account", "customer id", "demat account",
                "account no", "account number", "cif number"
            ),
            "passport", List.of("republic of india", "भारत गणराज्य"),
            "driving_license", List.of("indian union driving licence", "rta", "non transport", "transport vehicle"),
            "voter_id", List.of("election commission of india", "epic no", "identity card")
        );

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

	private void scanFile(Path localFile, String remotePath, List<String> piiTypes, List<PiiScanResult> results, String ip) {
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
	        if (finalText.isBlank()) return;

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
	                String dominantType = Collections.max(keywordMatchCounts.entrySet(),
	                        Map.Entry.comparingByValue()).getKey();
	                int dominantCount = keywordMatchCounts.get(dominantType);

	                // ✅ If dominant type ≥90% → scan only that type
	                if ((dominantCount / (double) totalKeywordMatches) >= 0.9) {
	                    Pattern pattern = PiiPatterns.PII_PATTERNS.get(dominantType);
	                    if (pattern != null) {
	                        Matcher matcher = pattern.matcher(finalText);
	                        while (matcher.find()) {
	                            String rawMatch = matcher.group().trim();
	                            String key = dedupeKey(dominantType, rawMatch);
	                            if (!seen.add(key)) continue;
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
	            if (!piiTypes.isEmpty() && !piiTypes.contains(piiType)) continue;

	            Matcher matcher = entry.getValue().matcher(finalText);
	            while (matcher.find()) {
	                String rawMatch = matcher.group().trim();

	                if (piiType.equals("micr") && !hasContext(finalText, rawMatch, MICR_CONTEXT_KEYWORDS))
	                    continue;
                    if ((piiType.equals("account_number") || piiType.equals("demat_account") ||
                            piiType.equals("cif_number") || piiType.equals("loan_account") ||
                            piiType.equals("fd_account") || piiType.equals("customer_id"))
                           && !hasContext(finalText, rawMatch, ACCOUNT_CONTEXT_KEYWORDS))
                           continue;

	                Predicate<String> validator = PiiPatterns.PII_VALIDATORS.get(piiType);
	                if (validator != null && !validator.test(rawMatch))
	                    continue;

	                String key = dedupeKey(piiType, rawMatch);
	                if (!seen.add(key)) continue;

	                results.add(new PiiScanResult(remotePath, piiType, rawMatch, ip));
	            }
	        }

	    } catch (Exception e) {
	        log.error("❌ scanFile error: " + localFile + ": " + e.getMessage());
	    }
	}

	private String getExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot > 0 && lastDot < filename.length() - 1) {
			return filename.substring(lastDot + 1).toLowerCase();
		}
		return "";
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
			BufferedImage image = safeReadImage(imageFile); // <-- This is a new method below
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
					BufferedImage pageImage = pdfRenderer.renderImageWithDPI(page, 300);

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

	private boolean isImageExt(String ext) {
		return IMAGE_EXTS.contains(ext);
	}

	private static final Set<Character> ILLEGAL_CHARS_WINDOWS = Set.of('*', '?', '"', '<', '>', '|');

	private boolean hasIllegalChars(String path) {
		for (char c : path.toCharArray()) {
			if (ILLEGAL_CHARS_WINDOWS.contains(c)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasDotFolder(String path) {

		String normalized = path.replace('\\', '/');
		String[] folders = normalized.split("/");

		for (String folder : folders) {
			if (folder.startsWith(".")) {
				return true;
			}
		}
		return false;
	}

	private boolean isExcluded(String path, Set<String> excludes) {
		String lowerPath = path.toLowerCase();

		if (hasIllegalChars(path)) {
			log.info("Excluded because of illegal chars: {}", path);
			return true;
		}
		if (hasDotFolder(path)) {
			log.info("Excluded because of dot folder: {}", path);
			return true;
		}

		for (String exclude : excludes) {
			String ex = exclude.toLowerCase();

			// Exact match or contains
			if (lowerPath.contains(ex)) {
				log.info("Excluded by pattern match '{}': {}", exclude, path);
				return true;
			}

			// Special: Exclude versioned STS folders like sts-4.17.2.RELEASE
			if (ex.equals("sts") && lowerPath.matches(".*sts[-._\\d]+release.*")) {
				log.info("Excluded versioned STS folder: {}", path);
				return true;
			}
		}
		return false;
	}

	private void waitForFileReady(File file, int maxWaitSeconds) {
		int waited = 0;
		while (waited < maxWaitSeconds) {
			try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
				// Successfully opened for read/write => file is ready
				return;
			} catch (IOException e) {
				try {
					Thread.sleep(1000); // wait 1 sec
					waited++;
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		log.warn("Waited {} seconds but file might still be locked: {}", maxWaitSeconds, file.getAbsolutePath());
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
