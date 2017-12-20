/*

  This is an example of how to use the Azure Mobile Engagement Export API from end to end with many println
  This example is unsupported and must not be used in a production environment
  It is provided under the MIT Licence (attached)

*/

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import static groovyx.net.http.ContentType.*

///////////////////////////////////////////
// 00. Parse Command Line
///////////////////////////////////////////
def SCRIPT_PATH = getClass().protectionDomain.codeSource.location.path
def SCRIPT_FILE = SCRIPT_PATH.substring(SCRIPT_PATH.lastIndexOf("/") + 1)
def SCRIPT_DIR = SCRIPT_PATH.substring(0, SCRIPT_PATH.lastIndexOf("/") + 1)
def cli = new CliBuilder(usage: "$SCRIPT_FILE options [--help] [--localMode]", header: "with options:")
cli.h(longOpt: 'help', 'Print this message')
cli.t(longOpt: 'Token', required: false, args: 1, argName: 'Token', 'Token if you want to reuse the authentication token of a previous session')
cli.T(longOpt: 'Task', required: true, args: 1, argName: 'Task', 'Provide the task GUID')

/* Parse and check command line arguments */
def options = cli.parse(args)
if (!options)
  return

/* Print usage if required */
if (options.h) {
  cli.usage()
  return
}

def params = new ExportParams()
def token = options.Token
def task = options.Task

////////////////////////////////////////////////////////////
// 1. Obtain a OAuth token
////////////////////////////////////////////////////////////
String.metaClass.encodeURL = {
   java.net.URLEncoder.encode(delegate, "UTF-8")
}
def azmeAPIAuthGetTokenURL = "https://$params.azmeAPIAuthGetTokenHost/$params.azmeTenantId/oauth2/"
def azmeAPIAuthGetTokenBody = "grant_type=client_credentials&client_id=${params.azmeAPIAuthAADApplicationId.encodeURL()}&client_secret=${params.azmeAPIAuthAADSecret.encodeURL()}&resource=${'https://management.core.windows.net/'.encodeURL()}"

if(token == null || token ==false){
  println "Retrieving token from $azmeAPIAuthGetTokenURL ($azmeAPIAuthGetTokenBody)"
  def RESTClient restClientToken = new RESTClient(azmeAPIAuthGetTokenURL)
  def response = restClientToken.post(
    path:'token',
    requestContentType :'application/x-www-form-urlencoded',
    body:azmeAPIAuthGetTokenBody
  )

  token = response.data.access_token
  println "Token: $token"
}

////////////////////////////////////////////////////////////
// 2. Get Task Status
////////////////////////////////////////////////////////////
def apiRoot = "https://$params.apiHost/subscriptions/$params.azmeSubscriptionId/resourceGroups/MobileEngagement/providers/Microsoft.MobileEngagement/appcollections/$params.appCollection/apps/$params.app/devices/exportTasks"

println '\nGetting the task status'
def taskStatus = "$apiRoot/$task"
println taskStatus
def RESTClient restClientExportTaskStatus = new RESTClient(taskStatus)
restClientExportTaskStatus.defaultRequestHeaders['Authorization'] = "Bearer $token"
restClientExportTaskStatus.defaultRequestHeaders['Accept'] = "application/json"

def restClientExportTaskStatusResponse = restClientExportTaskStatus.get(
  query: ['api-version':'2014-12-01'],
  requestContentType : 'application/json;charset=UTF-8')
println restClientExportTaskStatusResponse.status
println restClientExportTaskStatusResponse.data
