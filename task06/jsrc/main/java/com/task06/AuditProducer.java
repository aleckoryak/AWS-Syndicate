package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(name = "${target_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_table", value = "${target_table}")})
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
public class AuditProducer implements RequestHandler<DynamodbEvent, Map<String,Object>> {
	private static final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	public static final String INSERT = "INSERT";
	public static final String MODIFY = "MODIFY";
	public static final String KEY = "key";
	public static final String VALUE = "value";

	public Map<String, Object> handleRequest(DynamodbEvent request, Context context) {
		LambdaLogger lambdaLogger = context.getLogger();
		lambdaLogger.log("request:" + request.toString());

		for (DynamodbEvent.DynamodbStreamRecord record : request.getRecords()) {
			lambdaLogger.log("Received eventName: " + record.getEventName());
			StreamRecord streamRecord = record.getDynamodb();

			String id = UUID.randomUUID().toString();
			String createdAt = Instant.now().toString();

			if (INSERT.equals(record.getEventName())) {
				if (streamRecord != null && streamRecord.getNewImage() != null && streamRecord.getKeys() != null && streamRecord.getNewImage().get(VALUE) != null) {
					String key = streamRecord.getKeys().get(KEY).getS();
					Integer newValue = Integer.valueOf(streamRecord.getNewImage().get(VALUE).getN());

					// Prepare item for DynamoDB
					Item item = new Item();
					item.withString("id", id);
					item.withString("itemKey", key);
					item.withString("modificationTime", createdAt);
					item.withMap("newValue", Map.of("key", key, "value", newValue));


					// Save item to DynamoDB
					PutItemRequest putItemRequest = new PutItemRequest().withTableName(System.getenv("target_table")).withItem(ItemUtils.toAttributeValues(item));
					PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);

					lambdaLogger.log("putItemResult:" + putItemResult.toString());
				}
			} else if (MODIFY.equals(record.getEventName())) {
				if (streamRecord != null && streamRecord.getNewImage() != null && streamRecord.getKeys() != null && streamRecord.getNewImage().get(VALUE) != null) {
					String key = streamRecord.getKeys().get(KEY).getS();
					Integer oldValue = (streamRecord.getOldImage().get(VALUE) != null)? Integer.valueOf(streamRecord.getOldImage().get(VALUE).getN()):0;
					Integer newValue = Integer.valueOf(streamRecord.getNewImage().get(VALUE).getN());

					// Prepare item for DynamoDB
					Item item = new Item();
					item.withString("id", id);
					item.withString("itemKey", key);
					item.withString("modificationTime", createdAt);
					item.withString("updatedAttribute", "value");
					item.withNumber("oldValue", oldValue);
					item.withNumber("newValue", newValue);



					// Save item to DynamoDB
					PutItemRequest putItemRequest = new PutItemRequest().withTableName(System.getenv("target_table")).withItem(ItemUtils.toAttributeValues(item));
					PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);

					lambdaLogger.log("putItemResult:" + putItemResult.toString());
				}
			} else {
				lambdaLogger.log("Request cannot be processed :" + request.toString());
			}
		}

		return null;
	}
}
