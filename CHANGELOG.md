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

