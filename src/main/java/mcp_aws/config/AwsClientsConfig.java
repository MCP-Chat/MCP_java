package mcp_aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.sts.StsClient;

@Configuration
public class AwsClientsConfig {

	@Value("${aws.region:ap-northeast-2}")
	private String awsRegion;

	@Bean
	public BedrockRuntimeClient bedrockRuntimeClient() {
		return BedrockRuntimeClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	@Bean
	public StsClient stsClient() {
		return StsClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
} 