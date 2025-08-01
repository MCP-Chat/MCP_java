package mcp_aws.mcp.documentation.model;

import lombok.Builder;
import lombok.Data;
import mcp_aws.mcp.model.DocumentSearchResult;

import java.util.List;

@Data
@Builder
public class DocumentationSpecificModel {
    private String searchQuery;           // 검색 쿼리
    private int totalResults;             // 총 결과 수
    private List<DocumentSearchResult> results;  // 검색 결과 목록
    private String language;              // 문서 언어 (ko, en 등)
    private String serviceCategory;       // AWS 서비스 카테고리
} 