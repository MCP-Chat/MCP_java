package mcp_aws.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class McpToolInvoker {
	private final McpStdioClient stdio;
	private final ObjectMapper om = new ObjectMapper();

	public McpToolInvoker(McpStdioClient stdioClient) {
		this.stdio = stdioClient;
	}

	public String getTools(boolean documentation, Map<String, String> extraEnv) throws IOException {
		if (documentation) stdio.ensureDocumentationStarted(extraEnv); else stdio.ensureAwsApiStarted(extraEnv);
		Map<String, Object> req = new HashMap<>();
		req.put("method", "tools/list");
		return stdio.sendRequest(documentation, req, 5000);
	}

	public String callTool(boolean documentation, String toolName, Map<String, Object> args, Map<String, String> extraEnv) throws IOException {
		if (documentation) stdio.ensureDocumentationStarted(extraEnv); else stdio.ensureAwsApiStarted(extraEnv);
		Map<String, Object> params = new HashMap<>();
		params.put("name", toolName);
		if (args != null) params.put("arguments", args);
		Map<String, Object> req = new HashMap<>();
		req.put("method", "tools/call");
		req.put("params", params);
		return stdio.sendRequest(documentation, req, 30000);
	}
} 