
# Apex Assist

Apex Assists is a developer productivity extension that provides immediate validation of Apex source code without 
the need to wait for files to be deployed to an org. You can use it in addition to the Salesforce Apex
extension or as an alternative.

* TODO: Image here *

The extension implements two levels of validation:
- **As you type** each Apex file change is checked for basic errors
- **When you Save** the change to the Apex file is fully checked for errors and other other Apex files are re-checked 
to show any other code that may be broken, such as by renaming a function.

## Tutorial

* Link here *

## Setup

The extension is activated on any workspace which contains a sfdx-project.json file. It reads the 
packageDirectory entries to locate Salesforce metadata and honours .forceignore entries. It will read both
SFDX (source format) or MDAPI metadata and it not fussy about which you provide.

When your project is loaded you are likely to see some warnings. You can disable the display of warning in the the 
VSCode settings for 'Apex Assist' so these are not shown by default. 

## Cache Management

To speed workspace loading in VSCode the extension uses a cache that is by default created in $HOME/.apexlink_cache.
The first time you use the extension it will take slightly longer to start-up as the cache is populated, 
subsequent workspace loading will be significantly quicker.  

## Multi-Package Analysis

If you are working with Apex classes that reference managed packages then references to these
will initially show up as errors. You can declare the namespaces as known within you sfdx-project.json which will 
suppress these errors.

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
