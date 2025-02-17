# AWS-Syndicate

Deep Dive Into Serverless: AWS: AWS-Syndicate related tasks

videos related to the Syndicate
https://videoportal.epam.com/playlist/y769yAJ8
https://igyl7k4oi7frst4oig2hvf4j6e0nhsly.lambda-url.eu-central-1.on.aws/v1/home

syndicate git
https://github.com/epam/aws-syndicate

installation path 
1. python + aws-syndicate
https://chocolatey.org/install
https://docs.python-guide.org/starting/install3/win/

2. Installation from PyPI
   https://pypi.org/project/aws-syndicate/
### git clone 
   https://github.com/epam/aws-syndicate.git
### Create virtual environment:
```powershell  
python3 -m venv syndicate_venv
```   
### Activate your virtual environment:
```powershell 
syndicate_venv\Scripts\activate.bat
```
### Install Syndicate framework with pip from GitHub:
```powershell  
pip install aws-syndicate
```
### Set up a Syndicate Java [plugin](https://github.com/epam/aws-syndicate/tree/master/plugin "plugin"):
```powershell  
mvn install -f aws-syndicate/plugin/
```



## Deployment from scratch
1. Generate Project:

Use aws-syndicate to [generate a new project](https://github.com/epam/aws-syndicate/wiki/2.-Quick-start#221-creating-project-files). This will set up the basic structure needed for your Lambda deployment.
```powershell
syndicate generate project --name task09
```
2. Generate Config:

+ Navigate to task05 folder
```powershell
cd .\task09\
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
  $env:SDCT_CONF = "C:\projects\aws_deep_dive\AWS-Syndicate\task09\.syndicate-config-dev"
  echo $env:SDCT_CONF
```