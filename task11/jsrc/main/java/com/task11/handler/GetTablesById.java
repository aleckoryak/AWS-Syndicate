package com.task11.handler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

public class GetTablesById implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final Table table;
    private LambdaLogger logger;

    public GetTablesById(Table table) {
        this.table = table;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            logger.log("GetTablesById: " + requestEvent.getBody());
            logger.log("GetTablesById: table:" + table.getTableName());
            String tableId = requestEvent.getPathParameters().get("tableId");
            logger.log("GetTablesById: tableId:" + tableId);

            // Fetch the item by tableId
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("id", Integer.parseInt(tableId));
            Item item = table.getItem(spec);

            if (item == null) {
                JSONObject responseBody = new JSONObject().put("error", "Table not found with ID: " + tableId);
                return new APIGatewayProxyResponseEvent().withStatusCode(SC_BAD_REQUEST).withBody(responseBody.toString());
            }

            // Create a response object
            JSONObject responseBody = new JSONObject();
            responseBody.put("id", item.getInt("id"));
            responseBody.put("number", item.getInt("number"));
            responseBody.put("places", item.getInt("places"));
            responseBody.put("isVip", item.getBoolean("isVip"));
            if (item.isPresent("minOrder")) {
                responseBody.put("minOrder", item.getInt("minOrder"));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(responseBody.toString());

        } catch (NumberFormatException e) {
            logger.log("GetTablesById: Error parsing tableId: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", "Invalid table ID format").toString());
        } catch (Exception e) {
            logger.log("GetTablesById: Failed to get item: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", "Failed to get table: " + e.getMessage()).toString());
        }
    }
}
