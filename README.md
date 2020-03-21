
# Apex Assist

This extension provides advanced tools for supporting Salesforce Apex development. This version is *Experimental* in 
that it only supports finding unused methods & fields as an example analysis, the extension will highlight other code 
problems but only those that might impact this analysis.

## Performance

The extension is written in Scala and cross compiled to JavaScript. This means there is no need to have Java installed
but the use of just JavaScript does impact performance. This version adds a filesystem cache to reduce analysis time. 
The first time you run over a new product the cache will be empty and it may take a few minutes for
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

## FAQ

1. *What Apex files are used in the analysis?*
For directories with a sfdx-project.json file the analysis scans the directories identified by 'packageDirectory'. If 
there is no sfdx-package.json then all sub-directories are scanned for metadata. For sfdx format projects the 
.forceignore file directives are honoured.

2. *How are namespaces handled?*
For sfdx projects the namespace is read from sfdx-project.json if available, otherwise it is assumed that the package 
metadata can be deployed 'unmanged' without an explicit namespace.

3. *Can I reference other managed packaged such as CPQ?*
The base analysis code supports multi-package analysis but it's not yet possible via this extension, See 
[ApexLink](https://github.com/nawforce/ApexLink).

4. *I get an error for feature/field Y implemented in API XX, why?*
The analysis contains a description of the platform types and SObjects that is checks against. These could either be 
a little out of date or I may have missed something. Create an issue at [ApexLink](https://github.com/nawforce/ApexLink).        
