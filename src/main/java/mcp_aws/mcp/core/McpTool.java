package mcp_aws.mcp.core;

import java.util.Map;

public interface McpTool {
    /**
     * MCP 도구 실행
     * @param command 실행할 명령
     * @param parameters 명령에 필요한 파라미터
     * @return 실행 결과
     */
    String execute(String command, Map<String, Object> parameters);
} 