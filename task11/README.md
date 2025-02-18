# task10 Serverless API + Cognito Integration

To deploy a serverless API with the specified resources using AWS Lambda, DynamoDB for data storage, and Amazon Cognito for user authentication. The task is to create the API service for Tables Booking at your favorite restaurant.\
##  diagram

![diagram](img.png)

### Resources Names
+ Lambda Function: `api_handler` | lambdas_alias_name: learn
+ Cognito UserPool: `simple-booking-userpool` | booking_userpool: simple-booking-userpool
+ API Gateway:
  + API Name: task11_api | Stage Name: api
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
syndicate generate project --name task11
```
2. Generate Config:

+ Navigate to task05 folder
```powershell
cd .\task11\
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
  $env:SDCT_CONF = "C:\projects\aws_deep_dive\AWS-Syndicate\task11\.syndicate-config-dev"
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
syndicate generate meta api_gateway --resource_name task11_api --deploy_stage api
```
+ Generate API Gateway authorizer metadata
```powershell
syndicate generate meta api_gateway_authorizer --api_name task11_api --name task11_api_gateway_authorizer --type COGNITO_USER_POOLS --provider_name simple-booking-userpool
```
+ Generate API Gateway resources(paths) metadata
```powershell
syndicate generate meta api_gateway_resource --api_name task11_api --path signup --enable_cors false
syndicate generate meta api_gateway_resource --api_name task11_api --path signin --enable_cors false
syndicate generate meta api_gateway_resource --api_name task11_api --path tables --enable_cors false
syndicate generate meta api_gateway_resource --api_name task11_api --path reservations  --enable_cors false

```
+ Generate metadata for API Gateway resources methods
```powershell
syndicate generate meta api_gateway_resource_method --api_name task11_api --path signup --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task11_api --path signin --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1

syndicate generate meta api_gateway_resource_method --api_name task11_api --path tables --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task11_api --path tables --method GET --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1

syndicate generate meta api_gateway_resource_method --api_name task11_api --path reservations --method POST --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
syndicate generate meta api_gateway_resource_method --api_name task11_api --path reservations --method GET --integration_type lambda --lambda_name api_handler --lambda_region eu-central-1
```
!!! in addition set "enable_proxy": true, to all generated methods

4. Generate DynamoDB Metadata
   Use aws-syndicate to [generate metadata for a DynamoDB](https://github.com/epam/aws-syndicate/wiki/4.-Resources-Meta-Descriptions#421-dynamo-db-table) table named 'Weather'.
```powershell
syndicate generate meta dynamodb --resource_name Weather --hash_key_name id --hash_key_type S
```

5. Implement the Logic of the Function:

In the Lambda function code, implement the logic to pull the latest weather forecast from the Open-Meteo API and push it to DynamoDB.
**To get the weather forecast please use this [URL](
https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m)**

---

### Build and Deploy Project with the Syndicate Tool:

Use the aws-syndicate tool to [build and deploy your project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#231-create-an-s3-bucket-for-aws-syndicate-artifacts). This step packages and deploys your Lambda function along with the configured API Gateway.
```powershell
syndicate create_deploy_target_bucket
```

+ [Build](https://videoportal.epam.com/video/qYLn4xd7) the artifacts of the application and create a bundle:
```powershell
syndicate build -F -b task10_250217.122353
```
+ [Deploy](https://videoportal.epam.com/video/AaZWOPjY) the bundle:
```powershell
syndicate deploy --replace_output -b task10_250217.122353
```

---

### Verification
1. Check AWS Lambda Console:
+ Confirm that the Lambda function is listed in the AWS Lambda Console.
+ Verify that there are no deployment errors.

2. Check Lambda Layer Console:
+ Confirm that the Lambda Layer is listed in the AWS Lambda Console.
+ Verify that the SDK code is correctly organized and accessible.

3. API Client Request:

+ Use your chosen API client (Postman, Insomnia) to send a GET request to the function URL (/weather) of the Lambda function.
+ Verify that the response contains the latest weather forecast fetched using the SDK from the Lambda Layer.

4. CloudWatch Logs:

+ Check the CloudWatch Logs for the Lambda function to ensure there are no errors logged during the execution.
