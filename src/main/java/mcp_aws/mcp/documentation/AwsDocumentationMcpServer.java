package mcp_aws.mcp.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mcp_aws.mcp.core.BaseMcpServer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.Map;

@Slf4j
@Component
public class AwsDocumentationMcpServer extends BaseMcpServer {

    public AwsDocumentationMcpServer(BedrockRuntimeClient bedrockRuntimeClient, ObjectMapper objectMapper) {
        super(bedrockRuntimeClient, objectMapper);
    }

    @Override
    public String execute(String command, Map<String, Object> parameters) {
        return switch (command) {
            case "search_documentation" -> searchDocumentation(
                (String) parameters.get("searchPhrase"),
                (Integer) parameters.get("limit")
            );
            case "read_documentation" -> readDocumentation(
                (String) parameters.get("url")
            );
            default -> throw new IllegalArgumentException("지원하지 않는 명령입니다: " + command);
        };
    }

    private String searchDocumentation(String searchPhrase, Integer limit) {
        String systemMessage = "You are an AWS documentation search assistant. " +
                             "Please search and return relevant documentation in a structured format.";
        
        String prompt = String.format("Search AWS documentation for: %s\nLimit results to: %d", 
                                    searchPhrase, limit);
        
        return invokeModel(prompt, systemMessage);
    }

    private String readDocumentation(String url) {
        String systemMessage = "You are an AWS documentation reader. " +
                             "Please read and provide a detailed summary of the documentation.";
        
        String prompt = String.format("Read and summarize AWS documentation from: %s", url);
        
        return invokeModel(prompt, systemMessage);
    }
} 