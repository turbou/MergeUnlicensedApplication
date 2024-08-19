# UnlicensedAppMerge

## Prerequisite
- Java(JDK) v17
- Groovy v4.0.22

## Usage
```bash
groovy UAM.groovy XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
```
(XXXX stands for your application's <parent_id>)

## Description
Contrast PS UAM(UnlicensedAppMerge) script merges whole unlicensed child applications onto <parent_id> that you set.

## Limitation
The script only aims at the same language\'s unlicensed child applications and excludes already-merged applications.  
The <parent_id> application must be licensed when you run a script.

## Preparation
Prepare four environment variables for your system. This script extracts them via System.getenv() inside the code.
Set variables properly, or change the code if necessary, at your own risk!

## Example of your environment variables
 BASEURL: https://xxx.contrastsecurity.xxx/Contrast/  
 ORG_ID: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX  
 API_KEY: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  
 USERNAME: xxx@xxxx.xxx  
 SERVICE_KEY: XXXXXXXXXXXXXXXX
 

