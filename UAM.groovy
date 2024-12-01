@Grapes([
    @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.14'),
    @Grab(group='com.squareup.okhttp3', module='okhttp', version='4.12.0'),
    @Grab(group='com.squareup.okhttp3', module='okhttp-urlconnection', version='4.12.0'),
    @Grab(group='info.picocli', module='picocli-groovy', version='4.7.6'),
])

import static picocli.CommandLine.*
import org.apache.http.Header
import org.apache.http.HttpHeaders
import org.apache.http.message.BasicHeader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType
import okhttp3.RequestBody
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
import java.util.stream.Collectors

@Command(
    name = 'UAM',
    mixinStandardHelpOptions = true,
    version = 'UAM 0.1',
    usageHelpAutoWidth = true,
    description = 'Contrast PS UAM(UnlicensedAppMerge) script merges whole unlicensed child applications onto <parent_id> that you set.\n' +
                  'Limitation: The script only aims to same language\'s unlicensed child applications, and it excludes already-merged applications.\n' +
                  'Preparation: Prepare four environment variables for your system. This script extracts the environment variables from below parameters.\n\n' +
                  'Set variables properly, or change the code if necessary, at your own risk!\n' +
                  '  CONTRAST_BASEURL(e.g. https://xxx.contrastsecurity.xxx/Contrast)\n' +
                  '  CONTRAST_AUTHORIZATION\n' +
                  '  CONTRAST_API_KEY\n' +
                  '  CONTRAST_ORG_ID\n'
)
@picocli.groovy.PicocliScript

@Option(names = ["-c", "--confirm"], description = "Display a confirmation prompt before proceeding with the merge.")
@Field boolean confirm = false

@Option(names = ["-p", "--parent"], required = true, description = "Parent Appication ID(XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX)")
@Field String parent_id

@Option(names = ["-t", "--tag"], required = true, description = "Filter child applications to be merged by tag.")
@Field String child_tag

class Config {
    static BASEURL       = System.getenv().CONTRAST_BASEURL
    static AUTHORIZATION = System.getenv().CONTRAST_AUTHORIZATION
    static API_KEY       = System.getenv().CONTRAST_API_KEY
    static ORG_ID        = System.getenv().CONTRAST_ORG_ID
}

if (Config.BASEURL == null || Config.AUTHORIZATION == null || Config.API_KEY == null || Config.ORG_ID == null) {
    println "The required environment variable is not set."
    System.exit(1)
}

def authHeader = Config.AUTHORIZATION
def headers = []
headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"))
headers.add(new BasicHeader("API-Key", Config.API_KEY))
headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader))

def clientBuilder = new OkHttpClient.Builder()
def httpClient = clientBuilder.build()
def requestBuilder = new Request.Builder().url("${Config.BASEURL}/api/ng/${Config.ORG_ID}/applications/?expand=license,tags,skip_links").get()
for (Header header : headers) {
    requestBuilder.addHeader(header.getName(), header.getValue())
}
def request = requestBuilder.build()
def Response response = httpClient.newCall(request).execute()

def jsonParser = new JsonSlurper()
def resBody = response.body().string()
def appsJson = jsonParser.parseText(resBody)
//println JsonOutput.prettyPrint(JsonOutput.toJson(appsJson))
if (response.code() != 200) {
    println "Failed to get the application list."
    println "${resBody}"
    System.exit(1)
}

def parentAppName = null
def parentAppLang = null
def targetMap = [:]
def targetChildApps = []

appsJson.applications.each{app ->
    if (app.app_id == parent_id) {
        if (app.license.level == 'Licensed') {
            parentAppName = app.name
            parentAppLang = app.language
        }
    }
}

if (parentAppName == null) {
    println "Parent application cannot be found or is not licensed."
    System.exit(2)
}

appsJson.applications.each{app ->
    if (!app.master && app.parentApplicationId == null && !app.archived && app.license.level == 'Unlicensed' && app.language == parentAppLang) {
        if (app.tags.contains(child_tag)) {
            targetChildApps.add([appName: app.name, appId: app.app_id])
        }
    }
}

if (targetChildApps.empty) {
    println "There are no applications to merge."
    System.exit(0)
}

println "=================================================="
println "Parent application name is ${parentAppName}."
println "Merge target applications count is ${targetChildApps.size()}."
println " ----------------------------------------"
targetChildApps.each{app ->
    println "  ${app.appName}"
}
println " ----------------------------------------"
println "=================================================="

if (confirm) {
    def answer = System.console().readLine("Execute merge? (y/n): ")
    if (answer.toLowerCase() != 'y') {
        System.exit(0)
    }
}

def mediaTypeJson = MediaType.parse("application/json; charset=UTF-8")
def json = String.format("{\"apps\":[%s]}", targetChildApps.stream().map(app -> app.appId).collect(Collectors.joining("\",\"", "\"", "\"")))
def body = RequestBody.create(json, mediaTypeJson)

requestBuilder = new Request.Builder().url("${Config.BASEURL}/api/ng/${Config.ORG_ID}/modules/${parent_id}/merge").put(body)
for (Header header : headers) {
    requestBuilder.addHeader(header.getName(), header.getValue())
}
request = requestBuilder.build()
response = httpClient.newCall(request).execute()
resBody = response.body().string()
def resJson = jsonParser.parseText(resBody)
if (response.code() == 200) {
    println "The application merge was successful."
} else {
    println "The application merge failed."
    println "${resBody}"
    System.exit(3)
}

System.exit(0)

