package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.json.JSONObject;

import java.util.Map;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = false,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder builder = APIGatewayV2HTTPResponse.builder();
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Accept-Version", "*"
        );
        builder.withHeaders(headers);
        try {
            LambdaLogger logger = context.getLogger();
            logger.log("Received event: " + requestEvent.toString());
            logger.log("requestEvent.getRequestContext: " + requestEvent.getRequestContext().toString());
            logger.log("requestEvent.getPath(): " + requestEvent.getRawPath());
            String path = requestEvent.getRawPath();
            String httpMethod = (requestEvent.getRequestContext() != null && requestEvent.getRequestContext().getHttp().getMethod() != null) ?
                    requestEvent.getRequestContext().getHttp().getMethod() : null;


            if ("/hello".equals(path) && "GET".equals(httpMethod)) {
                JSONObject responseBody = new JSONObject().put("message", "Hello from Lambda");
                return builder.withStatusCode(200).withBody(responseBody.toString()).build();
            } else {
                String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, httpMethod);
                JSONObject responseBody = new JSONObject().put("message", errorMessage);
                return builder.withStatusCode(400).withBody(responseBody.toString()).build();
            }
        } catch (Exception e) {
            JSONObject responseBody = new JSONObject().put("error", "Internal Server Error");
            return builder.withStatusCode(500).withBody(responseBody.toString()).build();
        }
    }
}