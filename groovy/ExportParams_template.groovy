
class ExportParams{
  // Azure Storage Parameters where the exported files will be written
  public final azureStorageAccountName = '' // Your Azure Storage account name
  def azureStorageContainerName = ''        // The container name inside the storage account
  def azureStorageAccountKey  = ''          // The Azure Storage account key (secret)

  // AzME API OAuth Authentication Parameters
  def azmeTenantId = ''                 // Your Azure Active Directory Tenant ID
  def azmeAPIAuthAADApplicationId = ''  // Your Azure Active Directory application ID
  def azmeAPIAuthAADSecret = ''         // Your Azure Active Directory application secret

  // AzME API Parameters
  def azmeAPIAuthGetTokenHost = "login.microsoftonline.com"
  def apiHost = 'management.azure.com'
  def azmeSubscriptionId = ''   // Your Azure Subscription ID where the Mobile Engagement app is
  def appCollection = ''        // Your Mobile Engagement app collection name
  def app = ''                  // Your Mobile Engagement app name in the app collection
}
