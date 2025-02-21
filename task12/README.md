# task10 Serverless API + Cognito Integration + Open API Specification + Documenting

To deploy a serverless API with the specified resources using AWS Lambda, DynamoDB for data storage, and Amazon Cognito for user authentication. The task is to create the API service for Tables Booking at your favorite restaurant. Open API Specification v3, document it, and deploy the Swagger UI for the API.
##  diagram
![diagram](img.png)

### Resources Names
+ Lambda Function: `api_handler` | lambdas_alias_name: learn
+ Cognito UserPool: `simple-booking-userpool` | booking_userpool: simple-booking-userpool
+ API Gateway:
  + API Name: task12_api | Stage Name: api
  + /signup POST 
  + /signin POST 
  + /tables POST 
  + /tables GET 
  + /reservations POST 
  + /reservations GET

+ DynamoDB Table
  + Tables | reservations_table: Reservations
  + Reservations | reservations_table: Reservations

### Additional resources
+ https://github.com/epam/aws-syndicate/tree/master/examples/java/demo-apigateway-cognito

business goals, desision making. fr, nfr
45 o2 15.00
AWS User Pool tokens have different roles: the identity token (ID token) authenticates users to resource servers, and the access token authorizes API operations. For example, use the ID token to call an API with Cognito as the authorizer in AWS API Gateway, and the access token to allow users to modify attributes; their headers are similar, but they use different keys. Essentially, in the case of Cognito the ID token should be used as value of accessToken in the /signin response.

## Example
1.   `/signup` POST
```json
{
  "firstName": // string
  "lastName": // string
  "email": // email validation
  "password": // alphanumeric + any of "$%^*-_", 12+ chars
}
```
+ Response:
+ STATUS CODE:
  + 200 OK (Sign-up process is successful)
  + 400 Bad Request (There was an error in the request.)
+ Use software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest.builder() with the messageAction("SUPPRESS") method.

2. `/signin` POST
+ request
```json
{
         "email": // email
         "password": // alphanumeric + any of "$%^*", 12+ chars
     }
```
+ response
```json
{
         "accessToken": // string
     }
```
+ STATUS CODE:
  + 200 OK (The request has succeeded. The server has processed the sign-in request, and the provided credentials (email and password) were valid. The response contains an $access token, which will be used for subsequent authenticated requests.)
  + 400 Bad Request (There was an error in the request.)
---
3. `/tables` GET
+ Headers:
  + Authorization: Bearer $accessToken
+ Request: {}
+ Response:
```json
{
         "tables": [
             {
                 "id": // int
                 "number": // int, number of the table
                 "places": // int, amount of people to sit at the table
                 "isVip": // boolean, is the table in the VIP hall
                 "minOrder": // optional. int, table deposit required to book it
             },
             ...
         ]
     }
```
+ STATUS CODE:
  + 200 OK (The request has succeeded. The server has processed the request and has returned a list of tables as specified in the response body. Each table includes details such as its ID, number, capacity, whether it's in the VIP hall, and optionally, the minimum order required to book it.)
  + 400 Bad Request (There was an error in the request.)

4. /tables POST

+ Headers:
  + Authorization: Bearer $accessToken
+ Request:
```json
{
         "id": // int
         "number": // int, number of the table
         "places": // int, amount of people to sit at the table
         "isVip": // boolean, is the table in the VIP hall
         "minOrder": // optional. int, table deposit required to book it
     }
```
+ Response:
```json 
{
  "id": $table_id // int, id of the created table
}
```
+ STATUS CODE:
  + 200 OK (The request has succeeded. The server has successfully created a new table based on the information provided in the request body. The response contains the ID of the newly created table.)
  + 400 Bad Request (There was an error in the request.)

5. `/tables/{tableId}` GET

+ Headers:
  + Authorization: Bearer $accessToken
+ Request: {}
+ Response:
```json
{
         "id": // int
         "number": // int, number of the table
         "places": // int, amount of people to sit at the table
         "isVip": // boolean, is the table in the VIP hall
         "minOrder": // optional. int, table deposit required to book it
     }
```
+ STATUS CODE:
  + 200 OK (The request has succeeded. The server has processed the request and has returned information about the table specified by {tableId}. The response body contains details such as the table's ID, number, capacity, whether it's in the VIP hall, and optionally, the minimum order required to book it.)
  + 400 Bad Request (There was an error in the request.)

6. `/reservations` POST
+ Headers:
  + Authorization: Bearer $accessToken
+ Request:
```json 
{
         "tableNumber": // int, number of the table
         "clientName": //string
         "phoneNumber": //string
         "date": // string in yyyy-MM-dd format
         "slotTimeStart": // string in "HH:MM" format, like "13:00",
         "slotTimeEnd": // string in "HH:MM" format, like "15:00"
     }
```
+ Response:
```json
{
         "reservationId": // string uuidv4
     }
```
+ STATUS CODE:
  + 200 OK (The reservation was successfully created. The server has processed the request, and a new reservation has been successfully added to the system.)
  + 400 Bad Request (There was an error in the request. Possible reasons include invalid input, table not found, or conflicting reservations.)

7. `/reservations` GET
+ Headers:
  + Authorization: Bearer $accessToken
+ Request: {}
+ Response:
```json
{
         "reservations": [
             {
                 "tableNumber": // int, number of the table
                 "clientName": //string
                 "phoneNumber": //string
                 "date": // string in yyyy-MM-dd format
                 "slotTimeStart": // string in "HH:MM" format, like "13:00",
                 "slotTimeEnd": // string in "HH:MM" format, like "15:00"
             }
         ]
     }
```
+ STATUS CODE:
  + 200 OK (The request has succeeded. The server has provided a list of reservations as specified in the response body.)
  + 400 Bad Request (There was an error in the request.)

## Deployment from scratch
1. Generate Project:

Use aws-syndicate to [generate a new project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#221-creating-project-files). This will set up the basic structure needed for your Lambda deployment.
```powershell
syndicate generate project --name task12
```
2. Generate Config:

+ Navigate to task05 folder
```powershell
cd .\task12\
```
* Use aws-syndicate to generate a [config for your project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#222-creating-configuration-files-for-environment3. ).
  This will set up configuration files syndicate.yml and syndicate_aliases.yml that may be edited later.
```powershell
syndicate generate config --name "dev" `
    --region "eu-central-1" `
    --bundle_bucket_name "syndicate-education-platform-custom-sandbox-artifacts-sbox02/2fa561ce/task08" `
    --prefix "cmtr-2fa561ce-" `
    --extended_prefix "true" `
    --tags "course_id:SEP_GL_7,course_type:stm,student_id:2fa561ce,type:student" `
    --iam_permissions_boundary "arn:aws:iam::905418349556:policy/eo_role_boundary" `
    --access_key "ACCESS_KEY" `
    --secret_key "SECRET_KEY" `
    --session_token "SESSION_TOKEN"
```

* Set up the SDCT_CONF environment variable pointing to the folder with syndicate.yml file.
```powershell
  $env:SDCT_CONF = "C:\projects\aws_deep_dive\AWS-Syndicate\task12\.syndicate-config-dev"
  echo $env:SDCT_CONF
```
3. Generate ' processor' Lambda Function:

Inside your project, use aws-syndicate to [generate a Lambda function](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#224-creating-lambda-files). This step will create the necessary files and configurations
```powershell
syndicate generate lambda --name  api_handler  --runtime java
```
4. Generate Cognito Metadata
   Inside your project, use aws-syndicate to [generate a Cognito Metadata](https://github.com/epam/aws-syndicate/wiki/4.-Resources-Meta-Descriptions#414-cognito)
```powershell
syndicate generate meta cognito_user_pool --resource_name simple-booking-userpool
```
5. Generate API Gateway Metadata
   Use aws-syndicate to [generate API Gateway metadata](https://github.com/epam/aws-syndicate/wiki/4.-Resources-Meta-Descriptions#45-api-gateway) that includes the required API resources:
```powershell
syndicate generate meta api_gateway --resource_name task12_api --deploy_stage api
```
+ Generate API Gateway authorizer metadata
```powershell
syndicate generate meta api_gateway_authorizer --api_name task12_api --name task12_api_gateway_authorizer --type COGNITO_USER_POOLS --provider_name simple-booking-userpool
```
+ Generate API Gateway resources(paths) metadata
```powershell
syndicate generate meta api_gateway_resource --api_name task12_api --path signup --enable_cors false
syndicate generate meta api_gateway_resource --api_name task12_api --path signin --enable_cors false
syndicate generate meta api_gateway_resource --api_name task12_api --path tables --enable_cors false
syndicate generate meta api_gateway_resource --api_name task12_api --path "tables/{tableId}" --enable_cors false
syndicate generate meta api_gateway_resource --api_name task12_api --path reservations  --enable_cors false

```
+ Generate metadata for API Gateway resources methods
```powershell
syndicate generate meta api_gateway_resource_method --api_name task12_api --path signup --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task12_api --path signin --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1

syndicate generate meta api_gateway_resource_method --api_name task12_api --path tables --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task12_api --path tables --method GET --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task12_api --path "tables/{tableId}" --method GET --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1

syndicate generate meta api_gateway_resource_method --api_name task12_api --path reservations --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task12_api --path reservations --method GET --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
```
!!! in addition set "enable_proxy": true, to all generated methods

4. Generate DynamoDB Metadata
   Use aws-syndicate to [generate metadata for a DynamoDB](https://github.com/epam/aws-syndicate/wiki/4.-Resources-Meta-Descriptions#421-dynamo-db-table) table named 'Tables' with int id and Reservations with string(uuid) reservationId.
```powershell
syndicate generate meta dynamodb --resource_name Tables --hash_key_name id --hash_key_type N
syndicate generate meta dynamodb --resource_name Reservations --hash_key_name reservationId --hash_key_type S
```

5. Implement the Logic of the Function:

---

### Build and Deploy Project with the Syndicate Tool:

Use the aws-syndicate tool to [build and deploy your project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#231-create-an-s3-bucket-for-aws-syndicate-artifacts). This step packages and deploys your Lambda function along with the configured API Gateway.
```powershell
syndicate create_deploy_target_bucket
```

+ [Build](https://videoportal.epam.com/video/qYLn4xd7) the artifacts of the application and create a bundle:
```powershell
syndicate build -F -b task12_250221.120718
```
+ [Deploy](https://videoportal.epam.com/video/AaZWOPjY) the bundle:
```powershell
syndicate deploy --replace_output -b task12_250221.120718 -types lambda

syndicate update  --replace_output --force -b task12_250221.120718 -types lambda
```

---

### Verification
```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/signup' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "alec",
      "lastName": "kor",
      "email": "alec@gmail.com",
      "password": "QaZ!12345677"
}'
```

```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/signin' \
--header 'Content-Type: application/json' \
--data-raw '{
      "email": "alec@gmail.com",
      "password": "QaZ!12345677"
}'
```
```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/tables' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJraWQiOiJ0d0VJc2xsYzVMQ1NRaDJYdjNYUnhKXC9abmVkQ0NHN2VhTU4rNlh5TDNGWT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3Mzc0YTg1Mi1iMDgxLTcwZTctNjcxNi02ZDk4YmZkYTk5YzciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfamVXUUNNNGlXIiwiY29nbml0bzp1c2VybmFtZSI6ImFsZWNAZ21haWwuY29tIiwiZ2l2ZW5fbmFtZSI6ImFsZWMiLCJvcmlnaW5fanRpIjoiODI3N2M1YTQtZjIyNS00ZmNkLWFiZGQtMDFmN2Q5OTFlMjhlIiwiYXVkIjoiNGYwNGlya2JvbGw3cXBvamxwbjhoNzAzYnAiLCJldmVudF9pZCI6ImIwOTM0MTA0LTcwOTMtNDRhOS05MWNmLTZhYzg4MWI3MjE1ZCIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzQwMTA4NDg0LCJleHAiOjE3NDAxMTIwODQsImlhdCI6MTc0MDEwODQ4NCwiZmFtaWx5X25hbWUiOiJrb3IiLCJqdGkiOiI5ZmJiN2ZkNC01NDI1LTQ2MzctYjU4OS03N2RhNGQ3NGZiZDgiLCJlbWFpbCI6ImFsZWNAZ21haWwuY29tIn0.Tj17jq6mcQyO9l0cn8pSqBMdh_dCGK2M5Ui_V-wosA04yGhRfOWHRf1JR-a6e2_wStKAa7jG3cyVbuaeCrSe8_69GdJ7cfZvjMEW2USjIq0mQiV2XffP0O7nF2B5H7Ij7QE4jo8-meZQKYToI7ryCRRyGmHTeXaPitdxRRWSDOfIzED59-cBhloLzj366GBM26zQoGdpWbgaXHFUbY0tmXK4uXJDCk3ufsHwi2PQWVwve6VEGFQ4BpgqselLMgHOJt0MrnTzF6K9EJlliIxghM1V5gP418LpO6QpJHuzLRYzCtd2WWIERDR3ULrgrw2L4RQSlHoXIDzdAsojMa_qEA' \
--data '{
         "id": 2,
         "number": 2,
         "places": 4,
         "isVip": false,
         "minOrder": 1
     }'
```

```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/tables' \
--header 'Authorization: Bearer eyJraWQiOiJveThkVFVKNWhJamJZa3V2eEJZVHkyUkFDZFpNQytuOUd6UDhiVUVYRlZnPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiI0M2M0NzhlMi0zMGYxLTcwMzgtMGUwNS00NmVkN2ZjZWVlZmQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfb3Z4dkJ2VkFwIiwiY29nbml0bzp1c2VybmFtZSI6ImFsZWNAZ21haWwuY29tIiwiZ2l2ZW5fbmFtZSI6ImFsZWMiLCJvcmlnaW5fanRpIjoiN2Q5NWYzMzgtYTJmOC00NjBkLWJjNDItYzc4NWI0YTliOTJlIiwiYXVkIjoiM3Y1djBjaGI3dHQ0Y3MyZWJjYTNqczdmcW8iLCJldmVudF9pZCI6ImYwMGFmM2NhLThkYmEtNDEzNS04MjI0LTljYjFjMDZhNjkzNiIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzQwMTA3MzQ3LCJleHAiOjE3NDAxMTA5NDcsImlhdCI6MTc0MDEwNzM0NywiZmFtaWx5X25hbWUiOiJrb3IiLCJqdGkiOiJmYjlhOTQ4Yy0xODIxLTRiNjktODNkYy1kMTQyNTk2ZDM3YTAiLCJlbWFpbCI6ImFsZWNAZ21haWwuY29tIn0.BNAFOR_npNC0nPLm0ohMSLGJzeCkpmqrc8Ntm_7SeVmB_XPfx1KkYKqKg-nh8xnUezYwYonGubzzWO7q2qDyJl3OKkenxedeJr0ipgv1R9SWk9iMhogkg1hPIa1_EjaQEJuchjNOJ38rza9t7xwsB4TqQBwTHnq7g3fpcxAZLHTPuTSTzmpmepWGv-OYI2zWoMgSQyyMB5ALT1HwSv-Tu46MJXfSGvRLkIUqRYV2iNz341Z9S5wI9rqMIlSOkrTMpVh2ibyy1W8-GkGvj_IIbbD_bPDdr0XTgBkKBFF6hVBojDCj9aOXKvmg2Sma8sSc_UDl_hO6osPy5Ab2PWIYcQ'
```

```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/tables/2/' \
--header 'Authorization: Bearer eyJraWQiOiJ0d0VJc2xsYzVMQ1NRaDJYdjNYUnhKXC9abmVkQ0NHN2VhTU4rNlh5TDNGWT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3Mzc0YTg1Mi1iMDgxLTcwZTctNjcxNi02ZDk4YmZkYTk5YzciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfamVXUUNNNGlXIiwiY29nbml0bzp1c2VybmFtZSI6ImFsZWNAZ21haWwuY29tIiwiZ2l2ZW5fbmFtZSI6ImFsZWMiLCJvcmlnaW5fanRpIjoiODI3N2M1YTQtZjIyNS00ZmNkLWFiZGQtMDFmN2Q5OTFlMjhlIiwiYXVkIjoiNGYwNGlya2JvbGw3cXBvamxwbjhoNzAzYnAiLCJldmVudF9pZCI6ImIwOTM0MTA0LTcwOTMtNDRhOS05MWNmLTZhYzg4MWI3MjE1ZCIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzQwMTA4NDg0LCJleHAiOjE3NDAxMTIwODQsImlhdCI6MTc0MDEwODQ4NCwiZmFtaWx5X25hbWUiOiJrb3IiLCJqdGkiOiI5ZmJiN2ZkNC01NDI1LTQ2MzctYjU4OS03N2RhNGQ3NGZiZDgiLCJlbWFpbCI6ImFsZWNAZ21haWwuY29tIn0.Tj17jq6mcQyO9l0cn8pSqBMdh_dCGK2M5Ui_V-wosA04yGhRfOWHRf1JR-a6e2_wStKAa7jG3cyVbuaeCrSe8_69GdJ7cfZvjMEW2USjIq0mQiV2XffP0O7nF2B5H7Ij7QE4jo8-meZQKYToI7ryCRRyGmHTeXaPitdxRRWSDOfIzED59-cBhloLzj366GBM26zQoGdpWbgaXHFUbY0tmXK4uXJDCk3ufsHwi2PQWVwve6VEGFQ4BpgqselLMgHOJt0MrnTzF6K9EJlliIxghM1V5gP418LpO6QpJHuzLRYzCtd2WWIERDR3ULrgrw2L4RQSlHoXIDzdAsojMa_qEA'
```

```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/reservations' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJraWQiOiJ0d0VJc2xsYzVMQ1NRaDJYdjNYUnhKXC9abmVkQ0NHN2VhTU4rNlh5TDNGWT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3Mzc0YTg1Mi1iMDgxLTcwZTctNjcxNi02ZDk4YmZkYTk5YzciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfamVXUUNNNGlXIiwiY29nbml0bzp1c2VybmFtZSI6ImFsZWNAZ21haWwuY29tIiwiZ2l2ZW5fbmFtZSI6ImFsZWMiLCJvcmlnaW5fanRpIjoiODI3N2M1YTQtZjIyNS00ZmNkLWFiZGQtMDFmN2Q5OTFlMjhlIiwiYXVkIjoiNGYwNGlya2JvbGw3cXBvamxwbjhoNzAzYnAiLCJldmVudF9pZCI6ImIwOTM0MTA0LTcwOTMtNDRhOS05MWNmLTZhYzg4MWI3MjE1ZCIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzQwMTA4NDg0LCJleHAiOjE3NDAxMTIwODQsImlhdCI6MTc0MDEwODQ4NCwiZmFtaWx5X25hbWUiOiJrb3IiLCJqdGkiOiI5ZmJiN2ZkNC01NDI1LTQ2MzctYjU4OS03N2RhNGQ3NGZiZDgiLCJlbWFpbCI6ImFsZWNAZ21haWwuY29tIn0.Tj17jq6mcQyO9l0cn8pSqBMdh_dCGK2M5Ui_V-wosA04yGhRfOWHRf1JR-a6e2_wStKAa7jG3cyVbuaeCrSe8_69GdJ7cfZvjMEW2USjIq0mQiV2XffP0O7nF2B5H7Ij7QE4jo8-meZQKYToI7ryCRRyGmHTeXaPitdxRRWSDOfIzED59-cBhloLzj366GBM26zQoGdpWbgaXHFUbY0tmXK4uXJDCk3ufsHwi2PQWVwve6VEGFQ4BpgqselLMgHOJt0MrnTzF6K9EJlliIxghM1V5gP418LpO6QpJHuzLRYzCtd2WWIERDR3ULrgrw2L4RQSlHoXIDzdAsojMa_qEA' \
--data '{
         "tableNumber": 2,
         "clientName": "alec",
         "phoneNumber": "+2-23456435",
         "date": "2025-04-01",
         "slotTimeStart": "13:00",
         "slotTimeEnd": "15:00"
     }'
```

```curl
curl --location 'https://ev97tabc68.execute-api.eu-central-1.amazonaws.com/api/reservations' \
--header 'Authorization: Bearer eyJraWQiOiJ0d0VJc2xsYzVMQ1NRaDJYdjNYUnhKXC9abmVkQ0NHN2VhTU4rNlh5TDNGWT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI3Mzc0YTg1Mi1iMDgxLTcwZTctNjcxNi02ZDk4YmZkYTk5YzciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tXC9ldS1jZW50cmFsLTFfamVXUUNNNGlXIiwiY29nbml0bzp1c2VybmFtZSI6ImFsZWNAZ21haWwuY29tIiwiZ2l2ZW5fbmFtZSI6ImFsZWMiLCJvcmlnaW5fanRpIjoiODI3N2M1YTQtZjIyNS00ZmNkLWFiZGQtMDFmN2Q5OTFlMjhlIiwiYXVkIjoiNGYwNGlya2JvbGw3cXBvamxwbjhoNzAzYnAiLCJldmVudF9pZCI6ImIwOTM0MTA0LTcwOTMtNDRhOS05MWNmLTZhYzg4MWI3MjE1ZCIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNzQwMTA4NDg0LCJleHAiOjE3NDAxMTIwODQsImlhdCI6MTc0MDEwODQ4NCwiZmFtaWx5X25hbWUiOiJrb3IiLCJqdGkiOiI5ZmJiN2ZkNC01NDI1LTQ2MzctYjU4OS03N2RhNGQ3NGZiZDgiLCJlbWFpbCI6ImFsZWNAZ21haWwuY29tIn0.Tj17jq6mcQyO9l0cn8pSqBMdh_dCGK2M5Ui_V-wosA04yGhRfOWHRf1JR-a6e2_wStKAa7jG3cyVbuaeCrSe8_69GdJ7cfZvjMEW2USjIq0mQiV2XffP0O7nF2B5H7Ij7QE4jo8-meZQKYToI7ryCRRyGmHTeXaPitdxRRWSDOfIzED59-cBhloLzj366GBM26zQoGdpWbgaXHFUbY0tmXK4uXJDCk3ufsHwi2PQWVwve6VEGFQ4BpgqselLMgHOJt0MrnTzF6K9EJlliIxghM1V5gP418LpO6QpJHuzLRYzCtd2WWIERDR3ULrgrw2L4RQSlHoXIDzdAsojMa_qEA'
```