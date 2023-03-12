## [2.0.1]

- Fix for metadata downloading from org

## [2.0.0]

- Updated to support Spring '23
- Improved code navigation incl goto implementation
- Enhanced support for final keyword
- Support for Analytics namespace types

## [1.7.0]

- Support for downloading org metadata to aid semantic analysis
- Support for a custom unpackage metadata directive
- Fixes for unused analysis and code completion processing

## [1.6.0]

- Support context sensitive code completion
- Enabled code completion and got definition for triggers
- Improved API for accessing issues
- Improved SuppressWarnings handling

## [1.5.0]

- Added initial version of code completion
- Added type checking on local variable initialisation
- Changed project loading to improve speed and reduce memory use
- Fixed errors on partial class handling
- Fixed duplicate static method detection
- Fixed duplicate constructor method detection
- Removed support for Generics

## [1.4.0]

- Significantly improved Goto Defintion support
- Support for Generics Proof of Concept
- File watchers are more specific
- Improved checking for server termination

## [1.3.0]

- Added Improved multi-package directory and 2GP support
- Added Goto Definition support for classes and static methods/fields
- Added Unused local variables warning
- Changed Improved loading performance & lower memory usage
- Changed Unused method/field analysis enabled
- Fixed VF Page/Component controller unused reporting
- Fixed Dependency Explorer failing to open
- Fixed Add missing types for FSL
- Fixed SuppressWarning not turning off unused warnings
- Fixed Error message on duplicate methods

## [1.2.2]

- Added - Big object support
- Added - Transaction Finalizer support
- Added - IsPortalEnabled on User
- Fixed - Static fields in triggers
- Fixed - Process.PluginDescribeResult inner classes
- Fixed - Interface method matching for SObject Lists
- Fixed - Missing classes in Auth namespace
- Fixed - Field resolution via superclass problem
- Fixed - Return type on platform enum values()
- Fixed - Resolving classes with same identifier as namespace
- Fixed - Errors on Object special methods having wrong return types

## [1.2.1]

- Fixed - Verify arguments of functions calls to 3rd party namespace
- Fixed - Add ConnectAPI MessageSegments classes
- Fixed - Add Component.Flow.Interview platfom type
- Fixed - OrgCacheException made inner of OrgCache
- Fixed - Comparisions between Time values
- Fixed - Exclude non-custom fields from object metadata
- Fixed - Missing functions on eventbus.TriggerContext
- Fixed - Missing functions/fields on eventbus.TriggerContext, FieldDefinition, EventBus & DeployResult
- Fixed - Surpress no-override warning on special functions equals, toString & hashCode
- Fixed - Missing 'Standard' relationship fields on Custom objects
- Fixed - Missing 'Standard' Notes & NotesAndAttachements on Custom objects
- Fixed - Missing HasOptedOutOfFax field in Contact
- Fixed - Missing Phase field in BatchApexErrorEvent
- Added - Support for custom sharing reasons
- Fixed - Missing 'Standard' fields on Platform Events
- Fixed - Missing Quiddity value for VF
- Fixed - Ordinal function on platform enums
- Fixed - Platform limits class missing function
- Fixed - Static field access via inheritance
- Added - SOQL Date functions
- Fixed - SOQL Parsing of FOR clause in sub-queries
- Fixed - SOSL Parsing for field names containing dot
- Fixed - Precedence of cast expressions
- Fixed - Surrogate pair handling in source files

## [1.2.0]

- Changed - Updated dependency graph UI
- Changed - Updated SObject definitions for Spring '21 (thanks to @mrwordsmith)
- Fixed - Added System.Formula\* classes (thanks to @mrwordsmith)
- Fixed - Improved handling of override methods
- Added - Visualforce Component attribute handling

## [1.1.0]

- Added - Setting to hide dependency graph nodes by regex, e.g. fflib\_.\*
- Added - Command to clear diagnostics
- Added - Setting to only show warnings for changed files (default on)
- Added - A CHANGELOG ;-)
- Fixed - Diagnostic display as you type
- Fixed - Inline SOSL queries (thanks to @codefriar for flagging omission)

## [1.0.1]

- Fixed - .forceignore handling on Windows (thanks to @FishOfPrey raising)
- Fixed - Support for Quiddity (thanks to @codefriar)
