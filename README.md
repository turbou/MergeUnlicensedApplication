# UnlicensedAppMerge

## Prerequisite
- Java(JDK) v17
- Groovy v4.0.22

## Usage
```bash
groovy UAM.groovy -p XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX -t BOS
```
(XXXX stands for your application's <parent_id>)
- `-c, --confirm` Display a confirmation prompt before proceeding with the merge.
- `-p, --parent` Parent Appication ID(XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX)
- `-t, --tag` Filter child applications to be merged by tag.

## Description
Contrast PS UAM(UnlicensedAppMerge) script merges whole unlicensed child applications onto <parent_id> that you set.

## Limitation
The script only aims at the same language\'s unlicensed child applications and excludes already-merged applications.  
The <parent_id> application must be licensed when you run a script.

## Preparation
Prepare four environment variables for your system. This script extracts them via System.getenv() inside the code.
Set variables properly, or change the code if necessary, at your own risk!

## Example of your environment variables
 CONTRAST_BASEURL: https://xxx.contrastsecurity.xxx/Contrast/  
 CONTRAST_AUTHORIZATION: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==  
 CONTRAST_API_KEY: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  
 CONTRAST_ORG_ID: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX  
