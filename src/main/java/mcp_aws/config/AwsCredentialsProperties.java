package mcp_aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws")
public class AwsCredentialsProperties {
	private String accessKeyId;
	private String secretAccessKey;
	private String sessionToken;
	private String profile;
	private String region;

	public String getAccessKeyId() { return accessKeyId; }
	public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }
	public String getSecretAccessKey() { return secretAccessKey; }
	public void setSecretAccessKey(String secretAccessKey) { this.secretAccessKey = secretAccessKey; }
	public String getSessionToken() { return sessionToken; }
	public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
	public String getProfile() { return profile; }
	public void setProfile(String profile) { this.profile = profile; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
} 