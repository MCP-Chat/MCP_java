package mcp_aws.mcp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentSearchResult {
    private int rankOrder;
    private String url;
    private String title;
    private String context;
    private double score;
} 