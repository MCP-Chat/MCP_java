package mcp_aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcp_aws.mcp.documentation.service.DocumentationService;
import mcp_aws.mcp.diagram.service.DiagramService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {
    
    private final DocumentationService documentationService;
    private final DiagramService diagramService;
    
    public String processQuery(String query) {
        try {
            // 쿼리 분석하여 적절한 서비스로 라우팅
            if (query.toLowerCase().contains("diagram") || 
                query.toLowerCase().contains("다이어그램") ||
                query.toLowerCase().contains("구조도")) {
                
                var diagramModel = diagramService.createDiagram(
                    query,
                    determineDigramType(query)
                );
                
                return diagramService.formatDiagramResult(diagramModel);
                
            } else {
                var docModel = documentationService.searchDocumentation(query, 5);
                return documentationService.formatSearchResults(docModel);
            }
            
        } catch (Exception e) {
            log.error("Error processing query", e);
            return "죄송합니다. 요청을 처리하는 중 오류가 발생했습니다.";
        }
    }
    
    private String determineDigramType(String query) {
        if (query.toLowerCase().contains("sequence") || 
            query.toLowerCase().contains("시퀀스") ||
            query.toLowerCase().contains("순서")) {
            return "sequence";
        } else if (query.toLowerCase().contains("class") || 
                   query.toLowerCase().contains("클래스")) {
            return "class";
        } else if (query.toLowerCase().contains("flow") || 
                   query.toLowerCase().contains("플로우") ||
                   query.toLowerCase().contains("흐름")) {
            return "flowchart";
        } else {
            return "architecture";
        }
    }
} 