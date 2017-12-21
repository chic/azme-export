class ExportParams_Template {
    static string  azureStorageAccountName = ""; // Your Azure Storage account name
    static string  azureStorageContainerName = "";        // The container name inside the storage account
    static string  azureStorageAccountKey  = "";          // The Azure Storage account key (secret)

  // AzME API OAuth Authentication Parameters
    static string  azmeTenantId = "";                 // Your Azure Active Directory Tenant ID
    static string  azmeAPIAuthAADApplicationId = "";  // Your Azure Active Directory application ID
    static string  azmeAPIAuthAADSecret = "";         // Your Azure Active Directory application secret

  // AzME API Parameters
    static string  azmeAPIAuthGetTokenHost = "login.microsoftonline.com";
    static string  apiHost = "management.azure.com";
    static string  azmeSubscriptionId = "";   // Your Azure Subscription ID where the Mobile Engagement app is
    static string  appCollection = "";        // Your Mobile Engagement app collection name
    static string  app = "";                  // Your Mobile Engagement app name in the app collection

}