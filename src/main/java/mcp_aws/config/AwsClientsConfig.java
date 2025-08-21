package mcp_aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.sts.StsClient;

@Configuration
public class AwsClientsConfig {

	@Value("${aws.region:ap-northeast-2}")
	private String awsRegion;

	private final AwsCredentialsProperties props;

	public AwsClientsConfig(AwsCredentialsProperties props) {
		this.props = props;
	}

	@Bean
	public BedrockRuntimeClient bedrockRuntimeClient() {
		return BedrockRuntimeClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(resolveProvider())
				.build();
	}

	@Bean
	public StsClient stsClient() {
		return StsClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(resolveProvider())
				.build();
	}

	private AwsCredentialsProvider resolveProvider() {
		// If profile is set, prefer profile
		if (props.getProfile() != null && !props.getProfile().isBlank()) {
			return ProfileCredentialsProvider.create(props.getProfile());
		}
		// If AK/SK provided, use static (with optional session token)
		if (props.getAccessKeyId() != null && !props.getAccessKeyId().isBlank() &&
				props.getSecretAccessKey() != null && !props.getSecretAccessKey().isBlank()) {
			AwsCredentials creds = (props.getSessionToken() == null || props.getSessionToken().isBlank())
					? AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey())
					: AwsSessionCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey(), props.getSessionToken());
			return StaticCredentialsProvider.create(creds);
		}
		// fall back to default
		return DefaultCredentialsProvider.create();
	}
} 