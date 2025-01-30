package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import org.json.JSONObject;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
		String path = requestEvent.getPath();
		String httpMethod = requestEvent.getHttpMethod();

		if ("/hello".equals(path) && "GET".equals(httpMethod)) {
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(
							new JSONObject().put(
									"message",
									"Hello from Lambda"
							).toString()
					);
		} else {
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(400)

					.withBody(
							new JSONObject().put(
									"message",
									"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s"
									.formatted(path, httpMethod)
							).toString()
					);
		}
	}
}
