# task02
The Goal Of This Task is...
1 To deploy a Lambda function with a configured Function URL and ensure that accessing the /hello GET resource returns the following response:
#### Expected response
```buildoutcfg
{
    "statusCode": 200,
    "message": "Hello from Lambda"
}
```
2 Additionally, configure the Lambda function to return a 400 Bad Request error for any requests to endpoints other than /hello GET. This includes handling requests to endpoints like /student_id or any other unspecified endpoints.

#### Error response
```buildoutcfg
{
    "statusCode": 400,
    "message": "Bad request syntax or unsupported method. Request path: {path}. HTTP method: {method}"
}
```
Where {path} is the endpoint the request was made to, and {method} is the HTTP method used.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task02 diagram

[//]: # (![task02]&#40;pics/task02_diagram.png&#41;)

## Lambdas descriptions

### Lambda `lambda-name`
Lambda feature overview.

### Required configuration
#### Environment variables
* environment_variable_name: description

#### Trigger event
```buildoutcfg
{
    "key": "value",
    "key1": "value1",
    "key2": "value3"
}
```
* key: [Required] description of key
* key1: description of key1


---

## Deployment from scratch
cd ./aws_deep_dive/AWS-Syndicate
syndicate generate project --name task02
cd ./task02

syndicate generate config --name "dev"  .....


setx SDCT_CONF c:\projects\aws_deep_dive\AWS-Syndicate\task02\.syndicate-config-dev
OR
setx SDCT_CONF "C:\projects\aws_deep_dive\AWS-Syndicate\task02\.syndicate-config-dev"
echo $env:SDCT_CONF



syndicate generate lambda --name hello_world  --runtime java
update code

syndicate create_deploy_target_bucket

syndicate build

https://aleckoryak:{TOKEN}@github.com/aleckoryak/AWS-Syndicate.git

syndicate deploy

syndicate clean

