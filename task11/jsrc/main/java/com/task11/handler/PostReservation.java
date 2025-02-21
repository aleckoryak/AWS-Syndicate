package com.task11.handler;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

public class PostReservation implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private Table table;
    private Table reservationsTable;
    private LambdaLogger logger;

    public PostReservation(Table table, Table reservationsTable) {
        this.table = table;
        this.reservationsTable = reservationsTable;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            logger.log("PostReservation: " + requestEvent.getBody());
            logger.log("PostReservation: table:" + table.getTableName());

            JSONObject jsonRequest = new JSONObject(requestEvent.getBody());
            int tableNumber = jsonRequest.getInt("tableNumber");
            String clientName = jsonRequest.getString("clientName");
            String phoneNumber = jsonRequest.getString("phoneNumber");
            String date = jsonRequest.getString("date");
            String slotTimeStart = jsonRequest.getString("slotTimeStart");
            String slotTimeEnd = jsonRequest.getString("slotTimeEnd");

            // Validate input fields
            logger.log("PostReservation: validate input fields");
            if (!isValidPhoneNumber(phoneNumber) || !isValidDate(date) || !isValidTime(slotTimeStart) || !isValidTime(slotTimeEnd)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(SC_BAD_REQUEST)
                        .withBody("{\"error\": \"Invalid input data provided.\"}");
            }

            // Check for table exists
            logger.log("PostReservation: validate table exists");
            GetItemSpec isExistSpec = new GetItemSpec().withPrimaryKey("id", tableNumber);
            Item isExistItem = table.getItem(isExistSpec);

            if (isExistItem == null) {
                JSONObject responseBody = new JSONObject().put("error", "Table not found with ID: " + tableNumber);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(SC_BAD_REQUEST)
                        .withBody(responseBody.toString());
            }

            // Check for conflicting reservations
            logger.log("PostReservation: validate reservation");
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("#tableNum = :v_num and #resDate = :v_date")
                    .withNameMap(new NameMap()
                            .with("#tableNum", "tableNumber")
                            .with("#resDate", "date"))
                    .withValueMap(new ValueMap()
                            .withNumber(":v_num", tableNumber)
                            .withString(":v_date", date));

            if (reservationsTable.scan(scanSpec).iterator().hasNext()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(SC_BAD_REQUEST)
                        .withBody("{\"error\": \"Conflicting reservation exists.\"}");
            }

            logger.log("PostReservation: cteate item to reserve");
            String reservationId = UUID.randomUUID().toString();

            Item item = new Item()
                    .withPrimaryKey("reservationId", reservationId)
                    .withNumber("tableNumber", tableNumber)
                    .withString("clientName", clientName)
                    .withString("phoneNumber", phoneNumber)
                    .withString("date", date)
                    .withString("slotTimeStart", slotTimeStart)
                    .withString("slotTimeEnd", slotTimeEnd);

            // Put the item into the DynamoDB table
            logger.log("PostReservation: put item to db");
            PutItemOutcome putItemOutcome = reservationsTable.putItem(item);
            logger.log("PostReservation: putItemOutcome" + putItemOutcome);

            // Prepare API Gateway response
            JSONObject responseBody = new JSONObject().put("reservationId", reservationId);
            return new APIGatewayProxyResponseEvent().withStatusCode(SC_OK).withBody(responseBody.toString());

        } catch (Exception e) {
            logger.log("PostReservation: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\+?[0-9. ()-]{10,25}");
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            format.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        return time != null && time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }
}
