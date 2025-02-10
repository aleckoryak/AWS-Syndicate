package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        lambdaName = "api_handler",
        roleName = "api_handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@DependsOn(name = "Events", resourceType = ResourceType.DYNAMODB_TABLE)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Received event: " + event.toString());
            // Parse input from APIGatewayV2HTTPEvent
            Map<String, Object> input = objectMapper.readValue(event.getBody(), Map.class);
            Integer principalId = (Integer) input.get("principalId");
            Map<String, Object> content = (Map<String, Object>) input.get("content");

            // Generate UUID and timestamps
            String eventId = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();

            // DynamoDB item
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", new AttributeValue(eventId));
            item.put("principalId", new AttributeValue().withN(principalId.toString()));
            item.put("createdAt", new AttributeValue(createdAt));
            item.put("body", new AttributeValue(objectMapper.writeValueAsString(content)));

            // Save to DynamoDB
            PutItemRequest putItemRequest = new PutItemRequest().withTableName("Events").withItem(item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);

            // Prepare response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", eventId);
            responseBody.put("principalId", principalId);
            responseBody.put("createdAt", createdAt);
            responseBody.put("body", content);

            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(201);
            response.setBody(objectMapper.writeValueAsString(Map.of("event", responseBody)));
            response.setHeaders(Map.of("Content-Type", "application/json"));

            return response;
        } catch (Exception e) {
            logger.log("Error in processing request: " + e.getMessage());
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(500)
                    .withBody("{\"message\": \"Internal server error\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .build();
        }
    }
}
