package mcp_aws.mcp.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Map;

@Slf4j
public abstract class BaseMcpServer implements McpTool {
    
    protected final BedrockRuntimeClient bedrockRuntimeClient;
    protected final ObjectMapper objectMapper;
    protected static final String MODEL_ID = "anthropic.claude-3-haiku-20240307-v1:0";

    protected BaseMcpServer(BedrockRuntimeClient bedrockRuntimeClient, ObjectMapper objectMapper) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Bedrock 모델을 호출하여 응답을 생성합니다.
     */
    protected String invokeModel(String prompt, String systemMessage) {
        try {
            Map<String, Object> request = Map.of(
                "anthropic_version", "bedrock-2023-05-31",
                "max_tokens", 2048,
                "messages", new Object[]{
                    Map.of(
                        "role", "user",
                        "content", prompt
                    )
                },
                "system", systemMessage
            );

            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                .modelId(MODEL_ID)
                .body(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(request)))
                .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(invokeRequest);
            Map<String, Object> responseBody = objectMapper.readValue(response.body().asUtf8String(), Map.class);

            return ((Map<String, String>) ((Map<String, Object>) responseBody.get("content")).get("text")).get("value");

        } catch (Exception e) {
            log.error("Error invoking Bedrock model", e);
            throw new RuntimeException("모델 호출 중 오류가 발생했습니다.", e);
        }
    }
} 