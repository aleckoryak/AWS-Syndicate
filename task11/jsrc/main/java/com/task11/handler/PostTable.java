package com.task11.handler;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.SignUp;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

public class PostTable implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private Table table;
    private LambdaLogger logger;

    public PostTable(Table table) {
        this.table = table;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            logger.log("PostTable: " + requestEvent.getBody());
            logger.log("PostTable: table:" + table.getTableName());
            JSONObject json = new JSONObject(requestEvent.getBody());

            int id = json.getInt("id");
            int number = json.getInt("number");
            int places = json.getInt("places");
            boolean isVip = json.getBoolean("isVip");
            Integer minOrder = json.has("minOrder") ? json.getInt("minOrder") : null;

            // Check for table exists
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("id", number);
            Item isExistItem = table.getItem(spec);

            if (isExistItem != null) {
                JSONObject responseBody = new JSONObject().put("error", "Table with ID already exists: " + number);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(SC_BAD_REQUEST)
                        .withBody(responseBody.toString());
            }

            Item item = new Item()
                    .withPrimaryKey("id", id)
                    .withNumber("number", number)
                    .withNumber("places", places)
                    .withBoolean("isVip", isVip);

            if (minOrder != null) {
                item.withNumber("minOrder", minOrder);
            }

            // Put the item into the DynamoDB table
            PutItemOutcome putItemOutcome = table.putItem(item);
            context.getLogger().log("PostTable: putItemOutcome" + putItemOutcome);

            // Prepare API Gateway response
            JSONObject responseBody = new JSONObject().put("id", id);
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_OK).withBody(responseBody.toString());

        } catch (Exception e) {
            logger.log("PostTable: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}
