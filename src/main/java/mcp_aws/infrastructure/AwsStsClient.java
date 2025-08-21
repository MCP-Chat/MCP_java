package mcp_aws.infrastructure;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class AwsStsClient {
	private final StsClient sts;

	public AwsStsClient(StsClient sts) {
		this.sts = sts;
	}

	public Map<String, String> assumeRoleEnv(String roleArn) {
		if (roleArn == null || roleArn.isBlank()) return Map.of();
		AssumeRoleRequest req = AssumeRoleRequest.builder()
				.roleArn(roleArn)
				.roleSessionName("mcp-java-session")
				.durationSeconds((int) Duration.ofHours(1).getSeconds())
				.build();
		AssumeRoleResponse resp = sts.assumeRole(req);
		var creds = resp.credentials();
		Map<String, String> env = new HashMap<>();
		env.put("AWS_ACCESS_KEY_ID", creds.accessKeyId());
		env.put("AWS_SECRET_ACCESS_KEY", creds.secretAccessKey());
		env.put("AWS_SESSION_TOKEN", creds.sessionToken());
		return env;
	}
} 