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
            logger.log("PostSignUpHandler: " + requestEvent.getBody());
            logger.log("PostSignUpHandler: userPoolId:" + userPoolId);
            logger.log("PostSignUpHandler: clientId:" + clientId);

            SignUp signUp = SignUp.fromJson(requestEvent.getBody());
            AdminCreateUserResponse adminCreateUserResponse = signUp(signUp);
            logger.log("PostSignUpHandler: adminCreateUserResponse:" + adminCreateUserResponse);
            AdminRespondToAuthChallengeResponse adminRespondToAuthChallengeResponse = confirmSignUp(signUp);
            logger.log("PostSignUpHandler: adminRespondToAuthChallengeResponse:" + adminRespondToAuthChallengeResponse);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_OK).withBody("");
        } catch (Exception e) {
            logger.log("PostSignUpHandler: Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_BAD_REQUEST)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }


    protected AdminCreateUserResponse signUp(SignUp signUpRequest) {
        return cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(signUpRequest.email())
                .temporaryPassword(signUpRequest.password())
                .userAttributes(
                        AttributeType.builder()
                                .name("given_name")
                                .value(signUpRequest.firstName())
                                .build(),
                        AttributeType.builder()
                                .name("family_name")
                                .value(signUpRequest.lastName())
                                .build(),
                        AttributeType.builder()
                                .name("email")
                                .value(signUpRequest.email())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build())
                .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                .messageAction("SUPPRESS")
                .forceAliasCreation(Boolean.FALSE)
                .build());
    }

    protected AdminInitiateAuthResponse signIn(String email, String password) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", email);
        authParams.put("PASSWORD", password);
        return cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .build());
    }

    protected AdminRespondToAuthChallengeResponse confirmSignUp(SignUp signUpRequest) {
        AdminInitiateAuthResponse adminInitiateAuthResponse = signIn(signUpRequest.email(), signUpRequest.password());

        if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(adminInitiateAuthResponse.challengeNameAsString())) {
            throw new RuntimeException("unexpected challenge: " + adminInitiateAuthResponse.challengeNameAsString());
        }

        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("USERNAME", signUpRequest.email());
        challengeResponses.put("PASSWORD", signUpRequest.password());
        challengeResponses.put("NEW_PASSWORD", signUpRequest.password());

        return cognitoClient.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .challengeResponses(challengeResponses)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .session(adminInitiateAuthResponse.session())
                .build());
    }
}
