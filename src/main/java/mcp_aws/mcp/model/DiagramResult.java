package mcp_aws.mcp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagramResult {
    private String diagramType;    // 다이어그램 유형 (시퀀스, 아키텍처 등)
    private String content;        // Mermaid 형식의 다이어그램 내용
    private String description;    // 다이어그램 설명
} 