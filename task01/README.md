# task01

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task01 diagram

[//]: # (![task01]&#40;pics/task01_diagram.png&#41;)

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

#### Expected response
```buildoutcfg
{
    "status": 200,
    "message": "Operation succeeded"
}
```
---

## Deployment from scratch
cd ./aws_deep_dive/AWS-Syndicate
syndicate generate project --name task01
cd ./task01

syndicate generate config --name "dev" ......


setx SDCT_CONF c:\projects\aws_deep_dive\AWS-Syndicate\task01\.syndicate-config-dev
OR 
setx SDCT_CONF "C:\projects\aws_deep_dive\AWS-Syndicate\task01\.syndicate-config-dev"
echo $env:SDCT_CONF
	
	
	
syndicate generate lambda --name hello_world  --runtime java
    update code
	
syndicate create_deploy_target_bucket
	
syndicate build
	
https://aleckoryak:{TOKEN}@github.com/aleckoryak/AWS-Syndicate.git
	
syndicate deploy
	
syndicate clean

