package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "uuid_trigger", resourceType = ResourceType.CLOUDWATCH_RULE)
@DependsOn(name = "${target_bucket}", resourceType = ResourceType.S3_BUCKET)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "bucket", value = "${target_bucket}")})
public class UuidGenerator implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		LambdaLogger logger = context.getLogger();
		String BUCKET_NAME = System.getenv("bucket");
		String fileName = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		List<String> uuidList = IntStream.range(0, 10)
				.mapToObj(i -> UUID.randomUUID().toString())
				.collect(Collectors.toList());

		String jsonContent = createJson(uuidList);

		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
			InputStream stream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
			s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, stream, null));
			logger.log("Successfully written UUIDs to " + fileName);
			return "Successfully written UUIDs to " + fileName;
		} catch (Exception e) {
			logger.log("Error" + e.getMessage());
			return "Error writing to S3: " + e.getMessage();
		}
	}

	private String createJson(List<String> uuidList) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(new UuidWrapper(uuidList));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error creating JSON", e);
		}
	}

	static class UuidWrapper {
		private final List<String> ids;

		public UuidWrapper(List<String> ids) {
			this.ids = ids;
		}

		public List<String> getIds() {
			return ids;
		}
	}
}
