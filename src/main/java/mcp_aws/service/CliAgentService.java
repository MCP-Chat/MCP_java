package mcp_aws.service;

import mcp_aws.config.McpProperties;
import mcp_aws.infrastructure.AwsStsClient;
import mcp_aws.infrastructure.BedrockChatClient;
import mcp_aws.infrastructure.McpResponseParser;
import mcp_aws.infrastructure.McpToolInvoker;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CliAgentService {
	private final McpToolInvoker invoker;
	private final BedrockChatClient bedrock;
	private final McpProperties props;
	private final AwsStsClient sts;

	public CliAgentService(McpToolInvoker invoker, BedrockChatClient bedrock, McpProperties props, AwsStsClient sts) {
		this.invoker = invoker;
		this.bedrock = bedrock;
		this.props = props;
		this.sts = sts;
	}

	public String handle(String query, String roleArn) {
		try {
			Map<String, String> env = new HashMap<>();
			env.put("FASTMCP_LOG_LEVEL", props.getLogLevel() == null ? "ERROR" : props.getLogLevel());
			env.put("FASTMCP_TRANSPORT", "stdio");
			env.put("ALLOW_WRITE", "false");
			if (props.getRegion() != null) env.put("AWS_REGION", props.getRegion());
			if (roleArn != null && !roleArn.isBlank()) env.putAll(sts.assumeRoleEnv(roleArn));

			String toolsJson = invoker.getTools(false, env);
			List<String> tools = McpResponseParser.parseToolNames(toolsJson);
			String tool = chooseTool(query, tools);
			Map<String, Object> args = new HashMap<>();
			String resp = invoker.callTool(false, tool, args, env);
			String raw = McpResponseParser.extractToolCallText(resp);

			String system = "당신은 AWS CLI 결과를 한국어로 정리하는 어시스턴트입니다. 핵심만 간결히 요약하고, 표 형태가 유용하면 표로 제시하세요.";
			String user = "사용자 요청:\n" + query + "\n\n다음 AWS 결과를 한국어로 정리:\n" + raw;
			String answer = bedrock.ask(system, user);
			return (answer == null || answer.isBlank()) ? raw : answer;
		} catch (IOException e) {
			return "오류가 발생했습니다: " + e.getMessage();
		}
	}

	private String chooseTool(String query, List<String> tools) {
		if (containsAny(query, "ec2", "인스턴스") && tools.contains("list_instances")) return "list_instances";
		if (containsAny(query, "보안 그룹", "security group") && tools.contains("list_security_groups")) return "list_security_groups";
		if (containsAny(query, "스냅샷", "snapshot") && tools.contains("list_snapshots")) return "list_snapshots";
		if (containsAny(query, "볼륨", "volume") && tools.contains("list_volumes")) return "list_volumes";
		if (containsAny(query, "VPC", "vpc") && tools.contains("list_vpcs")) return "list_vpcs";
		for (String t : tools) {
			if (t.startsWith("list_")) return t;
		}
		return tools.isEmpty() ? "list_instances" : tools.get(0);
	}

	private boolean containsAny(String s, String... keys) {
		if (s == null) return false;
		String lower = s.toLowerCase();
		for (String k : keys) {
			if (lower.contains(k.toLowerCase())) return true;
		}
		return false;
	}
} 