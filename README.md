
# Apex Assist

This extension provides advanced tools for supporting Salesforce Apex development. Currently it demos the ability to
find unused methods & fields. This version is *Experimental* in two ways:

* It does not rely on having a JVM installed, the language analysis code  has been cross compiled to JavaScript for use with VSCode. This has reduced the performance so it should only be used with smaller projects.
* Finding unused methods & fields is just an example analysis, the extension will highlight other code problems but only those that might impact this analysis.

Performance improvements and more analysis features will be added in future releases.

## QuickStart

* Install Apex Assist
* Open a directory in VSCode containing Salesforce package source (MDAPI or SFDX format)
* Run the command 'ApexAssist: Find zombies'

![Zombie Command](https://raw.githubusercontent.com/nawforce/ApexLink/master/images/FindZombies.png)

* When the analysis is complete (may take a few minutes) unused methods & fields will have warnings

![Warning Example](https://raw.githubusercontent.com/nawforce/ApexLink/master/images/UnusedField.png)

You can monitor progress of the analysis in the VSCode Output Window, select 'Apex Assist' from the drop down menu.

## FAQ

1. *What Apex files are used in the analysis?*
For directories with a sfdx-project.json file the analysis scans the directories identified by 'packageDirectory'. If there is no sfdx-package.json then all sub-directories are scanned for metadata. For sfdx format projects the .forceignore file directives are honoured.

2. *How are namespaces handled?*
For sfdx projects the namespace is read from sfdx-project.json if available, otherwise it is assumed that the package metadata can be deployed 'unmanged' without an explicit namespace.

3. *Can I reference other managed packaged such as CPQ?*
The base analysis code supports multi-package analysis but it's not yet possible via this extension, See [ApexLink](https://github.com/nawforce/ApexLink).

4. *I get an error for feature/field Y implemented in API XX, why?*
The analysis contains a description of the platform types and SObjects that is checks against. These could either be a little out of date or I may have missed something. Create an issue at [ApexLink](https://github.com/nawforce/ApexLink).        
