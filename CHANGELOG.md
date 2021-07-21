## [1.3.0]
* Added Improved multi-package directory and 2GP support
* Added Goto Definition support for classes and static methods/fields
* Added Unused local variables warning
* Changed Improved loading performance & lower memory usage
* Changed Unused method/field analysis enabled
* Fixed VF Page/Component controller unused reporting
* Fixed Dependency Explorer failing to open
* Fixed Add missing types for FSL
* Fixed SuppressWarning not turning off unused warnings
* Fixed Error message on duplicate methods


## [1.2.2]

* Added - Big object support
* Added - Transaction Finalizer support
* Added - IsPortalEnabled on User
* Fixed - Static fields in triggers
* Fixed - Process.PluginDescribeResult inner classes
* Fixed - Interface method matching for SObject Lists
* Fixed - Missing classes in Auth namespace
* Fixed - Field resolution via superclass problem
* Fixed - Return type on platform enum values()
* Fixed - Resolving classes with same identifier as namespace
* Fixed - Errors on Object special methods having wrong return types

## [1.2.1]

* Fixed - Verify arguments of functions calls to 3rd party namespace
* Fixed - Add ConnectAPI MessageSegments classes
* Fixed - Add Component.Flow.Interview platfom type
* Fixed - OrgCacheException made inner of OrgCache
* Fixed - Comparisions between Time values
* Fixed - Exclude non-custom fields from object metadata
* Fixed - Missing functions on eventbus.TriggerContext
* Fixed - Missing functions/fields on eventbus.TriggerContext, FieldDefinition, EventBus & DeployResult
* Fixed - Surpress no-override warning on special functions equals, toString & hashCode
* Fixed - Missing 'Standard' relationship fields on Custom objects
* Fixed - Missing 'Standard' Notes & NotesAndAttachements on Custom objects
* Fixed - Missing HasOptedOutOfFax field in Contact
* Fixed - Missing Phase field in BatchApexErrorEvent
* Added - Support for custom sharing reasons
* Fixed - Missing 'Standard' fields on Platform Events
* Fixed - Missing Quiddity value for VF
* Fixed - Ordinal function on platform enums
* Fixed - Platform limits class missing function
* Fixed - Static field access via inheritance
* Added - SOQL Date functions
* Fixed - SOQL Parsing of FOR clause in sub-queries
* Fixed - SOSL Parsing for field names containing dot 
* Fixed - Precedence of cast expressions
* Fixed - Surrogate pair handling in source files

## [1.2.0]

* Changed - Updated dependency graph UI
* Changed - Updated SObject definitions for Spring '21 (thanks to @mrwordsmith)
* Fixed - Added System.Formula* classes (thanks to @mrwordsmith)
* Fixed - Improved handling of override methods
* Added - Visualforce Component attribute handling

## [1.1.0]

* Added - Setting to hide dependency graph nodes by regex, e.g. fflib_.*
* Added - Command to clear diagnostics
* Added - Setting to only show warnings for changed files (default on)
* Added - A CHANGELOG ;-)
* Fixed - Diagnostic display as you type
* Fixed - Inline SOSL queries (thanks to @codefriar for flagging omission)

## [1.0.1]

* Fixed - .forceignore handling on Windows (thanks to @FishOfPrey raising)
* Fixed - Support for Quiddity (thanks to @codefriar)

