package mcp_aws.mcp.documentation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mcp_aws.mcp.documentation.AwsDocumentationMcpServer;
import mcp_aws.mcp.documentation.model.DocumentationSpecificModel;
import mcp_aws.mcp.model.DocumentSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentationService {
    
    private final AwsDocumentationMcpServer documentationServer;
    
    public DocumentationSpecificModel searchDocumentation(String query, int limit) {
        try {
            String response = documentationServer.execute("search_documentation", Map.of(
                "searchPhrase", query,
                "limit", limit
            ));
            
            // TODO: 응답을 파싱하여 DocumentationSpecificModel로 변환
            // 임시로 빈 결과 반환
            return DocumentationSpecificModel.builder()
                .searchQuery(query)
                .totalResults(0)
                .results(List.of())
                .language("ko")
                .build();
            
        } catch (Exception e) {
            log.error("Error searching documentation", e);
            throw new RuntimeException("문서 검색 중 오류가 발생했습니다.", e);
        }
    }
    
    public String readDocumentation(String url) {
        try {
            return documentationServer.execute("read_documentation", Map.of(
                "url", url
            ));
            
        } catch (Exception e) {
            log.error("Error reading documentation", e);
            throw new RuntimeException("문서 읽기 중 오류가 발생했습니다.", e);
        }
    }
    
    public String formatSearchResults(DocumentationSpecificModel model) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("검색어: %s\n", model.getSearchQuery()));
        result.append(String.format("총 결과: %d개\n\n", model.getTotalResults()));
        
        for (DocumentSearchResult doc : model.getResults()) {
            result.append(String.format("📚 [%s](%s)\n", doc.getTitle(), doc.getUrl()));
            result.append(doc.getContext()).append("\n\n");
        }
        
        return result.toString();
    }
} 