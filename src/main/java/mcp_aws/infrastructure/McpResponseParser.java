package mcp_aws.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class McpResponseParser {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static List<String> parseToolNames(String json) {
		List<String> names = new ArrayList<>();
		try {
			JsonNode root = MAPPER.readTree(json);
			JsonNode result = root.get("result");
			if (result == null) return names;
			JsonNode tools = result.get("tools");
			if (tools != null && tools.isArray()) {
				for (JsonNode t : tools) {
					JsonNode name = t.get("name");
					if (name != null && name.isTextual()) names.add(name.asText());
				}
			}
			return names;
		} catch (Exception e) {
			return names;
		}
	}

	public static String extractToolCallText(String json) {
		try {
			JsonNode root = MAPPER.readTree(json);
			JsonNode result = root.get("result");
			if (result == null) return json;
			// Prefer explicit 'output' field if present
			JsonNode output = result.get("output");
			if (output != null) {
				if (output.isTextual()) return output.asText();
				return output.toString();
			}
			// If result is textual
			if (result.isTextual()) return result.asText();
			// If result has content
			JsonNode content = result.get("content");
			if (content != null) {
				if (content.isTextual()) return content.asText();
				if (content.isArray()) {
					StringBuilder sb = new StringBuilder();
					for (JsonNode c : content) {
						if (c.isTextual()) sb.append(c.asText()).append('\n');
						else sb.append(c.toString()).append('\n');
					}
					return sb.toString().trim();
				}
			}
			// Fallback: stringify result
			return result.toString();
		} catch (Exception e) {
			return json;
		}
	}

	public static String extractFirstUrlFromSearchResult(String json) {
		try {
			JsonNode root = MAPPER.readTree(json);
			JsonNode result = root.get("result");
			if (result == null) result = root; // sometimes raw array
			// If array of items {url,...}
			if (result.isArray()) {
				Iterator<JsonNode> it = result.elements();
				while (it.hasNext()) {
					JsonNode item = it.next();
					JsonNode url = item.get("url");
					if (url != null && url.isTextual()) return url.asText();
				}
			}
			// If result has items field array
			JsonNode items = result.get("items");
			if (items != null && items.isArray()) {
				for (JsonNode item : items) {
					JsonNode url = item.get("url");
					if (url != null && url.isTextual()) return url.asText();
				}
			}
			// Fallback: regex-less naive scan
			String raw = result.toString();
			int idx = raw.indexOf("http");
			if (idx >= 0) {
				int end = raw.indexOf('"', idx);
				if (end > idx) return raw.substring(idx, end);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
} 