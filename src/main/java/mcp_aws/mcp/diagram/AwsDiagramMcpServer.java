package mcp_aws.mcp.diagram;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mcp_aws.mcp.core.BaseMcpServer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.Map;

@Slf4j
@Component
public class AwsDiagramMcpServer extends BaseMcpServer {

    public AwsDiagramMcpServer(BedrockRuntimeClient bedrockRuntimeClient, ObjectMapper objectMapper) {
        super(bedrockRuntimeClient, objectMapper);
    }

    @Override
    public String execute(String command, Map<String, Object> parameters) {
        return switch (command) {
            case "create_diagram" -> createDiagram(
                (String) parameters.get("description"),
                (String) parameters.getOrDefault("type", "architecture")
            );
            case "update_diagram" -> updateDiagram(
                (String) parameters.get("content"),
                (String) parameters.get("changes")
            );
            default -> throw new IllegalArgumentException("지원하지 않는 명령입니다: " + command);
        };
    }

    private String createDiagram(String description, String type) {
        String systemMessage = "You are an AWS architecture diagram creator. " +
                             "Please create a Mermaid diagram based on the description. " +
                             "Use only standard Mermaid syntax without custom styling.";
        
        String prompt = String.format("Create a %s diagram for the following AWS architecture:\n%s", 
                                    type, description);
        
        return invokeModel(prompt, systemMessage);
    }

    private String updateDiagram(String content, String changes) {
        String systemMessage = "You are an AWS architecture diagram editor. " +
                             "Please update the existing Mermaid diagram based on the requested changes. " +
                             "Maintain the original structure where possible.";
        
        String prompt = String.format("Update the following Mermaid diagram:\n%s\n\nRequested changes:\n%s",
                                    content, changes);
        
        return invokeModel(prompt, systemMessage);
    }
} 