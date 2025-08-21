package mcp_aws.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/cli")
public class CliAgentController {

	@GetMapping("/")
	public String home() {
		return "chat";
	}

	@PostMapping(value = "/chat", consumes = "application/json")
	@ResponseBody
	public String chat(@RequestBody Map<String, Object> body) {
		String query = (String) body.getOrDefault("query", "");
		String roleArn = (String) body.getOrDefault("roleArn", "");
		return "[CLI] " + (query == null ? "" : query) + (roleArn == null || roleArn.isBlank() ? "" : (" | roleArn=" + roleArn));
	}
} 