package com.layer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class WeatherService {
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public String getWeatherData(double latitude, double longitude) throws IOException {
        String url = String.format("%s?latitude=%f&longitude=%f&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m",
                BASE_URL, latitude, longitude);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            String json = EntityUtils.toString(response.getEntity());
            return json; // Raw JSON string for processing or return
        }
    }
}
