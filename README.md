
# Apex Assist

Apex Assist provides instant validation of Apex source code without the need to wait for files to be deployed to an org. You can use it in addition to the Salesforce Apex extension or as an alternative. The extension is particularly useful when used on its own as it uses very little memory or cpu. The local validation makes refactoring code much easier and quicker than waiting for and deployment to an org to fail, you get continual feedback on problems in the code as you are making changes.

In the latest version we have added full support for 'Goto Defintion'. This uses the type analysis code used for local validation to find definitions for just about anything you can reference in Apex, this includes Apex entities such as classes and methods but also more general metadata items such as custom objects, custom fields and labels.

Here is an example of using it to rename a method and finding uses of that method that need to be updated.

![Another](https://raw.githubusercontent.com/nawforce/media/main/apex-assist/MethodRename.gif)

The extension also includes a class dependency explorer which you can use to visualize your code base.

![Another](https://raw.githubusercontent.com/nawforce/media/main/apex-assist/DependencyExplorer.gif)

There are two levels of validation:
- **As you type** checking occurs for syntax errors and basic semantics errors
- **When you Save** checking occurs for full syntax and semantic errors, additional validation occurs to see if
other files may now have errors due to the changes made.
  
Foe example, a badly used modifier can be detected as you type, but passing the wrong arguments to a method would only be detected after saving the file.

## Setup

The extension activates on any workspace which contains a sfdx-project.json file. It reads the packageDirectory entries to locate Salesforce metadata and honours forceignore entries. It will read both SFDX (source format) or MDAPI metadata so you can use a fake sfdx-project.json/.forceignore files with MDAPI projects if needed.

When your project loads, you are likely to see some warnings. You can disable the display of warnings in the VSCode settings for 'Apex Assist', so these are not shown by default. 

The extension requires that you have a Java 8 or above installed and that the 'java' command line executable is available.

## Cache Management

To speed workspace loading in VSCode the extension uses a cache that is by default created in $HOME/.apexlink_cache. The first time you use the extension it will take a little longer to start-up, typically about 30 seconds. Subsequent workspace loading will be significantly quicker, a few seconds typically.  

## Multi-Package Analysis

If you are working with Apex classes that reference managed packages then references to these will initially show 
up as errors. You can declare the namespaces as known within your sfdx-project.json which will suppress these errors.

To do this, add for the "aa" namespace add:

    "plugins": {
        "dependencies": [
            {"namespace": "aa"}
        ]
    }


## FAQ

1. *How are namespaces handled?*
For sfdx projects the namespace is read from sfdx-project.json if available, otherwise it is assumed that the package metadata can be deployed 'unmanged' without an explicit namespace.

2. *I get an error for feature/field Y implemented in API XX, why?*
The analysis library contains a description of the platform types and SObjects that it checks against. These could either be a little out of date or I may have missed something. Create an issue at [apex-link](https://github.com/nawforce/apex-link).

3. *Where is the cache held?*
It's located in your home directory in '.apexlink_cache'. Each time the cache is used items older than 7 days will be removed but you can clear it at anytime by simply removing it. You can also set the environment variable APEXLINK_CACHE_DIR to provide a custom location.

4. *What types of errors can it detect?*
The code analysis at the moment centers around making sure all identifiers in your code can be located, these are things like Class names/fields, local variables, methods, custom objects and their fields etc. If an identifier can not be located you will always get an error. Parts of Apex that contribute to this goal are fully implemented but there are other validations which have yet to be implemented such as type checking of assignments. 

5. *The extension is not working, how do I find out what is happening?*
Open the VSCode Output window (View->Output) and select 'Apex-Assist' from the drop down. Here you will see debug output from the extension. if you need assistance create an issue at [ApexAssist](https://github.com/nawforce/apex-assist) and include the output window contents.
