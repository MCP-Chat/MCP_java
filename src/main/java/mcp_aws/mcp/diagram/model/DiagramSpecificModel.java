package mcp_aws.mcp.diagram.model;

import lombok.Builder;
import lombok.Data;
import mcp_aws.mcp.model.DiagramResult;

import java.util.List;

@Data
@Builder
public class DiagramSpecificModel {
    private String requestDescription;    // 다이어그램 생성 요청 설명
    private String diagramType;          // 다이어그램 유형
    private List<String> components;     // 포함된 AWS 컴포넌트 목록
    private List<String> connections;    // 컴포넌트 간 연결 정보
    private DiagramResult result;        // 생성된 다이어그램 결과
    private String style;                // 다이어그램 스타일 (기본, 상세 등)
    private boolean includeIcons;        // AWS 아이콘 포함 여부
} 