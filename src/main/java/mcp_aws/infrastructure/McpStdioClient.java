package mcp_aws.infrastructure;

import mcp_aws.config.McpProperties;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class McpStdioClient {
	private final McpProperties properties;

	public McpStdioClient(McpProperties properties) {
		this.properties = properties;
	}

	public Process startAwsApiServer(Map<String, String> extraEnv) throws IOException {
		return startServer(properties.getAwsApiArgs(), extraEnv);
	}

	public Process startDocumentationServer(Map<String, String> extraEnv) throws IOException {
		return startServer(properties.getDocumentationArgs(), extraEnv);
	}

	public void stopServer(Process process) {
		if (process == null) return;
		process.destroy();
		try {
			process.waitFor(defaultValue(properties.getShutdownTimeoutMs(), 5000), TimeUnit.MILLISECONDS);
		} catch (InterruptedException ignored) { }
		if (process.isAlive()) {
			process.destroyForcibly();
		}
	}

	public String sendJsonRpc(Process process, String requestJson, long timeoutMs) throws IOException {
		if (process == null || !process.isAlive()) {
			throw new IllegalStateException("MCP 서버 프로세스가 실행 중이 아닙니다.");
		}
		// 간단 동기 I/O (플레이스홀더). 추후 프레이밍/프로토콜 처리 필요
		try (OutputStream os = process.getOutputStream()) {
			os.write((requestJson + "\n").getBytes(StandardCharsets.UTF_8));
			os.flush();
		}
		// 단순히 표준출력의 첫 줄을 응답으로 읽음 (MVP)
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			process.getErrorStream().transferTo(OutputStream.nullOutputStream());
			return br.readLine();
		}
	}

	private Process startServer(List<String> args, Map<String, String> extraEnv) throws IOException {
		if (args == null || args.isEmpty()) {
			throw new IllegalArgumentException("MCP 서버 실행 인자가 비어있습니다.");
		}
		List<String> command = new ArrayList<>();
		command.add(properties.getCommand());
		command.addAll(args);

		ProcessBuilder pb = new ProcessBuilder(command);
		Map<String, String> env = pb.environment();
		if (extraEnv != null) {
			env.putAll(extraEnv);
		}
		if (properties.getRegion() != null) {
			env.putIfAbsent("AWS_REGION", properties.getRegion());
		}
		Process process = pb.start();
		// stdout/stderr 드레인(간단 버전)
		drainAsync(process.getInputStream(), s -> {});
		drainAsync(process.getErrorStream(), s -> {});
		return process;
	}

	private void drainAsync(InputStream is, Consumer<String> consumer) {
		Executors.newSingleThreadExecutor().submit(() -> {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				String line;
				while ((line = br.readLine()) != null) {
					consumer.accept(line);
				}
			} catch (IOException ignored) { }
		});
	}

	private long defaultValue(Integer value, long defaultVal) {
		return value == null ? defaultVal : value;
	}
} 