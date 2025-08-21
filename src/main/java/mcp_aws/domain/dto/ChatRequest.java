package mcp_aws.domain.dto;

public class ChatRequest {
	private String query;
	private String roleArn;

	public String getQuery() { return query; }
	public void setQuery(String query) { this.query = query; }
	public String getRoleArn() { return roleArn; }
	public void setRoleArn(String roleArn) { this.roleArn = roleArn; }
} 