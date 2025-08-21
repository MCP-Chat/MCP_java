package mcp_aws.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mcp_aws.config.McpProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

@Component
public class McpStdioClient {
	private final McpProperties properties;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Object awsLock = new Object();
	private final Object docLock = new Object();
	private ServerSession awsSession;
	private ServerSession docSession;

	public McpStdioClient(McpProperties properties) {
		this.properties = properties;
	}

	public void ensureAwsApiStarted(Map<String, String> extraEnv) throws IOException {
		synchronized (awsLock) {
			if (awsSession == null || !awsSession.isAlive()) {
				awsSession = startServer(properties.getAwsApiArgs(), extraEnv);
			}
		}
	}

	public void ensureDocumentationStarted(Map<String, String> extraEnv) throws IOException {
		synchronized (docLock) {
			if (docSession == null || !docSession.isAlive()) {
				docSession = startServer(properties.getDocumentationArgs(), extraEnv);
			}
		}
	}

	public void stopAll() {
		stopSession(awsSession);
		stopSession(docSession);
	}

	public String sendRequest(boolean documentation, Map<String, Object> request, long timeoutMs) throws IOException {
		ServerSession session = documentation ? docSession : awsSession;
		if (session == null || !session.isAlive()) {
			throw new IllegalStateException("MCP 서버 세션이 시작되지 않았습니다.");
		}
		String id = session.nextId();
		request.put("jsonrpc", "2.0");
		request.put("id", id);
		CompletableFuture<String> future = new CompletableFuture<>();
		session.pending.put(id, future);

		String json = toJson(request);
		writeFramed(session, json);
		try {
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			session.pending.remove(id);
			throw new IOException("MCP 요청 타임아웃");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("요청 대기 중 인터럽트", e);
		} catch (ExecutionException e) {
			throw new IOException("요청 처리 실패: " + e.getCause(), e);
		}
	}

	// --- internals ---
	private ServerSession startServer(List<String> args, Map<String, String> extraEnv) throws IOException {
		if (args == null || args.isEmpty()) throw new IllegalArgumentException("MCP 서버 실행 인자가 비어있습니다.");
		List<String> command = new ArrayList<>();
		command.add(properties.getCommand());
		command.addAll(args);

		ProcessBuilder pb = new ProcessBuilder(command);
		Map<String, String> env = pb.environment();
		if (properties.getRegion() != null) env.putIfAbsent("AWS_REGION", properties.getRegion());
		if (extraEnv != null) env.putAll(extraEnv);
		Process process = pb.start();

		ServerSession session = new ServerSession(process);
		session.startReader();
		session.startErrorLogger();

		// initialize
		Map<String, Object> init = new HashMap<>();
		init.put("method", "initialize");
		Map<String, Object> params = new HashMap<>();
		params.put("protocolVersion", "2024-11-05");
		params.put("capabilities", Map.of());
		params.put("clientInfo", Map.of("name", "mcp-java", "version", "0.1.0"));
		init.put("params", params);
		try {
			String resp = sendRequestInternal(session, init, defaultValue(properties.getStartTimeoutMs(), 15000));
			// send initialized notification (no id)
			Map<String, Object> initialized = new HashMap<>();
			initialized.put("jsonrpc", "2.0");
			initialized.put("method", "initialized");
			initialized.put("params", Map.of());
			writeFramed(session, toJson(initialized));
			// small delay to allow server to finish setup
			try { Thread.sleep(200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
		} catch (IOException e) {
			stopSession(session);
			throw e;
		}
		return session;
	}

	private void stopSession(ServerSession session) {
		if (session == null) return;
		session.stop();
	}

	private String sendRequestInternal(ServerSession session, Map<String, Object> request, long timeoutMs) throws IOException {
		String id = session.nextId();
		request.put("jsonrpc", "2.0");
		request.put("id", id);
		CompletableFuture<String> future = new CompletableFuture<>();
		session.pending.put(id, future);
		writeFramed(session, toJson(request));
		try {
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			session.pending.remove(id);
			throw new IOException("MCP 요청 타임아웃");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("요청 대기 중 인터럽트", e);
		} catch (ExecutionException e) {
			throw new IOException("요청 처리 실패: " + e.getCause(), e);
		}
	}

	private void writeFramed(ServerSession session, String json) throws IOException {
		byte[] payload = json.getBytes(StandardCharsets.UTF_8);
		String headers = "Content-Length: " + payload.length + "\r\n" +
				"Content-Type: application/json; charset=utf-8\r\n\r\n";
		synchronized (session.writeLock) {
			session.out.write(headers.getBytes(StandardCharsets.UTF_8));
			session.out.write(payload);
			session.out.flush();
		}
	}

	private String toJson(Map<String, Object> req) throws IOException {
		try {
			return objectMapper.writeValueAsString(req);
		} catch (Exception e) {
			throw new IOException("JSON 직렬화 실패", e);
		}
	}

	private long defaultValue(Integer value, long def) { return value == null ? def : value; }

	private final class ServerSession {
		private final Process process;
		private final InputStream in;
		private final InputStream err;
		private final OutputStream out;
		private final ConcurrentHashMap<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();
		private final java.util.concurrent.atomic.AtomicInteger idGen = new java.util.concurrent.atomic.AtomicInteger(0);
		private volatile boolean running = true;
		private Thread reader;
		private Thread errReader;
		private final Object writeLock = new Object();

		ServerSession(Process process) {
			this.process = process;
			this.in = process.getInputStream();
			this.err = process.getErrorStream();
			this.out = process.getOutputStream();
		}

		boolean isAlive() { return process.isAlive() && running; }
		String nextId() { return String.valueOf(idGen.incrementAndGet()); }

		void startReader() {
			reader = new Thread(() -> {
				try (BufferedInputStream bis = new BufferedInputStream(in)) {
					while (running) {
						int contentLength = readContentLength(bis);
						if (contentLength <= 0) continue;
						byte[] buf = bis.readNBytes(contentLength);
						String json = new String(buf, StandardCharsets.UTF_8);
						try {
							JsonNode node = objectMapper.readTree(json);
							JsonNode idNode = node.get("id");
							if (idNode != null) {
								String key = idNode.isInt() ? String.valueOf(idNode.intValue()) : idNode.asText();
								CompletableFuture<String> fut = pending.remove(key);
								if (fut != null) fut.complete(json);
							}
						} catch (Exception ignore) {}
					}
				} catch (IOException ignore) {
				} finally {
					running = false;
					pending.forEach((k, f) -> f.completeExceptionally(new IOException("세션 종료")));
					pending.clear();
				}
			}, "mcp-stdio-reader");
			reader.setDaemon(true);
			reader.start();
		}

		void startErrorLogger() {
			errReader = new Thread(() -> {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(err, StandardCharsets.UTF_8))) {
					String line;
					while ((line = br.readLine()) != null) {
						System.err.println("[MCP-STDERR] " + line);
					}
				} catch (IOException ignore) {}
			}, "mcp-stdio-stderr");
			errReader.setDaemon(true);
			errReader.start();
		}

		int readContentLength(BufferedInputStream bis) throws IOException {
			int contentLength = -1;
			String line;
			while (!(line = readLine(bis)).isEmpty()) {
				String lower = line.toLowerCase(Locale.ROOT);
				if (lower.startsWith("content-length:")) {
					String val = line.substring("Content-Length:".length()).trim();
					try { contentLength = Integer.parseInt(val); } catch (NumberFormatException ignore) {}
				}
			}
			return contentLength;
		}

		String readLine(BufferedInputStream bis) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (true) {
				int b = bis.read();
				if (b == -1) return baos.toString(StandardCharsets.UTF_8);
				if (b == '\n') break;
				if (b == '\r') { /* ignore */ } else { baos.write(b); }
			}
			return baos.toString(StandardCharsets.UTF_8);
		}

		void stop() {
			running = false;
			try { if (out != null) out.flush(); } catch (IOException ignore) {}
			process.destroy();
			try { process.waitFor(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
			if (process.isAlive()) process.destroyForcibly();
		}
	}
} 