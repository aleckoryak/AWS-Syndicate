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