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
cli.T(longOpt: 'Type', required: true, args: 1, argName: 'Type', 'Type: use "token" to export tokens or "tag" to export tags')
cli.e(longOpt: 'Env', required: false, args: 1, argName: 'Env', 'enter \'win\' if you are on Windows. Don\'t use the option if you are on Mac or linux')

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
def extractType = options.Type
def execPrefix = options.Env != 'win' ? '' : 'cmd /c '

///////////////////////////////////////////
// 1. Create a SAS for the container
///////////////////////////////////////////
// Permission
def blobCreateSASPermissions = 'wl'

// Valid for 1 day
def blobCreateSASExpiry = (new Date() + 1).format("yyyy-MM-dd'T'HH:mm'Z'", TimeZone.getTimeZone('UTC'))
// The CLI Command
def blobCreateSASCommand = "az storage container generate-sas --account-name $params.azureStorageAccountName --account-key $params.azureStorageAccountKey --name $params.azureStorageContainerName --permissions $blobCreateSASPermissions --expiry $blobCreateSASExpiry"
// Now do the SAS creation
println "Executing $blobCreateSASCommand"
sasCreateProcess = "${execPrefix}$blobCreateSASCommand".execute()
sasCreateProcess.waitFor()
if (sasCreateProcess.exitValue()) {
    println "ERROR: " + sasCreateProcess.err.text
    return
} else {
    sasCreateProcessResult = sasCreateProcess.text.replace('"','')
    println "Result: $sasCreateProcessResult"
}

////////////////////////////////////////////////////////////
// Here is the URL of the file to import
//def azureStorageContainerUri = sasCreateResultMap.url
def azureStorageContainerUri = "https://${params.azureStorageAccountName}.blob.core.windows.net/${params.azureStorageContainerName}?$sasCreateProcessResult"
println "Container URL with SAS: $azureStorageContainerUri"
////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////
// 2. Obtain a OAuth token
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
// 3. Create the Export Job
////////////////////////////////////////////////////////////

def apiRoot = "https://$params.apiHost/subscriptions/$params.azmeSubscriptionId/resourceGroups/MobileEngagement/providers/Microsoft.MobileEngagement/appcollections/$params.appCollection/apps/$params.app/devices/exportTasks"

// Get Export Tasks List
println 'Getting Export Tasks List'
println apiRoot
def RESTClient restClientExport = new RESTClient(apiRoot)
restClientExport.defaultRequestHeaders['Authorization'] = "Bearer $token"
restClientExport.defaultRequestHeaders['Accept'] = "application/json"

try{
  def responseJob = restClientExport.get(
    query: ['api-version':'2014-12-01'],
    requestContentType : 'application/json;charset=UTF-8'
  )
  println responseJob.status
  println responseJob.data
}catch(HttpResponseException responseException){
  println "Status:$responseException.statusCode"
  println "Cause:$responseException.cause"
  println "Message:$responseException.message"
}

// Create a Snapshot task
println '\nCreating a snapshot task'
def createExportTask = "$apiRoot/$extractType"
println createExportTask
def exportTask = [
  containerUrl : azureStorageContainerUri,
  description: "An export",
  exportFormat: "JsonBlob"
]
def RESTClient restClientExportCreateExportTask = new RESTClient(createExportTask)
restClientExportCreateExportTask.defaultRequestHeaders['Authorization'] = "Bearer $token"
restClientExportCreateExportTask.defaultRequestHeaders['Accept'] = "application/json"

def restClientExportCreateExportTaskResponse = restClientExportCreateExportTask.post(
  query: ['api-version':'2014-12-01'],
  requestContentType : 'application/json;charset=UTF-8',
  body:JsonOutput.toJson(exportTask)

)
println restClientExportCreateExportTaskResponse.status
println restClientExportCreateExportTaskResponse.data

// Get a task status
println '\nGetting the task status'
def taskStatus = "$apiRoot/${restClientExportCreateExportTaskResponse.data.id}"
println taskStatus
def RESTClient restClientExportTaskStatus = new RESTClient(taskStatus)
restClientExportTaskStatus.defaultRequestHeaders['Authorization'] = "Bearer $token"
restClientExportTaskStatus.defaultRequestHeaders['Accept'] = "application/json"

def restClientExportTaskStatusResponse = restClientExportTaskStatus.get(
  query: ['api-version':'2014-12-01'],
  requestContentType : 'application/json;charset=UTF-8')
println restClientExportTaskStatusResponse.status
println restClientExportTaskStatusResponse.data
