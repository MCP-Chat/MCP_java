package mcp_aws.infrastructure;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class McpToolInvoker {
	private final McpStdioClient stdioClient;

	public McpToolInvoker(McpStdioClient stdioClient) {
		this.stdioClient = stdioClient;
	}

	public String getTools(boolean documentation, Map<String, String> extraEnv) throws IOException {
		var process = documentation ? stdioClient.startDocumentationServer(extraEnv) : stdioClient.startAwsApiServer(extraEnv);
		try {
			// TODO: JSON-RPC 요청 구성 (MCP get_tools)
			String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}";
			return stdioClient.sendJsonRpc(process, request, 5000);
		} finally {
			stdioClient.stopServer(process);
		}
	}

	public String callTool(boolean documentation, String toolName, Map<String, Object> args, Map<String, String> extraEnv) throws IOException {
		var process = documentation ? stdioClient.startDocumentationServer(extraEnv) : stdioClient.startAwsApiServer(extraEnv);
		try {
			// TODO: JSON-RPC 요청 구성 (MCP tools/call)
			String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"" + toolName + "\"}}";
			return stdioClient.sendJsonRpc(process, request, 30000);
		} finally {
			stdioClient.stopServer(process);
		}
	}
} 