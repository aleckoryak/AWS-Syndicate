package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "processor",
        roleName = "processor-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        tracingMode = TracingMode.Active,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@DependsOn(name = "Weather", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "table", value = "${target_table}")
}
)

public class Processor implements RequestHandler<Object, Map<String, Object>> {
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
    private final DynamoDbClient dbClient = DynamoDbClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.EU_CENTRAL_1) //set your region
            .build();

    public Map<String, Object> handleRequest(Object request, Context context) {
        LambdaLogger logger = context.getLogger();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            String weatherRaw = getWeatherData();
            JSONObject weatherJSON = new JSONObject(weatherRaw);
            storeWeatherData(weatherJSON);

            resultMap.put("statusCode", 200);
            resultMap.put("message", "ALL DONE");
        } catch (IOException e) {
            logger.log("Not able to get data from the Weather service:" + e.getMessage());
            resultMap.put("statusCode", 500);
            resultMap.put("error", "Not able to get data from the Weather service:" + e.getMessage());
        } catch (DynamoDbException e) {
            logger.log("Not able to store data to DB:" + e.getMessage());
            resultMap.put("statusCode", 500);
            resultMap.put("error", "ot able to store data to DB:" + e.getMessage());
        }
        return resultMap;
    }

    private String getWeatherData() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL);
            HttpResponse response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    private void storeWeatherData(JSONObject jsonObject) throws DynamoDbException {

        // Prepare item values
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", AttributeValue.builder()
                .s(UUID.randomUUID().toString())
                .build());
        JSONObject forecast = new JSONObject();
        forecast.put("elevation", jsonObject.getDouble("elevation"));
        forecast.put("generationtime_ms", jsonObject.getDouble("generationtime_ms"));
        forecast.put("hourly", jsonObject.getJSONObject("hourly").toString());
        forecast.put("hourly_units", jsonObject.getJSONObject("hourly_units").toString());
        forecast.put("latitude", jsonObject.getDouble("latitude"));
        forecast.put("longitude", jsonObject.getDouble("longitude"));
        forecast.put("timezone", jsonObject.getString("timezone"));
        forecast.put("timezone_abbreviation", jsonObject.getString("timezone_abbreviation"));
        forecast.put("utc_offset_seconds", jsonObject.getInt("utc_offset_seconds"));

        itemValues.put("forecast", AttributeValue.builder()
                .s(forecast.toString())
                .build());

        // Put the item into the DynamoDB table
        PutItemRequest request = PutItemRequest.builder()
                .tableName(System.getenv("table"))
                .item(itemValues)
                .build();

        dbClient.putItem(request);

    }
}
