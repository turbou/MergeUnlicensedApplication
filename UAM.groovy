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
import groovy.transform.Field
import java.util.stream.Collectors

@Command(
    name = 'UAM',
    mixinStandardHelpOptions = true,
    version = 'UAM 0.1',
    usageHelpAutoWidth = true,
    description = 'Contrast PS UAM(UnlicensedAppMerge) script merges whole unlicensed child applications onto <parent_id> that you set.\n' +
                  'Limitation: The script only aims to same language\'s unlicensed child applications, and it excludes already-merged applications.\n' +
                  'Preparation: Prepare four environment variables for your system. This script extracts the environment variables via the code below.\n\n' +
                  'Set variables properly, or change the code if necessary, at your own risk!\n' +
                  '  os.environ["CONTRAST_BASEURL"]\n' +
                  '  os.environ["CONTRAST_ORG_ID"]\n' +
                  '  os.environ["CONTRAST_API_KEY"]\n' +
                  '  os.environ["CONTRAST_USERNAME"]\n' +
                  '  os.environ["CONTRAST_SERVICE_KEY"]\n'
)
@picocli.groovy.PicocliScript

@Option(names = ["-t", "--test"], description = "If you set this option, You can check a list of merge target applications without performing the merge process.")
@Field boolean test = false

@Parameters(index = '0', description = 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX')
@Field String parent_id

class Config {
    static BASEURL     = System.getenv().CONTRAST_BASEURL
    static ORG_ID      = System.getenv().CONTRAST_ORG_ID
    static API_KEY     = System.getenv().CONTRAST_API_KEY
    static USERNAME    = System.getenv().CONTRAST_USERNAME
    static SERVICE_KEY = System.getenv().CONTRAST_SERVICE_KEY
}

def authHeader = "${Config.USERNAME}:${Config.SERVICE_KEY}".bytes.encodeBase64().toString()
def headers = []
headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"))
headers.add(new BasicHeader("API-Key", Config.API_KEY))
headers.add(new BasicHeader(HttpHeaders.AUTHORIZATION, authHeader))

def clientBuilder = new OkHttpClient.Builder()
def httpClient = clientBuilder.build()
def requestBuilder = new Request.Builder().url("${Config.BASEURL}/api/ng/${Config.ORG_ID}/applications/?expand=license,skip_links").get()
for (Header header : headers) {
    requestBuilder.addHeader(header.getName(), header.getValue())
}
def request = requestBuilder.build()
def Response response = httpClient.newCall(request).execute()

def jsonParser = new JsonSlurper()
def resBody = response.body().string()
def appsJson = jsonParser.parseText(resBody)
if (response.code() != 200) {
    println "Failed to get the application list."
    println "${resBody}"
    System.exit(1)
}

def parentAppName = null
def targetMap = [:]
def targetChildApps = []

appsJson.applications.each{app ->
    if (app.app_id == parent_id) {
        if (app.license.level == 'Licensed') {
            parentAppName = app.name
        }
    }
    if (!app.master && app.parentApplicationId == null && !app.archived && app.license.level == 'Unlicensed') {
        targetChildApps.add([appName: app.name, appId: app.app_id])
    }
}

if (parentAppName == null) {
    println "Parent application cannot be found or is not licensed."
    System.exit(2)
}

if (targetChildApps.empty) {
    println "There are no applications to merge."
    System.exit(0)
}

println "Parent application name is ${parentAppName}."
println "Merge target applications count is ${targetChildApps.size()}."
println "----------------------------------------"
targetChildApps.each{app ->
    println "  ${app.appName}"
}
println "----------------------------------------"

if (test) {
    println "Exit without merging for test execution."
    System.exit(0)
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

