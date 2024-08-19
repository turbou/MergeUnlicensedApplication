# UnlicensedAppMerge

## Prerequisite
- Java17
- Groovy v4.0.22

## Usage
```bash
groovy UAM.groovy XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
```
(XXXX stands for your <parent_id>)

## Description
Contrast PS UAM(UnlicensedAppMerge) script merges whole unlicensed child applications onto <parent_id> that you set.

## Limitation
The script only aims to same language\'s unlicensed child applications, and it excludes already-merged applications.\n' +

## Preparation
Prepare four environment variables for your system. This script extracts the environment variables via System.getenv() inside the code.
Set variables properly, or change the code if necessary, at your own risk!

## Example of your environment variables
 BASEURL: https://xxx.contrastsecurity.xxx/Contrast/
 ORG_ID: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
 API_KEY: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
 USERNAME: xxx@xxxx.xxx
 SERVICE_KEY: XXXXXXXXXXXXXXXX
 

