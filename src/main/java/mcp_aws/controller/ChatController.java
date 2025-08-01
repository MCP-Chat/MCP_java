package mcp_aws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import mcp_aws.service.McpService;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final McpService mcpService;

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    @PostMapping("/query")
    @ResponseBody
    public String processQuery(@RequestBody String query) {
        return mcpService.processQuery(query);
    }
} 