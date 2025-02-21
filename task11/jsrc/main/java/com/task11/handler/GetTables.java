package com.task11.handler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

public class GetTables implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private Table table;
    private LambdaLogger logger;

    public GetTables(Table table) {
        this.table = table;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            logger.log("GetTables: " + requestEvent.getBody());
            logger.log("GetTables: table:" + table.getTableName());


            ScanSpec scanSpec = new ScanSpec();
            Iterator<Item> items = table.scan(scanSpec).iterator();

            // Prepare JSON response body
            JSONArray tablesArray = new JSONArray();

            while (items.hasNext()) {
                Item item = items.next();
                logger.log("GetTables: item:" + item);
                JSONObject tableObject = new JSONObject();
                tableObject.put("id", item.getInt("id"));
                tableObject.put("number", item.getInt("number"));
                tableObject.put("places", item.getInt("places"));
                tableObject.put("isVip", item.getBoolean("isVip"));
                if (item.isPresent("minOrder")) {
                    tableObject.put("minOrder", item.getInt("minOrder"));
                }
                tablesArray.put(tableObject);
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("tables", tablesArray);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(responseBody.toString());

        } catch (Exception e) {
            logger.log("GetTables: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}
