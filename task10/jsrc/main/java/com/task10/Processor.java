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
import java.util.stream.Collectors;

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
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());

        // Processing hourly JSON object as a map
        JSONObject hourlyJson = jsonObject.getJSONObject("hourly");
        Map<String, AttributeValue> hourlyMap = new HashMap<>();
        hourlyMap.put("time", AttributeValue.builder().s(hourlyJson.getJSONArray("time").toString()).build());
        hourlyMap.put("temperature_2m", AttributeValue.builder().n(hourlyJson.getJSONArray("temperature_2m").toString()).build());


        // Processing hourly_units JSON object as a map
        JSONObject hourlyUnitsJson = jsonObject.getJSONObject("hourly_units");
        Map<String, AttributeValue> hourlyUnitsMap = new HashMap<>();
        hourlyUnitsMap.put("time", AttributeValue.builder().s(hourlyUnitsJson.getString("time")).build());
        hourlyUnitsMap.put("temperature_2m", AttributeValue.builder().s(hourlyUnitsJson.getString("temperature_2m")).build());


        // Prepare other attributes
        Map<String, AttributeValue> forecastMap = new HashMap<>();
        forecastMap.put("elevation", AttributeValue.builder().n(String.valueOf(jsonObject.getDouble("elevation"))).build());
        forecastMap.put("generationtime_ms", AttributeValue.builder().n(String.valueOf(jsonObject.getDouble("generationtime_ms"))).build());
        forecastMap.put("latitude", AttributeValue.builder().n(String.valueOf(jsonObject.getDouble("latitude"))).build());
        forecastMap.put("longitude", AttributeValue.builder().n(String.valueOf(jsonObject.getDouble("longitude"))).build());
        forecastMap.put("timezone", AttributeValue.builder().s(jsonObject.getString("timezone")).build());
        forecastMap.put("timezone_abbreviation", AttributeValue.builder().s(jsonObject.getString("timezone_abbreviation")).build());
        forecastMap.put("utc_offset_seconds", AttributeValue.builder().n(String.valueOf(jsonObject.getInt("utc_offset_seconds"))).build());
        forecastMap.put("hourly", AttributeValue.builder().m(hourlyMap).build());
        forecastMap.put("hourly_units", AttributeValue.builder().m(hourlyUnitsMap).build());

        // Adding forecast map to item values
        itemValues.put("forecast", AttributeValue.builder().m(forecastMap).build());

        String tableName = System.getenv("table");
        if (tableName == null) {
            throw new IllegalStateException("Table name environment variable 'table' is not set");
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        dbClient.putItem(request);
        System.out.println("Item inserted successfully!");

    }
}


/*

        Map<String, AttributeValue> forecast = new HashMap<>();
        forecast.put("elevation", new AttributeValue().withN(String.valueOf(jsonObject.getDouble("elevation"))));
        forecast.put("generationtime_ms", new AttributeValue().withN(String.valueOf(jsonObject.getDouble("generationtime_ms"))));
        forecast.put("longitude", new AttributeValue().withN(String.valueOf(jsonObject.getDouble("longitude"))));
        forecast.put("latitude", new AttributeValue().withN(String.valueOf(jsonObject.getDouble("latitude"))));
        forecast.put("utc_offset_seconds", new AttributeValue().withN(String.valueOf(jsonObject.getDouble("utc_offset_seconds"))));
        forecast.put("timezone_abbreviation", new AttributeValue().withS(jsonObject.getString("timezone_abbreviation")));
        forecast.put("timezone", new AttributeValue().withS(String.valueOf("timezone")));

        Map<String, AttributeValue> hourly = new HashMap<>();
        jsonObject.getJSONObject("hourly").getJSONArray("time").forEach(time -> new AttributeValue().withS(time)).collect(Collectors.toList());
        jsonObject.getJSONObject("hourly").getJSONArray("temperature_2m").forEach(time -> new AttributeValue().withN(time)).collect(Collectors.toList());

        hourly.put("temperature_2m", jsonObject.getJSONObject("hourly").getJSONArray("time").forEach(time -> new AttributeValue().withS(time)).collect(Collectors.toList()));
        hourly.put("time", jsonObject.getJSONObject("hourly").getJSONArray("temperature_2m").forEach(time -> new AttributeValue().withN(time)).collect(Collectors.toList()));
        forecast.put("hourly", new AttributeValue().withM(hourly));

        Map<String, AttributeValue> hourlyUnits = new HashMap<>();

        hourlyUnits.put("temperature_2m", new AttributeValue().withS(jsonObject.getJSONObject("hourly_units").getString("temperature_2m")));
        hourlyUnits.put("time", new AttributeValue().withS(jsonObject.getJSONObject("hourly_units").getString("time")));

        forecast.put("hourly_units", new AttributeValue().withM(hourlyUnits));
        itemValues.put("forecast", new AttributeValue().withM(forecast));
*/