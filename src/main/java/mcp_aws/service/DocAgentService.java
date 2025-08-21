package mcp_aws.service;

import mcp_aws.infrastructure.BedrockChatClient;
import mcp_aws.infrastructure.McpToolInvoker;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DocAgentService {
	private final McpToolInvoker invoker;
	private final BedrockChatClient bedrock;

	public DocAgentService(McpToolInvoker invoker, BedrockChatClient bedrock) {
		this.invoker = invoker;
		this.bedrock = bedrock;
	}

	public String handle(String query) {
		try {
			// 1) search_documentation
			Map<String, Object> searchArgs = new HashMap<>();
			searchArgs.put("searchPhrase", query);
			searchArgs.put("limit", 5);
			String searchResult = invoker.callTool(true, "search_documentation", searchArgs, Map.of("FASTMCP_LOG_LEVEL", "ERROR"));

			// 단순 파싱: 첫 번째 URL 추출 (MVP: 실제 JSON 파싱은 후속 보강)
			String url = extractFirstUrl(searchResult);
			if (url == null || url.isBlank()) {
				return "죄송합니다. 관련 문서를 찾지 못했습니다.";
			}

			// 2) read_documentation
			Map<String, Object> readArgs = new HashMap<>();
			readArgs.put("url", url);
			String docContent = invoker.callTool(true, "read_documentation", readArgs, Map.of("FASTMCP_LOG_LEVEL", "ERROR"));

			// 3) 요약 생성
			String system = "당신은 AWS 문서 전문 AI 어시스턴트입니다. 반드시 다음 형식으로 한국어 답변하세요:\n\n## 답변\n(문서 내용 기반 요약)\n\n## 참고 문서\n- [문서](" + url + ")";
			String user = "다음 AWS 문서 내용을 한국어로 요약하고 핵심만 정리해주세요.\n\n" + docContent;
			String answer = bedrock.ask(system, user);
			if (answer == null || answer.isBlank()) {
				return "문서를 읽었지만 요약에 실패했습니다.";
			}
			return answer;
		} catch (IOException e) {
			return "오류가 발생했습니다: " + e.getMessage();
		}
	}

	private String extractFirstUrl(String raw) {
		if (raw == null) return null;
		int http = raw.indexOf("http");
		if (http < 0) return null;
		int end = raw.indexOf('\n', http);
		if (end < 0) end = raw.length();
		return raw.substring(http, end).trim();
	}
} 