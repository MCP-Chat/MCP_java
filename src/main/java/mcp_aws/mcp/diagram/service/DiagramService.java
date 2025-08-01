package mcp_aws.mcp.diagram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcp_aws.mcp.diagram.AwsDiagramMcpServer;
import mcp_aws.mcp.diagram.model.DiagramSpecificModel;
import mcp_aws.mcp.model.DiagramResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagramService {
    
    private final AwsDiagramMcpServer diagramServer;
    
    public DiagramSpecificModel createDiagram(String description, String type) {
        try {
            String response = diagramServer.execute("create_diagram", Map.of(
                "description", description,
                "type", type
            ));
            
            // TODO: 응답을 파싱하여 DiagramSpecificModel로 변환
            // 임시로 기본 결과 반환
            return DiagramSpecificModel.builder()
                .requestDescription(description)
                .diagramType(type)
                .components(List.of())
                .connections(List.of())
                .result(DiagramResult.builder()
                    .diagramType(type)
                    .content(response)
                    .description(description)
                    .build())
                .style("basic")
                .includeIcons(true)
                .build();
            
        } catch (Exception e) {
            log.error("Error creating diagram", e);
            throw new RuntimeException("다이어그램 생성 중 오류가 발생했습니다.", e);
        }
    }
    
    public DiagramSpecificModel updateDiagram(String content, String changes) {
        try {
            String response = diagramServer.execute("update_diagram", Map.of(
                "content", content,
                "changes", changes
            ));
            
            // TODO: 응답을 파싱하여 DiagramSpecificModel로 변환
            return DiagramSpecificModel.builder()
                .requestDescription(changes)
                .diagramType(determineDiagramType(content))
                .result(DiagramResult.builder()
                    .content(response)
                    .description(changes)
                    .build())
                .build();
            
        } catch (Exception e) {
            log.error("Error updating diagram", e);
            throw new RuntimeException("다이어그램 수정 중 오류가 발생했습니다.", e);
        }
    }
    
    private String determineDiagramType(String content) {
        if (content.contains("sequenceDiagram")) {
            return "sequence";
        } else if (content.contains("classDiagram")) {
            return "class";
        } else if (content.contains("flowchart")) {
            return "flowchart";
        } else {
            return "architecture";
        }
    }
    
    public String formatDiagramResult(DiagramSpecificModel model) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("다이어그램 유형: %s\n", model.getDiagramType()));
        result.append(String.format("설명: %s\n\n", model.getRequestDescription()));
        result.append("```mermaid\n");
        result.append(model.getResult().getContent());
        result.append("\n```\n");
        
        if (!model.getComponents().isEmpty()) {
            result.append("\n포함된 컴포넌트:\n");
            model.getComponents().forEach(comp -> result.append("- ").append(comp).append("\n"));
        }
        
        return result.toString();
    }
} 