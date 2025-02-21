package com.task12.handler;

import com.amazonaws.services.dynamodbv2.document.Item;
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

public class GetReservation implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private Table reservation;
    private LambdaLogger logger;

    public GetReservation(Table reservation) {
        this.reservation = reservation;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            logger.log("GetReservation: " + requestEvent.getBody());
            logger.log("GetReservation: table:" + reservation.getTableName());


            ScanSpec scanSpec = new ScanSpec();
            Iterator<Item> items = reservation.scan(scanSpec).iterator();

            // Prepare JSON response body
            JSONArray reservationsArray = new JSONArray();

            while (items.hasNext()) {
                Item item = items.next();
                logger.log("GetReservation: item:" + item);
                JSONObject reservationObject = new JSONObject();
                reservationObject.put("tableNumber", item.getInt("tableNumber"));
                reservationObject.put("clientName", item.getString("clientName"));
                reservationObject.put("phoneNumber", item.getString("phoneNumber"));
                reservationObject.put("date", item.getString("date"));
                reservationObject.put("slotTimeStart", item.getString("slotTimeStart"));
                reservationObject.put("slotTimeEnd", item.getString("slotTimeEnd"));

                reservationsArray.put(reservationObject);
            }

            JSONObject responseBody = new JSONObject();
            responseBody.put("reservations", reservationsArray);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK)
                    .withBody(responseBody.toString());

        } catch (Exception e) {
            logger.log("GetReservation: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}
