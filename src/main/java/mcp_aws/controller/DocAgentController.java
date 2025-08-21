package mcp_aws.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequestMapping("/api/doc")
public class DocAgentController {

	@PostMapping(value = "/chat", consumes = "application/json")
	@ResponseBody
	public String chat(@RequestBody Map<String, Object> body) {
		String query = (String) body.getOrDefault("query", "");
		return "[DOC] " + (query == null ? "" : query);
	}
} 