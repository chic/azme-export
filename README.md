# Azure Mobile Engagememnt export example

This repository contains 2 examples of how to use Azure Mobile Engagement export REST API to get your app's token
The examples are available in Groovy and C#/.net environment

## Pre-requisite

Visit the [main documentation](https://docs.microsoft.com/en-us/azure/mobile-engagement/mobile-engagement-api-export-overview) to learn about Azure Mobile Engagement export in general.

The result of the export needs to be written in an Azure Storage Container: you will need to use an existing or create a Blob Storage Account from the Azure portal. If creating one, you may select the cheapest options as export is not a storage IO intensive operations. 

Then you can either specify your storage account parameters in the ExportParams class of this example, or pass the full URL with the SAS.

If you want to generate your own 1-time SAS, I strongly advise the use of Azure Storage Explorer, [it is very easy to create a SAS URL with it](https://docs.microsoft.com/en-us/azure/vs-azure-tools-storage-explorer-blobs#get-the-sas-for-a-blob-container).


> **Make sure you ask for Write and List permissions.**

## Use with .net

- Make sure you have the [dotnet cli installed](https://aka.ms/dotnetcoregs)
- Then change to the dotnet folder
- Set up the export parameters:
  - Copy the `ExportParams_Template.cs` file into `ExportParams.cs` 
  - Change the class name to `ExportParams`
  - Update all the parameters. You may omit the Storage parameters if you provide the Container URL in the commandline
- Restore the NuGet packages with `dotnet restore`
- Execute with `dotnet run` if you have provided all the parameters
- OR execute with `dotnet run <ContainerURLWithSAS>` if you obtained your SAS from Azure Storage Explorer
