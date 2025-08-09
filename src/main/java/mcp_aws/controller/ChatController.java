package mcp_aws.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ChatController {

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    // 기존 단일 /query(Text)
    @PostMapping(value = "/query", consumes = "text/plain")
    @ResponseBody
    public String processQuery(@RequestBody String query) {
        return "[임시] 기존 엔드포인트 응답: " + (query == null ? "" : query);
    }

    // CLI 탭용 엔드포인트
    @PostMapping(value = "/query/cli", consumes = "application/json")
    @ResponseBody
    public String processCliQuery(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        String roleArn = (String) body.getOrDefault("roleArn", "");
        return "[CLI 임시 응답] query=" + (query == null ? "" : query) + (roleArn == null || roleArn.isBlank() ? "" : (" | roleArn=" + roleArn));
    }

    // 문서 탭용 엔드포인트
    @PostMapping(value = "/query/doc", consumes = "application/json")
    @ResponseBody
    public String processDocQuery(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        return "[DOC 임시 응답] query=" + (query == null ? "" : query);
    }
} 