package mcp_aws.controller;

import mcp_aws.service.DocAgentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/doc")
public class DocAgentController {

	private final DocAgentService service;

	public DocAgentController(DocAgentService service) {
		this.service = service;
	}

	@PostMapping(value = "/chat", consumes = "application/json")
	@ResponseBody
	public String chat(@RequestBody Map<String, Object> body) {
		String query = (String) body.getOrDefault("query", "");
		return service.handle(query);
	}
} 