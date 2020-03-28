
# Apex Assist

This extension provides advanced tools for supporting Salesforce Apex development. This version is *Experimental* in 
that it only supports finding unused methods & fields as an example analysis, the extension will highlight other code 
problems but only those that might impact this analysis.

## Performance

The extension is written in Scala and cross compiled to JavaScript. This means there is no need to have Java installed
but the use of just JavaScript does impact performance. This version adds a filesystem cache to reduce analysis time. 
The first time you run over a new project the cache will be empty and it may take a few minutes for
the analysis to complete, subsequent analysis on the same or similar projects should complete in a few seconds. 

## QuickStart

* Check your VSCode version is up to date
* Install Apex Assist
* Open a directory in VSCode containing Salesforce package source (MDAPI or SFDX format)
* Run the command 'ApexAssist: Find zombies'

![Zombie Command](https://raw.githubusercontent.com/nawforce/ApexLink/master/images/FindZombies.png)

* When the analysis is complete (may take a few minutes) unused methods & fields will have warnings

![Warning Example](https://raw.githubusercontent.com/nawforce/ApexLink/master/images/UnusedField.png)

You can monitor progress of the analysis in the VSCode Output Window, select 'Apex Assist' from the drop down menu.

## Multi-Package Analysis

The base analysis code supports full multi-package analysis but only a subset is currently supported by this extension.
If you are analysing a project that depends on other packages you should provide a comma separated list of the namespaces 
in the "extraNamespaces" VS Code settings for 'Apex Assist'. This will suppress errors you will get otherwise because
the namespace can not be found. See [ApexLink](https://github.com/nawforce/ApexLink) for more information.  

## FAQ

1. *What Apex files are used in the analysis?*
For directories with a sfdx-project.json file the analysis scans the directories identified by 'packageDirectory'. If 
there is no sfdx-package.json then all sub-directories are scanned for metadata. For sfdx format projects the 
.forceignore file directives are honoured.

2. *How are namespaces handled?*
For sfdx projects the namespace is read from sfdx-project.json if available, otherwise it is assumed that the package 
metadata can be deployed 'unmanged' without an explicit namespace.

3. *I get an error for feature/field Y implemented in API XX, why?*
The analysis contains a description of the platform types and SObjects that is checks against. These could either be 
a little out of date or I may have missed something. Create an issue at [ApexLink](https://github.com/nawforce/ApexLink).

4. Where is the cache held?
It's located in your home directory in '.apexlink_cache'. Each time the cache is used items older than 7 days will be
removed but you can clear it at anytime by simply removing it. You can also set the environment variable 
APEXLINK_CACHE_DIR to provide a custom location.        
