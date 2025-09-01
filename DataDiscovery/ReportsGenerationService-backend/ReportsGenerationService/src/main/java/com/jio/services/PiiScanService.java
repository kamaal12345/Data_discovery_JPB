package com.jio.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.jio.convertor.PiiScanRequestConvertor;
import com.jio.customexception.BusinessException;
import com.jio.dto.PiiScanRequestDto;
import com.jio.dto.RemotePiiScanRequestDto;
import com.jio.entity.PiiScanRequest;
import com.jio.entity.PiiScanResult;
import com.jio.repository.PiiScanRequestRepository;
import com.jio.utils.ExcelExportUtil;
import com.jio.utils.PathBatcher;
import com.jio.utils.PiiPatterns;
import com.jio.utils.SSHConnector;

import jakarta.transaction.Transactional;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class PiiScanService {

	@Autowired
	private PiiScanRequestRepository piiScanRequestRepository;

	@Autowired
	private SSHConnector sshConnector;

	private final int BATCH_SIZE = 50;

	private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "bmp", "tiff", "gif", "webp");
	private static final Set<String> TEXT_EXTS = Set.of("txt", "log", "csv", "md");
	private static final String PDF_EXT = "pdf";

	private static final Set<String> ALLOWED_EXTS;
	static {
		Set<String> s = new HashSet<>();
		s.addAll(IMAGE_EXTS);
		s.addAll(TEXT_EXTS);
		s.add(PDF_EXT);
		ALLOWED_EXTS = Collections.unmodifiableSet(s);
	}

	private static final Set<String> defaultSystemDirs = new HashSet<>(Arrays.asList("$Recycle.Bin", "Program Files",
			"Program Files (x86)", "Python313", "ProgramData", "AppData", "Windows", "Documents and Settings",
			"node_modules", ".angular", "src", ".metadata", "target", "main"));

	@Transactional
	public PiiScanRequestDto scanFiles(PiiScanRequestDto requestDto) throws IOException {
		List<PiiScanResult> results = new ArrayList<>();

		Long maxFileSizeMb = requestDto.getMaxFileSize();
		long maxFileSizeInBytes = (maxFileSizeMb != null && maxFileSizeMb > 0) ? maxFileSizeMb * 1024L * 1024L
				: 10 * 1024L * 1024L;

		final Set<String> excludePatterns = new HashSet<>();
		if (requestDto.getExcludePatterns() != null && !requestDto.getExcludePatterns().isEmpty()) {
			excludePatterns.addAll(Arrays.stream(requestDto.getExcludePatterns().split(",")).map(String::trim)
					.collect(Collectors.toSet()));
		} else {
			excludePatterns.addAll(defaultSystemDirs);
		}

		Path rootPath = Paths.get(requestDto.getFilePath());

		try {
			Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					try {
						String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";

						if (Files.isSymbolicLink(dir)) {
							System.out.println("Skipping symbolic link: " + dir);
							return FileVisitResult.SKIP_SUBTREE;
						}

						if (!Files.isReadable(dir)) {
							System.out.println("Directory not readable: " + dir);
							return FileVisitResult.SKIP_SUBTREE;
						}

						if (defaultSystemDirs.contains(dirName)) {
							System.out.println("Skipping system directory: " + dir);
							return FileVisitResult.SKIP_SUBTREE;
						}

						if (matchesAnyPattern(dir.toString(), excludePatterns)) {
							System.out.println("Skipping user-excluded directory: " + dir);
							return FileVisitResult.SKIP_SUBTREE;
						}

					} catch (Exception e) {
						System.out.println("Skipping directory due to error: " + dir + " - " + e.getMessage());
						return FileVisitResult.SKIP_SUBTREE;
					}

					return FileVisitResult.CONTINUE;
				}

//				@Override
//				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
//					try {
//						String filePath = file.toString();
//
//						if (matchesAnyPattern(filePath, excludePatterns)) {
//							return FileVisitResult.CONTINUE;
//						}
//
//						if (!isFileWithinSizeLimit(file, maxFileSizeInBytes)) {
//							return FileVisitResult.CONTINUE;
//						}
//
//						if (isStsReleaseFolder(file)) {
//							System.out.println("Skipping STS release folder: " + file);
//							return FileVisitResult.SKIP_SUBTREE;
//						}
//
//						if (isMediaFile(file) || isSkippableFile(file)) {
//							return FileVisitResult.CONTINUE;
//						}
//
//						scanFile(file, requestDto.getPiiTypes(), results);
//
//					} catch (Exception e) {
//						System.err.println("Error reading file: " + file + " - " + e.getMessage());
//					}
//
//					return FileVisitResult.CONTINUE;
//				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String fileName = file.toString();
//	                System.out.println("Checking file: " + fileName);

					String lower = fileName.toLowerCase();
					if (matchesAnyPattern(lower, excludePatterns) || !isFileWithinSizeLimit(file, maxFileSizeInBytes)
							|| isStsReleaseFolder(file) || isSkippableFile(file)) {
						System.out.println(" -> Skipped (pattern/size/skippable)");
						return FileVisitResult.CONTINUE;
					}

					if (lower.endsWith(".pdf")) {
						System.out.println(" -> Visiting as PDF");
						scanFile(file, requestDto.getPiiTypes(), results);
					} else if (isImage(file)) {
						System.out.println(" -> Visiting as IMAGE");
						scanFile(file, requestDto.getPiiTypes(), results);
					} else if (!isVideoFile(file)) {
						System.out.println(" -> Visiting as TEXT");
						scanFile(file, requestDto.getPiiTypes(), results);
					} else {
						System.out.println(" -> Skipping video/media file");
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					if (exc instanceof AccessDeniedException) {
						System.out.println("Access denied to: " + file);
						return FileVisitResult.SKIP_SUBTREE;
					}
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			throw new RuntimeException("Error during scan: " + requestDto.getFilePath(), e);
		}

		PiiScanRequest requestEntity = PiiScanRequestConvertor.convertToPiiScanRequestEntity(requestDto);
		requestEntity.setPiiScanResults(results);
		results.forEach(result -> result.setPiiScanRequest(requestEntity));
		PiiScanRequest savedRequest = piiScanRequestRepository.save(requestEntity);
		return PiiScanRequestConvertor.convertToPiiScanRequestDTO(savedRequest);
	}

	private static boolean isFileWithinSizeLimit(Path file, long maxSize) throws IOException {
		return Files.size(file) <= maxSize;
	}

	private static boolean isVideoFile(Path file) {
		String fileName = file.toString().toLowerCase();
		return fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".avi")
				|| fileName.endsWith(".mov") || fileName.endsWith(".wmv") || fileName.endsWith(".flv")
				|| fileName.endsWith(".webm");
	}

	private static boolean isSkippableFile(Path file) {
		String name = file.toString().toLowerCase();

		return name.endsWith(".zip") || name.endsWith(".xml") || name.endsWith(".info") || name.endsWith(".rar")
				|| name.endsWith(".7z") || name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".css")
				|| name.endsWith(".cjs") || name.endsWith(".pom") || name.endsWith("open-source-licenses.txt")
				|| name.endsWith("manifest.mf") || name.contains(".mappings") || name.endsWith(".prefs")
				|| name.endsWith(".exe");
	}

	private String extractTextFromPdf(Path file) {
		try (PDDocument document = PDDocument.load(file.toFile())) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		} catch (IOException e) {
			System.err.println("PDF read error: " + file + " - " + e.getMessage());
			return null;
		}
	}

//	private String extractTextFromImage(Path file) {
//		try {
//			Tesseract tesseract = new Tesseract();
//			tesseract.setDatapath("C:/Users/InfoSec/AppData/Local/Programs/Tesseract-OCR/tessdata");
//			return tesseract.doOCR(file.toFile());
//		} catch (TesseractException e) {
//			System.err.println("OCR error: " + file + " - " + e.getMessage());
//			return null;
//		}
//	}

	private String extractTextFromImage(Path file) {
		try {
			Tesseract tesseract = new Tesseract();

			tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
			tesseract.setLanguage("eng");

			return tesseract.doOCR(file.toFile());
		} catch (TesseractException e) {
			System.err.println("OCR error: " + file + " - " + e.getMessage());
			return null;
		}
	}

	private static boolean isStsReleaseFolder(Path dir) {
		String folderName = dir.getFileName().toString().toLowerCase();
		return folderName.matches("sts-\\d+\\.\\d+\\.\\d+(\\.\\d+)?\\.release");
	}

	private static boolean matchesAnyPattern(String name, Set<String> patterns) {
		String lower = name.toLowerCase();
		for (String pattern : patterns) {
			if (lower.equals(pattern.trim().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private static final Set<String> SUPPORTED_IMAGE_FORMATS = Set.of(".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff");

	private static boolean isImageFile(Path file) {
		String name = file.getFileName().toString().toLowerCase();
		return SUPPORTED_IMAGE_FORMATS.stream().anyMatch(name::endsWith);
	}

	private static boolean isImageByMimeType(Path file) {
		try {
			String mimeType = Files.probeContentType(file);
			return mimeType != null && mimeType.startsWith("image");
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean isImage(Path file) {
		return isImageFile(file) || isImageByMimeType(file);
	}

	private void scanFile(Path file, List<String> piiTypes, List<PiiScanResult> results) {
		try {
			String content = null;
			String fileName = file.toString().toLowerCase();

			// Extract content
			if (fileName.endsWith(".pdf")) {
				content = extractTextFromPdf(file);
//	            System.out.println("[PDF] Extracted content length: " + (content == null ? 0 : content.length()));
			} else if (isImage(file)) {
				content = extractTextFromImage(file);
//	            System.out.println("[Image] OCR content length: " + (content == null ? 0 : content.length()));
			} else {
				content = String.join("\n", Files.readAllLines(file, StandardCharsets.UTF_8));
//	            System.out.println("[Text] Extracted content length: " + (content == null ? 0 : content.length()));
			}

			// If content was successfully extracted
			if (content != null && !content.isEmpty()) {
//	            System.out.println("Preview: " + content.substring(0, Math.min(200, content.length())));

				for (Map.Entry<String, Pattern> entry : PiiPatterns.PII_PATTERNS.entrySet()) {
					String type = entry.getKey();

					// If type is allowed
					if (piiTypes == null || piiTypes.isEmpty() || piiTypes.contains("all") || piiTypes.contains(type)) {
						Matcher matcher = entry.getValue().matcher(content);

						while (matcher.find()) {
							PiiScanResult result = new PiiScanResult();
							result.setFilePath(file.toString());
							result.setPiiType(type);
							result.setMatchedData(matcher.group());
							results.add(result);

							System.out.println("Detected [" + type + "]: " + matcher.group() + " in file: " + file);
						}
					}
				}
			} else {
				System.out.println("No content found in: " + file);
			}

		} catch (Exception e) {
			System.err.println("scanFile error for " + file + ": " + e.getMessage());
		}
	}

	// Method to scan a file for PII data

//	private void scanFile(Path file, List<String> piiTypes, List<PiiScanResult> results) {
//		try {
//			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
//
//			for (String line : lines) {
//				for (Map.Entry<String, Pattern> entry : PiiPatterns.PII_PATTERNS.entrySet()) {
//					String type = entry.getKey();
//
//					if (piiTypes == null || piiTypes.isEmpty() || piiTypes.contains("all") || piiTypes.contains(type)) {
//						Matcher matcher = entry.getValue().matcher(line);
//
//						while (matcher.find()) {
//							String matchedPart = matcher.group();
//
//							PiiScanResult scanResult = new PiiScanResult();
//							scanResult.setFilePath(file.toString());
//							scanResult.setPiiType(type);
//							scanResult.setMatchedData(matchedPart);
//							results.add(scanResult);
//						}
//					}
//				}
//			}
//		} catch (IOException e) {
//			System.err.println("Error reading file: " + file + " - " + e.getMessage());
//		}
//	}

	public void exportPiiResults(Long requestId, OutputStream out) throws Exception {
		PiiScanRequest request = piiScanRequestRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid requestId"));

		List<PiiScanResult> results = request.getPiiScanResults();
		ExcelExportUtil.exportPiiResultsToExcel(results, out);
	}

	public Page<PiiScanRequestDto> getAllScanRequests(Integer offset, Integer pageSize, String field, Integer sort,
			String searchText) {
		Sort.Direction direction = (sort != null && sort == 1) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = StringUtils.hasText(field) ? PageRequest.of(offset, pageSize, Sort.by(direction, field))
				: PageRequest.of(offset, pageSize, Sort.by(direction, "createdDate"));

		Page<PiiScanRequest> page;

		if (StringUtils.hasText(searchText)) {
			page = piiScanRequestRepository.findByFilePathContainingIgnoreCase(searchText, pageable);
		} else {
			page = piiScanRequestRepository.findAll(pageable);
		}

		return PiiScanRequestConvertor.convertToPiiScanRequestDTOPage(page, pageable);
	}

//	public PiiScanRequestDto scanViaSshOnce(RemotePiiScanRequestDto requestDto) throws Exception {
//		SshConnectionDto conn = requestDto.getConnection();
//
//		Session session = null;
//		ChannelSftp sftp = null;
//
//		try {
//			session = sshConnector.connect(conn.getHost(), conn.getPort(), conn.getUsername(), conn.getPassword());
//			sftp = sshConnector.getSftpChannel();
//
//			return scanRemoteUsingSession(requestDto, sftp);
//
//		} finally {
//			if (sftp != null && sftp.isConnected())
//				sftp.disconnect();
//			if (session != null && session.isConnected())
//				session.disconnect();
//		}
//	}
//
//	public PiiScanRequestDto scanRemoteUsingSession(RemotePiiScanRequestDto requestDto, ChannelSftp sftp)
//			throws Exception {
//		List<PiiScanResult> results = new ArrayList<>();
//
//		long maxFileSizeInBytes = (requestDto.getMaxFileSize() != null && requestDto.getMaxFileSize() > 0)
//				? requestDto.getMaxFileSize() * 1024L * 1024L
//				: 10 * 1024L * 1024L;
//
//		Set<String> excludePatterns = new HashSet<>();
//		if (requestDto.getExcludePatterns() != null && !requestDto.getExcludePatterns().isEmpty()) {
//			excludePatterns.addAll(Arrays.stream(requestDto.getExcludePatterns().split(",")).map(String::trim)
//					.collect(Collectors.toSet()));
//		} else {
//			excludePatterns.addAll(defaultSystemDirs);
//		}
//
//		scanDirectory(sftp, requestDto.getFilePath(), requestDto.getPiiTypes(), maxFileSizeInBytes, excludePatterns,
//				results);
//
//		PiiScanRequest entity = PiiScanRequestConvertor.convertToPiiScanRequestEntity(requestDto);
//		entity.setPiiScanResults(results);
//		for (PiiScanResult result : results) {
//			result.setPiiScanRequest(entity);
//		}
//
//		PiiScanRequest saved = piiScanRequestRepository.save(entity);
//		return PiiScanRequestConvertor.convertToPiiScanRequestDTO(saved);
//	}
//
//	private void scanDirectory(ChannelSftp sftp, String remotePath, String piiTypes, long maxFileSize,
//			Set<String> excludePatterns, List<PiiScanResult> results) throws IOException {
//
//		System.out.println("🔍 Scanning directory: " + remotePath);
//
//		try {
//			SftpATTRS attrs = sftp.lstat(remotePath);
//			if (!attrs.isDir()) {
//				throw new BusinessException("INVALID_DIRECTORY", "Path is not a directory: " + remotePath);
//			}
//		} catch (SftpException e) {
//			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
//				throw new BusinessException("DIRECTORY_NOT_FOUND", "Remote path not found: " + remotePath);
//			}
//			throw new BusinessException("SFTP_ERROR", "Failed to access remote path: " + remotePath);
//		}
//
//		Vector<ChannelSftp.LsEntry> entries;
//		try {
//			entries = sftp.ls(remotePath);
//		} catch (SftpException e) {
//			String msg = "Cannot list remote path: " + remotePath;
//			System.err.println("❌ " + msg + " - " + e.getMessage());
//			throw new BusinessException("REMOTE_PATH_NOT_FOUND", msg);
//		}
//
//		System.out.println("📁 Found " + entries.size() + " entries in: " + remotePath);
//
//		for (ChannelSftp.LsEntry entry : entries) {
//			String filename = entry.getFilename();
//			String fullPath = remotePath + "/" + filename;
//
//			if (filename.equals(".") || filename.equals(".."))
//				continue;
//
//			System.out.println("➡️ Checking entry: " + fullPath);
//
//			if (entry.getAttrs().isDir()) {
//				if (excludePatterns.stream().anyMatch(fullPath::contains)) {
//					System.out.println("⛔ Skipping excluded directory: " + fullPath);
//					continue;
//				}
//				scanDirectory(sftp, fullPath, piiTypes, maxFileSize, excludePatterns, results);
//			} else {
//				long fileSize = entry.getAttrs().getSize();
//				if (fileSize > maxFileSize) {
//					System.out.println("⛔ Skipping large file (" + fileSize + " bytes): " + fullPath);
//					continue;
//				}
//
//				if (isSkippableFile(filename)) {
//					System.out.println("⛔ Skipping skippable file: " + fullPath);
//					continue;
//				}
//
//				try (InputStream is = sftp.get(fullPath)) {
//					System.out.println("📄 Reading file: " + fullPath);
//					scanFileFromStream(fullPath, is, piiTypes, results);
//				} catch (Exception e) {
//					System.out.println("❌ Error reading file: " + fullPath + ", Error: " + e.getMessage());
//				}
//			}
//		}
//	}
//
//	private void scanFileFromStream(String filePath, InputStream input, String types, List<PiiScanResult> results)
//			throws IOException {
//
//		if (types == null || types.trim().isEmpty())
//			types = "all";
//
//		System.out.println("🔍 Scanning file: " + filePath);
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
//			String line;
//			while ((line = reader.readLine()) != null) {
//				for (Map.Entry<String, Pattern> entry : PII_PATTERNS.entrySet()) {
//					String type = entry.getKey();
//
//					if ("all".equalsIgnoreCase(types) || types.contains(type)) {
//						Matcher matcher = entry.getValue().matcher(line);
//						while (matcher.find()) {
//							PiiScanResult result = new PiiScanResult();
//							result.setFilePath(filePath);
//							result.setPiiType(type);
//							result.setMatchedData(matcher.group());
//							results.add(result);
//
//							System.out.println("✅ Found match: " + matcher.group() + " (" + type + ")");
//						}
//					}
//				}
//			}
//
//		}
//	}

	// Remote Server Scanning Logic to Scan pii Imformation multi tthreding code

//	public PiiScanRequestDto scanViaSshOnce(RemotePiiScanRequestDto dto) throws Exception {
//	    List<PiiScanResult> results = Collections.synchronizedList(new ArrayList<>());
//
//	    for (String host : dto.getConnection().getHost()) {
//	        Session session = sshConnector.connect(host, dto.getConnection().getPort(),
//	                dto.getConnection().getUsername(), dto.getConnection().getPassword());
//
//	        try {
//	            List<PiiScanResult> ipResults = scanRemoteSession(dto, session, host); // Pass host/IP
//	            results.addAll(ipResults);
//	        } finally {
//	            sshConnector.disconnect(session);
//	        }
//	    }
//
//	    PiiScanRequest saved = saveResults(dto, results);
//	    return PiiScanRequestConvertor.convertToPiiScanRequestDTO(saved);
//	}
//
//
//	public PiiScanRequest saveResults(RemotePiiScanRequestDto dto, List<PiiScanResult> results) {
//		PiiScanRequest request = new PiiScanRequest();
//		request.setTargetName(dto.getTargetName());
//		request.setFilePath(dto.getFilePath());
//		request.setMaxFileSize(dto.getMaxFileSize());
//		request.setPiiTypes(dto.getPiiTypes());
//		request.setExcludePatterns(dto.getExcludePatterns());
//		request.setStopScannAfter(dto.getStopScannAfter());
//
//		for (PiiScanResult result : results) {
//			result.setPiiScanRequest(request);
//		}
//		request.setPiiScanResults(results);
//		return piiScanRequestRepository.save(request);
//	}
//
//	private Set<String> parseExcludes(String excludePatterns) {
//		if (excludePatterns == null || excludePatterns.trim().isEmpty()) {
//			return Collections.emptySet();
//		}
//
//		return Arrays.stream(excludePatterns.split(",")).map(String::trim).filter(s -> !s.isEmpty())
//				.collect(Collectors.toSet());
//	}
//
//
//	private List<PiiScanResult> scanRemoteSession(RemotePiiScanRequestDto dto, Session session, String ip) throws Exception {
//	    long maxSizeBytes = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
//	    Set<String> excludes = parseExcludes(dto.getExcludePatterns());
//	    PathBatcher batches = new PathBatcher(session, dto.getFilePath(), maxSizeBytes, excludes, BATCH_SIZE);
//
//	    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//	    List<PiiScanResult> results = Collections.synchronizedList(new ArrayList<>());
//
//	    String typesCsv = String.join(",", dto.getPiiTypes());
//
//	    for (List<String> files : batches) {
//	        pool.submit(() -> {
//	            files.forEach(path -> processFile(path, session, typesCsv, results, ip));
//	        });
//	    }
//
//	    pool.shutdown();
//	    pool.awaitTermination(30, TimeUnit.MINUTES);
//
//	    if (results.isEmpty()) {
//	        throw new BusinessException("INVALID_PATH", "No valid files found or invalid path provided.");
//	    }
//
//	    return results;
//	}
//
//
//	private String escape(String input) {
//		if (input == null)
//			return "";
//		return "'" + input.replace("'", "'\\''") + "'";
//	}
//
//
//
//	private void processFile(String path, Session session, String typesCSV, List<PiiScanResult> out, String ip) {
//	    try {
//	        List<String> lines = runCommand(session, "cat " + escape(path));
//	        scanLines(path, lines, typesCSV, out, ip); // Pass IP
//	    } catch (Exception e) {
//	        System.err.println("Error processing file: " + path);
//	        e.printStackTrace();
//	    }
//	}
//
//	private List<String> runCommand(Session session, String cmd) throws Exception {
//		ChannelExec ch = (ChannelExec) session.openChannel("exec");
//		ch.setCommand(cmd);
//		ch.setInputStream(null);
//		InputStream stdout = ch.getInputStream();
//		ch.connect();
//
//		List<String> lines = new ArrayList<>();
//		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stdout))) {
//			String l;
//			while ((l = rdr.readLine()) != null)
//				lines.add(l);
//		} finally {
//			ch.disconnect();
//		}
//		return lines;
//	}
//	
//	private void scanLines(String path, List<String> lines, String typesCSV, List<PiiScanResult> results, String ip) {
//	    Set<String> types = Optional.ofNullable(typesCSV).map(t -> Set.of(t.split(","))).orElse(Set.of());
//
//	    for (String line : lines) {
//	        for (Map.Entry<String, Pattern> entry : PiiPatterns.PII_PATTERNS.entrySet()) {
//	            String piiType = entry.getKey();
//	            Pattern pattern = entry.getValue();
//
//	            if (types.isEmpty() || types.contains(piiType)) {
//	                Matcher matcher = pattern.matcher(line);
//	                while (matcher.find()) {
//	                    PiiScanResult r = new PiiScanResult(path, List.of(piiType), matcher.group(), ip);
//	                    results.add(r);
//	                }
//	            }
//	        }
//	    }
//	}

	// previous code

//	public PiiScanRequestDto scanViaSshOnce(RemotePiiScanRequestDto dto) throws Exception {
//	Session session = sshConnector.connect(dto.getConnection().getHost(), dto.getConnection().getPort(),
//			dto.getConnection().getUsername(), dto.getConnection().getPassword());
//	try {
//		List<PiiScanResult> results = scanRemoteSession(dto, session);
//		PiiScanRequest saved = saveResults(dto, results);
//		return PiiScanRequestConvertor.convertToPiiScanRequestDTO(saved);
//	} finally {
//		sshConnector.disconnect(session);
//	}
//}

//	private List<PiiScanResult> scanRemoteSession(RemotePiiScanRequestDto dto, Session session) throws Exception {
//	long maxSizeBytes = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
//	Set<String> excludes = parseExcludes(dto.getExcludePatterns());
//	PathBatcher batches = new PathBatcher(session, dto.getFilePath(), maxSizeBytes, excludes, BATCH_SIZE);
//
//	ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//	List<PiiScanResult> results = Collections.synchronizedList(new ArrayList<>());
//
//	for (List<String> files : batches) {
//		pool.submit(() -> {
//			files.forEach(path -> processFile(path, session, dto.getPiiTypes(), results));
//		});
//	}
//
//	pool.shutdown();
//	pool.awaitTermination(30, TimeUnit.MINUTES);
//
//	if (results.isEmpty()) {
//		throw new BusinessException("INVALID_PATH", "No valid files found or invalid path provided.");
//	}
//
//	return results;
//}

//	private void processFile(String path, Session session, String typesCSV, List<PiiScanResult> out) {
//	try {
//		List<String> lines = runCommand(session, "cat " + escape(path));
//		scanLines(path, lines, typesCSV, out);
//	} catch (Exception ignored) {
//	}
//}

//	private void scanLines(String path, List<String> lines, String typesCSV, List<PiiScanResult> results) {
//	Set<String> types = Optional.ofNullable(typesCSV).map(t -> Set.of(t.split(","))).orElse(Set.of());
//
//	for (String line : lines) {
//		for (Map.Entry<String, Pattern> entry : PiiPatterns.PII_PATTERNS.entrySet()) {
//			String piiType = entry.getKey();
//			Pattern pattern = entry.getValue();
//
//			if (types.isEmpty() || types.contains(piiType)) {
//				Matcher matcher = pattern.matcher(line);
//				while (matcher.find()) {
//					PiiScanResult r = new PiiScanResult(path, piiType, matcher.group());
//					results.add(r);
//				}
//			}
//		}
//	}
//}

	// with this code we can scan these file only .txt, .csv, .log, .json

	public PiiScanRequestDto scanViaSshOnce(RemotePiiScanRequestDto dto) throws Exception {
		List<PiiScanResult> results = new ArrayList<>();
		int stopAfter = dto.getStopScannAfter() != null ? dto.getStopScannAfter() : Integer.MAX_VALUE;

		for (String host : dto.getConnection().getHost()) {
			Session session = sshConnector.connect(host, dto.getConnection().getPort(),
					dto.getConnection().getUsername(), dto.getConnection().getPassword());

			try {
				int[] counter = new int[] { 0 };
				scanRemoteSession(dto, session, host, results, counter, stopAfter);
				if (counter[0] >= stopAfter) {
					break; // Stop scanning other hosts if limit reached
				}
			} finally {
				sshConnector.disconnect(session);
			}
		}

		if (results.isEmpty()) {
			throw new BusinessException("INVALID_PATH", "No valid files found or invalid path provided.");
		}

		PiiScanRequest saved = saveResults(dto, results);
		return PiiScanRequestConvertor.convertToPiiScanRequestDTO(saved);
	}

	public PiiScanRequest saveResults(RemotePiiScanRequestDto dto, List<PiiScanResult> results) {
		PiiScanRequest request = new PiiScanRequest();
		request.setTargetName(dto.getTargetName());
		request.setFilePath(dto.getFilePath());
		request.setMaxFileSize(dto.getMaxFileSize());
		request.setPiiTypes(dto.getPiiTypes());
		request.setExcludePatterns(dto.getExcludePatterns());
		request.setStopScannAfter(dto.getStopScannAfter());

		for (PiiScanResult result : results) {
			result.setPiiScanRequest(request);
		}
		request.setPiiScanResults(results);
		return piiScanRequestRepository.save(request);
	}

	private String escape(String input) {
		if (input == null)
			return "";
		return "'" + input.replace("'", "'\\''") + "'";
	}

	private Set<String> parseExcludes(String excludePatterns) {
		if (excludePatterns == null || excludePatterns.trim().isEmpty()) {
			return Collections.emptySet();
		}
		return Arrays.stream(excludePatterns.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	private boolean isExcluded(String path, Set<String> excludes) {
		if (excludes == null || excludes.isEmpty())
			return false;
		for (String pattern : excludes) {
			if (path.contains(pattern)) {
				return true;
			}
		}
		return false;
	}

	private List<String> runCommand(Session session, String cmd) throws Exception {
		ChannelExec ch = (ChannelExec) session.openChannel("exec");
		ch.setCommand(cmd);
		ch.setInputStream(null);
		InputStream stdout = ch.getInputStream();
		ch.connect();

		List<String> lines = new ArrayList<>();
		try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stdout))) {
			String l;
			while ((l = rdr.readLine()) != null)
				lines.add(l);
		} finally {
			ch.disconnect();
		}
		return lines;
	}

	private void scanRemoteSession(RemotePiiScanRequestDto dto, Session session, String ip, List<PiiScanResult> results,
			int[] piiCounter, int stopAfter) throws Exception {

		long maxSizeBytes = Optional.ofNullable(dto.getMaxFileSize()).orElse(10L) * 1024 * 1024;
		Set<String> excludes = parseExcludes(dto.getExcludePatterns());
		AtomicBoolean shouldStopSignal = new AtomicBoolean(false);
		PathBatcher batches = new PathBatcher(session, dto.getFilePath(), maxSizeBytes, excludes, BATCH_SIZE,
				ALLOWED_EXTS, shouldStopSignal);

		String typesCsv = String.join(",", dto.getPiiTypes());

		outer: for (List<String> files : batches) {
			for (String path : files) {
				if (piiCounter[0] >= stopAfter) {
					break outer;
				}
				if (isExcluded(path, excludes)) {
					System.out.println("Skipping excluded file: " + path);
					continue;
				}
				List<String> lines = runCommand(session, "cat " + escape(path));
				int added = scanLinesWithLimit(path, lines, typesCsv, results, ip, stopAfter - piiCounter[0]);
				piiCounter[0] += added;
				if (piiCounter[0] >= stopAfter) {
					break outer;
				}
			}
		}
	}

	private int scanLinesWithLimit(String path, List<String> lines, String typesCSV, List<PiiScanResult> results,
			String ip, int maxToAdd) {
		int added = 0;
		Set<String> types = Optional.ofNullable(typesCSV).map(t -> Set.of(t.split(","))).orElse(Set.of());

		for (String line : lines) {
			for (Map.Entry<String, Pattern> entry : PiiPatterns.PII_PATTERNS.entrySet()) {
				String piiType = entry.getKey();
				Pattern pattern = entry.getValue();

				if (types.isEmpty() || types.contains(piiType)) {
					Matcher matcher = pattern.matcher(line);
					while (matcher.find()) {
						if (added >= maxToAdd) {
							return added;
						}
						PiiScanResult result = new PiiScanResult(path, piiType, matcher.group(), ip);
						results.add(result);
					}
				}
			}
		}

		return added;
	}

//	public Page<PiiScanResultDto> getPagedScanResults(Long requestId, int page, int size) {
//		Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
//		return piiScanResultRepository.findByPiiScanRequest_RequestId(requestId, pageable)
//				.map(PiiScanResultConvertor::convertToPiiScanResultDTO);
//	}

}
