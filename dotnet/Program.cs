using System;
using System.Collections.Generic;
using System.Json;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Auth;
using Microsoft.WindowsAzure.Storage.Blob;

namespace dotnet
{
    class Program
    {
        private static string azmeAPIAuthGetTokenURL = $"https://{ExportParams.azmeAPIAuthGetTokenHost}/{ExportParams.azmeTenantId}/oauth2/token";

        private static async Task<string> GetOAuthToken(){
            HttpClient client = new HttpClient();
            var content = new FormUrlEncodedContent(new[]
            {
                new KeyValuePair<string, string>("grant_type", "client_credentials"),
                new KeyValuePair<string, string>("client_id", ExportParams.azmeAPIAuthAADApplicationId),
                new KeyValuePair<string, string>("client_secret", ExportParams.azmeAPIAuthAADSecret),
                new KeyValuePair<string, string>("resource", "https://management.core.windows.net/")            
            });
            var result = await client.PostAsync(azmeAPIAuthGetTokenURL, content);
            string json = await result.Content.ReadAsStringAsync();
            JsonValue parsedJson = JsonValue.Parse(json);
            return parsedJson["access_token"];
        }
        private static async Task<string> CreateExportTokenTask(string token, string sasUrl){
            string apiRoot = $"https://{ExportParams.apiHost}/subscriptions/{ExportParams.azmeSubscriptionId}/resourceGroups/MobileEngagement/providers/Microsoft.MobileEngagement/appcollections/{ExportParams.appCollection}/apps/{ExportParams.app}/devices/exportTasks/tokens?api-version=2014-12-01";

            HttpClient client = new HttpClient();
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
            var content = new FormUrlEncodedContent(new[]
            {
                new KeyValuePair<string, string>("containerUrl", sasUrl),
                new KeyValuePair<string, string>("description", "An export"),
                new KeyValuePair<string, string>("exportFormat", "CsvBlob"),
            });

            var result = await client.PostAsync(apiRoot, content);
            string json = await result.Content.ReadAsStringAsync();

            return json;
        }


        static void Main(string[] args)
        {
            // //////////////////////
            // Obtaining a SAS URL
            // //////////////////////
            string storageUri = $"https://{ExportParams.azureStorageAccountName}.blob.core.windows.net/{ExportParams.azureStorageContainerName}";
            CloudBlobContainer container = new CloudBlobContainer(
                new Uri(storageUri), 
                new StorageCredentials(ExportParams.azureStorageAccountName,ExportParams.azureStorageAccountKey)
            );
            SharedAccessBlobPolicy sasPolicy = new SharedAccessBlobPolicy();
            sasPolicy.Permissions = SharedAccessBlobPolicy.PermissionsFromString("wl");
            sasPolicy.SharedAccessExpiryTime = DateTimeOffset.Now.AddDays(1);
            sasPolicy.SharedAccessStartTime = DateTimeOffset.Now;
            String sas = container.GetSharedAccessSignature(sasPolicy);

            string storageUriWithSAS = storageUri + sas;

            Console.WriteLine($"Storage URL with SAS: {storageUriWithSAS}");

            // //////////////////////
            // Obtain an oauth token
            // //////////////////////

            string token = GetOAuthToken().GetAwaiter().GetResult();
            Console.WriteLine($"Token: {token}");

            // //////////////////////
            // Create a token export task
            // //////////////////////
            string taskResult = CreateExportTokenTask(token, storageUriWithSAS).GetAwaiter().GetResult();
            Console.WriteLine($"Task: {taskResult}");

        }
    }
}
