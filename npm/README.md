
# Apex Assist

This is a developer productibity extension that provides immediate validation of Apex source code without 
the need to wait for files to be deployed to an org. You can use it in addition to the Salesforce Apex
extension or standlone if you want to significantly reduce the CPU and Memory needs of VSCode which can be
very helpful on larger projects.

The extension currently implements two levels of validation:
* As you type, each Apex file change is checked for basic errors
* When you Save, the change to the file is fully checked for errors but also other Apex classes are checked to show other code that may be broken by a change such as renaming a function.

The 'On Save' full validation is only performed on files that need checking, how many is determined by a dependency analysis of the Apex classes.

## Quick Setup

The extension is activated on any workspace which contains a sfdx-project.json file. It reads the 
packageDirectory entries to locate Salesforce metadata and honours .forceignore entries. It will read both
SFDX (source format) or MDAPI metadata and it not fussy about which you provide.

## Cache Management

To speed workspace loading in VSCode the extension uses a cache that is by default created in $HOME/.apexlink_cache.
The first time you use the extension it will take slightly longer to start-up as the cache is populated, 
subsequent workspace loading will be significantly quicker.  

## Multi-Package Analysis

If you are working with Apex classes that reference managed packages then references to these
will initially show up as errors. You can declare the namespaces as known within you sfdx-project.json which will surpress these errors.

To do this add for the "aa" namespace add:

    "plugins": {
        "dependencies": [
            {"namespace": "aa"}
        ]
    }


## FAQ

1. *How are namespaces handled?*
For sfdx projects the namespace is read from sfdx-project.json if available, otherwise it is assumed that the package 
metadata can be deployed 'unmanged' without an explicit namespace.

2. *I get an error for feature/field Y implemented in API XX, why?*
The analysis contains a description of the platform types and SObjects that it checks against. These could either be 
a little out of date or I may have missed something. Create an issue at [ApexLink](https://github.com/nawforce/ApexLink).

3. Where is the cache held?
It's located in your home directory in '.apexlink_cache'. Each time the cache is used items older than 7 days will be
removed but you can clear it at anytime by simply removing it. You can also set the environment variable 
APEXLINK_CACHE_DIR to provide a custom location.        
