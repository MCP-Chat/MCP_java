package mcp_aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {
	private String command;
	private List<String> awsApiArgs;
	private List<String> documentationArgs;
	private Integer startTimeoutMs;
	private Integer requestTimeoutMs;
	private Integer shutdownTimeoutMs;
	private Integer maxRetries;
	private String logLevel;
	private String region;

	public String getCommand() { return command; }
	public void setCommand(String command) { this.command = command; }

	public List<String> getAwsApiArgs() { return awsApiArgs; }
	public void setAwsApiArgs(List<String> awsApiArgs) { this.awsApiArgs = awsApiArgs; }

	public List<String> getDocumentationArgs() { return documentationArgs; }
	public void setDocumentationArgs(List<String> documentationArgs) { this.documentationArgs = documentationArgs; }

	public Integer getStartTimeoutMs() { return startTimeoutMs; }
	public void setStartTimeoutMs(Integer startTimeoutMs) { this.startTimeoutMs = startTimeoutMs; }

	public Integer getRequestTimeoutMs() { return requestTimeoutMs; }
	public void setRequestTimeoutMs(Integer requestTimeoutMs) { this.requestTimeoutMs = requestTimeoutMs; }

	public Integer getShutdownTimeoutMs() { return shutdownTimeoutMs; }
	public void setShutdownTimeoutMs(Integer shutdownTimeoutMs) { this.shutdownTimeoutMs = shutdownTimeoutMs; }

	public Integer getMaxRetries() { return maxRetries; }
	public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }

	public String getLogLevel() { return logLevel; }
	public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
} 