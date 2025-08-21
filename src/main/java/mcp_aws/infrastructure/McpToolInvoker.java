package mcp_aws.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class McpToolInvoker {
	private final McpStdioClient stdioClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public McpToolInvoker(McpStdioClient stdioClient) {
		this.stdioClient = stdioClient;
	}

	public String getTools(boolean documentation, Map<String, String> extraEnv) throws IOException {
		var process = documentation ? stdioClient.startDocumentationServer(extraEnv) : stdioClient.startAwsApiServer(extraEnv);
		try {
			Map<String, Object> req = new HashMap<>();
			req.put("jsonrpc", "2.0");
			req.put("id", 1);
			req.put("method", "tools/list");
			return stdioClient.sendJsonRpc(process, objectMapper.writeValueAsString(req), 5000);
		} finally {
			stdioClient.stopServer(process);
		}
	}

	public String callTool(boolean documentation, String toolName, Map<String, Object> args, Map<String, String> extraEnv) throws IOException {
		var process = documentation ? stdioClient.startDocumentationServer(extraEnv) : stdioClient.startAwsApiServer(extraEnv);
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("name", toolName);
			if (args != null) params.put("arguments", args);
			Map<String, Object> req = new HashMap<>();
			req.put("jsonrpc", "2.0");
			req.put("id", 1);
			req.put("method", "tools/call");
			req.put("params", params);
			return stdioClient.sendJsonRpc(process, objectMapper.writeValueAsString(req), 30000);
		} finally {
			stdioClient.stopServer(process);
		}
	}
} 