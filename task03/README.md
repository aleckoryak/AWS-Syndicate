# task03

This task involves deploying a Lambda function along with API Gateway. The API Gateway will have a /hello GET resource with a specific request configuration and a predefined response when accessed.
he Goal Of This Task is...

To deploy a Lambda function and an API Gateway with the following API resources:

+ Resource: /hello
+ HTTP Method: GET
+ Request: Empty (no request payload)
+ Response:
```json
{
    "statusCode": 200,
    "message": "Hello from Lambda"
}
```

### Lambda 
+ lambdas_function_name: `hello_world`
+ lambdas_alias_name: `learn`
### API Gateway:
+ API Name: `task3_api`
+ Stage Name: `api`
### Resources:
+ `GET /hello` 


#### Expected response
```json
{
    "statusCode": 200,
    "message": "Hello from Lambda"
}
```
---

## Deployment from scratch
1. Generate Project:

Use aws-syndicate to [generate a new project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#221-creating-project-files). This will set up the basic structure needed for your Lambda deployment. 
```powershell
syndicate generate project --name task03
```
2. Generate Config:

+ Navigate to task03 folder
```powershell
cd .\task3\
```
* Use aws-syndicate to generate a [config for your project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#222-creating-configuration-files-for-environment3. ).
This will set up configuration files syndicate.yml and syndicate_aliases.yml that may be edited later.
```powershell
syndicate generate config --name "dev" `
  --region "eu-central-1" `
  --bundle_bucket_name "syndicate-education-platform-custom-sandbox-artifacts-sbox02/2fa561ce/task03" `
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
  $env:SDCT_CONF = "C:\projects\aws_deep_dive\AWS-Syndicate\task03\.syndicate-config-dev"
  echo $env:SDCT_CONF
```
3. Generate Lambda:

Inside your project, use aws-syndicate to [generate a Lambda function](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#224-creating-lambda-files). This step will create the necessary files and configurations
```powershell
syndicate generate lambda --name hello_world  --runtime java
```