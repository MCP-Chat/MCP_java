package mcp_aws.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.Map;

@Component
public class BedrockChatClient {
	private static final String MODEL_ID = "apac.anthropic.claude-3-5-sonnet-20241022-v2:0";
	private final BedrockRuntimeClient client;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public BedrockChatClient(BedrockRuntimeClient client) {
		this.client = client;
	}

	public String ask(String system, String user) {
		try {
			Map<String, Object> request = Map.of(
					"anthropic_version", "bedrock-2023-05-31",
					"max_tokens", 4096,
					"messages", List.of(
							Map.of(
									"role", "user",
									"content", List.of(Map.of("type", "text", "text", user))
							)
					),
					"system", system
			);
			InvokeModelRequest invoke = InvokeModelRequest.builder()
					.modelId(MODEL_ID)
					.body(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(request)))
					.build();
			InvokeModelResponse response = client.invokeModel(invoke);
			Map<String, Object> body = objectMapper.readValue(response.body().asUtf8String(), Map.class);
			Object contentObj = body.get("content");
			if (contentObj instanceof List<?> list) {
				for (Object o : list) {
					if (o instanceof Map<?, ?> m && "text".equals(m.get("type"))) {
						Object text = m.get("text");
						if (text instanceof String s) return s;
					}
				}
			}
			return "";
		} catch (Exception e) {
			throw new RuntimeException("Bedrock 호출 실패: " + e.getMessage(), e);
		}
	}
} 