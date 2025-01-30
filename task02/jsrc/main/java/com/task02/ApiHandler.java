package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import org.json.JSONObject;

import java.util.Collections;

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
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		String path = requestEvent.getPath();
		String httpMethod = requestEvent.getHttpMethod();

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setHeaders(Collections.singletonMap("Content-Type", "application/json"));

		if ("/hello".equals(path) && "GET".equals(httpMethod)) {
			JSONObject responseBody = new JSONObject()
					.put("message", "Hello from Lambda");
			response.withStatusCode(200)
					.withBody(responseBody.toString());
		} else {
			String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, httpMethod);
			JSONObject responseBody = new JSONObject()
					.put("error", errorMessage);
			response.withStatusCode(400)
					.withBody(responseBody.toString());
		}

		return response;
	}
}