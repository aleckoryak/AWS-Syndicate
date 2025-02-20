/*
 * Copyright 2024 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.task11.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.SignUp;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Created by Roman Ivanov on 7/20/2024.
 */
public class PostSignUpHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private LambdaLogger logger;

    private final String userPoolId = System.getenv("COGNITO_ID");
    private final String clientId = System.getenv("CLIENT_ID");
    private final CognitoIdentityProviderClient cognitoClient;

    public PostSignUpHandler(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        logger = context.getLogger();
        try {
            SignUp signUp = SignUp.fromJson(requestEvent.getBody());

            // sign up
            String userId = cognitoSignUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));

            logger.log(String.format("PostSignUpHandler: User has been successfully signed up. userId: %s", userId));

            Map<String, String> authParams = Map.of(
                    "USERNAME", signUp.email(),
                    "PASSWORD", signUp.password()
            );

            AdminInitiateAuthResponse adminInitiateAuthResponse = cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build());

            logger.log(String.format("PostSignUpHandler: idToken: %s accessToken: %s", adminInitiateAuthResponse.authenticationResult().idToken(),adminInitiateAuthResponse.authenticationResult().accessToken()));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK).withBody("");
//                    .withBody(new JSONObject()
//                            .put("message", "User has been successfully signed up.")
//                            .put("userId", userId)
//                            .toString());
        } catch (Exception e) {
            logger.log("PostSignUpHandler: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    protected AdminCreateUserResponse cognitoSignUp(SignUp signUp) {
        logger.log("userPoolId:" + userPoolId);
        logger.log("clientId:" + clientId);
        return cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(signUp.email())
                .temporaryPassword(signUp.password())
                .userAttributes(
                        AttributeType.builder()
                                .name("given_name")
                                .value(signUp.firstName())
                                .build(),
                        AttributeType.builder()
                                .name("family_name")
                                .value(signUp.lastName())
                                .build(),
                        AttributeType.builder()
                                .name("email")
                                .value(signUp.email())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .messageAction("SUPPRESS")
//                .forceAliasCreation(Boolean.FALSE)
                .build()
        );
    }
}
