package mcp_aws.controller;

import mcp_aws.domain.dto.ChatRequest;
import mcp_aws.service.CliAgentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/cli")
public class CliAgentController {

	private final CliAgentService service;

	public CliAgentController(CliAgentService service) {
		this.service = service;
	}

	@GetMapping("/")
	public String home() {
		return "chat";
	}

	@PostMapping(value = "/chat", consumes = "application/json")
	@ResponseBody
	public String chat(@RequestBody ChatRequest body) {
		String query = body.getQuery();
		String roleArn = body.getRoleArn();
		return service.handle(query, roleArn);
	}
} 