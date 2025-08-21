package mcp_aws.service;

import mcp_aws.config.McpProperties;
import mcp_aws.infrastructure.BedrockChatClient;
import mcp_aws.infrastructure.McpToolInvoker;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CliAgentService {
	private final McpToolInvoker invoker;
	private final BedrockChatClient bedrock;
	private final McpProperties props;

	public CliAgentService(McpToolInvoker invoker, BedrockChatClient bedrock, McpProperties props) {
		this.invoker = invoker;
		this.bedrock = bedrock;
		this.props = props;
	}

	public String handle(String query, String roleArn) {
		try {
			Map<String, String> env = new HashMap<>();
			env.put("FASTMCP_LOG_LEVEL", props.getLogLevel() == null ? "ERROR" : props.getLogLevel());
			env.put("ALLOW_WRITE", "false");
			if (props.getRegion() != null) env.put("AWS_REGION", props.getRegion());
			// TODO: roleArn 지원(STS AssumeRole) - 후속 단계에서 구현

			String toolsJson = invoker.getTools(false, env);
			String tool = chooseTool(query, toolsJson);
			Map<String, Object> args = new HashMap<>();
			String raw = invoker.callTool(false, tool, args, env);

			String system = "당신은 AWS CLI 결과를 한국어로 정리하는 어시스턴트입니다. 핵심만 간결히 요약하고, 표 형태가 유용하면 표로 제시하세요.";
			String user = "사용자 요청:\n" + query + "\n\n다음 AWS 결과를 한국어로 정리:\n" + raw;
			String answer = bedrock.ask(system, user);
			return (answer == null || answer.isBlank()) ? raw : answer;
		} catch (IOException e) {
			return "오류가 발생했습니다: " + e.getMessage();
		}
	}

	private String chooseTool(String query, String toolsJson) {
		if (containsAny(query, "ec2", "인스턴스") && toolsJson.contains("list_instances")) return "list_instances";
		if (containsAny(query, "보안 그룹", "security group") && toolsJson.contains("list_security_groups")) return "list_security_groups";
		if (containsAny(query, "스냅샷", "snapshot") && toolsJson.contains("list_snapshots")) return "list_snapshots";
		if (containsAny(query, "볼륨", "volume") && toolsJson.contains("list_volumes")) return "list_volumes";
		if (containsAny(query, "VPC", "vpc") && toolsJson.contains("list_vpcs")) return "list_vpcs";
		// fallback: toolsJson에서 list_* 중 첫 번째
		Matcher m = Pattern.compile("list_[a-z_]+").matcher(toolsJson);
		if (m.find()) return m.group();

		return "list_instances";
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