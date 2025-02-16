package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.layer.WeatherService;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	layers = {"weather-service-layer"},
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@LambdaLayer(
		layerName = "weather-service-layer",
		libraries = {"weather-service-1.0.0.jar"},
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)

public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private final WeatherService weatherService = new WeatherService();


	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
		APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder builder = APIGatewayV2HTTPResponse.builder();
		try {
			LambdaLogger logger = context.getLogger();
			logger.log("Received event: " + requestEvent.toString());
			logger.log("requestEvent.getRequestContext: " + requestEvent.getRequestContext().toString());
			logger.log("requestEvent.getPath(): " + requestEvent.getRawPath());
			String path = requestEvent.getRawPath();
			String httpMethod = (requestEvent.getRequestContext() != null && requestEvent.getRequestContext().getHttp().getMethod() != null) ?
					requestEvent.getRequestContext().getHttp().getMethod() : null;

			// Retrieve weather data
			 // Berlin, Germany
			if ("/weather".equals(path) && "GET".equals(httpMethod)) {
				String weatherData = weatherService.getWeatherData(52.52, 13.41);
				return builder.withStatusCode(200).withBody(weatherData).build();
			} else {
				String errorMessage = String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s", path, httpMethod);
				JSONObject responseBody = new JSONObject().put("statusCode", 400).put("message", errorMessage);
				return builder.withStatusCode(400).withBody(responseBody.toString()).build();
			}
		} catch (Exception e) {
			JSONObject responseBody = new JSONObject().put("error", "Internal Server Error");
			return builder.withStatusCode(500).withBody(responseBody.toString()).build();
		}

	}


}
