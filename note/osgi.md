osgi> help

---Application Admin Commands---
    activeApps - lists all running application IDs
    apps - lists all installed application IDs
    lockApp <application id> - locks the specified application ID
    schedApp <application id> <time filter> [true|false] - schedules the specified application id to launch at the specified time filter.  Can optionally make the schedule recurring.
    startApp <application id> - starts the specified application ID
    stopApp <application id> - stops the specified running application ID
    unlockApp <application id> - unlocks the specified application ID
    unschedApp <application id> - unschedules all scheduled applications with the specified application ID

---Extension Registry Commands---
    ns [-v] [name] - display extension points in the namespace; add -v to display extensions
    pt [-v] uniqueExtensionPointId - display the extension point and extensions; add -v to display config elements

---Configurator Commands---
    confapply [<config URL>] - Applies a configuration


close - shutdown and exit
   scope: equinox

diag - Displays unsatisfied constraints for the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   IDs of bundle(s), for which to display unsatisfied constraints

shutdown - shutdown the OSGi Framework
   scope: equinox

gc - perform a garbage collection
   scope: equinox

javadoc - retrieve resource JavaDoc from repository
   scope: obr
   flags:
      -x, --extract   extract documentation
   parameters:
      File   local target directory
      String[]   ( <bundle-name> | <symbolic-name> | <bundle-id> )[@<version>] ...

profilelog - Display & flush the profile log messages
   scope: equinox

frameworklevel - set framework active start level
   scope: felix
   parameters:
      int   target start level

frameworklevel - query framework active start level
   scope: felix

echo
   scope: gogo
   parameters:
      Object[]   

telnet - start/stop a telnet server
   scope: equinox
   parameters:
      String[]   

info
   scope: scr
   parameters:
      String   

deploy - deploy resource from repository
   scope: obr
   flags:
      -s, --start   start deployed bundles
   parameters:
      String[]   ( <bundle-name> | <symbolic-name> | <bundle-id> )[@<version>] ...

status - display installed bundles and registered services
   scope: equinox
   parameters:
      String[]   [-s <comma separated list of bundle states>] [segment of bsn]

start - start bundles
   scope: felix
   flags:
      -p, --policy   use declared activation policy
      -t, --transient   start bundle transiently
   parameters:
      String[]   target bundle identifiers or URLs

setprop - set OSGi properties
   scope: equinox
   parameters:
      String[]   list of properties with values to be set; the format is <key>=<value> and the pairs are separated with space if more than one

getprop - displays the system properties with the given name, or all of them
   scope: equinox
   parameters:
      String[]   name of system property to dispaly

help
   scope: equinox
   parameters:
      CommandSession   
      String[]   

b - display details for the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   (<id>|<location>)

install - install bundle using URLs
   scope: felix
   parameters:
      String[]   target URLs

uninstall - uninstall the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to uninstall

type
   scope: gogo
   parameters:
      CommandSession   
      String[]   

setp - set OSGi properties
   scope: equinox
   parameters:
      String[]   list of properties with values to be set; the format is <key>=<value> and the pairs are separated with space if more than one

getopt
   scope: gogo
   parameters:
      List   
      Object[]   

bundles - display details for all installed bundles
   scope: equinox
   parameters:
      String[]   [-s <comma separated list of bundle states>] [segment of bsn]

gosh
   scope: gogo
   parameters:
      CommandSession   
      String[]   

p - display imported/exported package details
   scope: equinox
   parameters:
      String   Package name of the package to display

p - display imported/exported package details
   scope: equinox
   parameters:
      Bundle[]   Bundle whose packages to display. If not present displays all exported packages

not
   scope: gogo
   parameters:
      CommandSession   
      Function   

update - update bundle
   scope: felix
   parameters:
      Bundle   target bundle

update - update bundle from URL
   scope: felix
   parameters:
      Bundle   target bundle
      String   URL from where to retrieve bundle

r - refresh the packages of the specified bundles; if -all option is specified refresh packages of all installed bundles
   scope: equinox
   flags:
      -all   specify to refresh the packages of all installed bundles
   parameters:
      Bundle[]   list of bundles whose packages to be refreshed; if not present refreshes all bundles

exit - exit immediately (System.exit)
   scope: equinox

update - Update the specified bundle from the specified location
   scope: equinox
   parameters:
      Bundle   Bundle to update
      URL   Location of the new bundle content

update - update the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to update

h - print bundle headers
   scope: equinox
   parameters:
      Bundle[]   bundles to print headers for

i - install and optionally start bundle from the given URL
   scope: equinox
   flags:
      -start   specify if the bundle should be started after installation
   parameters:
      String   Location of bundle to install

pr - Display system properties
   scope: equinox

cat
   scope: gogo
   parameters:
      CommandSession   
      String[]   

requiredBundles - lists required bundles having the specified symbolic name
   scope: equinox
   parameters:
      String[]   symbolic name for required bundles to be listed; if not specified all required bundles will be listed

log - display all matching log entries
   scope: felix
   parameters:
      String   minimum log level [ debug | info | warn | error ]

log - display some matching log entries
   scope: felix
   parameters:
      int   maximum number of entries
      String   minimum log level [ debug | info | warn | error ]

s - display installed bundles and registered services
   scope: equinox
   parameters:
      String[]   [-s <comma separated list of bundle states>] [segment of bsn]

t - stop the named thread with the provided throwable
   scope: equinox
   parameters:
      String   stop
      String   the thread on which to perform the action
      Class   the class of the throwable to throw (default = java.lang.IllegalStateException)

t - display threads and thread groups
   scope: equinox

t - stop the named thread with an IllegalStateException
   scope: equinox
   parameters:
      String   stop
      String   the thread on which to perform the action

disconnect
   scope: equinox
   parameters:
      CommandSession   

stop - stop the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to stop

which - determines from where a bundle loads a class
   scope: felix
   parameters:
      Bundle   target bundle
      String   target class name

threads - stop the named thread with the provided throwable
   scope: equinox
   parameters:
      String   stop
      String   the thread on which to perform the action
      Class   the class of the throwable to throw (default = java.lang.IllegalStateException)

threads - display threads and thread groups
   scope: equinox

threads - stop the named thread with an IllegalStateException
   scope: equinox
   parameters:
      String   stop
      String   the thread on which to perform the action

refresh - refresh bundles
   scope: felix
   parameters:
      Bundle[]   target bundles (can be null or empty)

headers - print bundle headers
   scope: equinox
   parameters:
      Bundle[]   bundles to print headers for

format
   scope: gogo
   parameters:
      CommandSession   
      Object   

format
   scope: gogo
   parameters:
      CommandSession   

setibsl - set the initial bundle start level
   scope: equinox
   parameters:
      int   new start level

until
   scope: gogo
   parameters:
      CommandSession   
      Function   
      Function   

init - uninstall all bundles
   scope: equinox

classSpaces - lists required bundles having the specified symbolic name
   scope: equinox
   parameters:
      String[]   symbolic name for required bundles to be listed; if not specified all required bundles will be listed

tac
   scope: gogo
   parameters:
      CommandSession   
      String[]   

start - start the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to start

resolve - resolve bundles
   scope: felix
   parameters:
      Bundle[]   target bundles (can be null or empty)

list - list repository resources
   scope: obr
   flags:
      -v, --verbose   display all versions
   parameters:
      String[]   optional strings used for name matching

ls - get current directory contents
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session

ls - get specified path contents
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session
      String   path with optionally wildcarded file name

sl - display the start level for the specified bundle, or for the framework if no bundle specified
   scope: equinox
   parameters:
      Bundle[]   bundle to get the start level

setbsl - set the start level for the bundle(s)
   scope: equinox
   parameters:
      int   new start level
      Bundle[]   bundle(s) to change startlevel

se - display registered service details. Examples for [filter]: (objectClass=com.xyz.Person); (&(objectClass=com.xyz.Person)(sn=Jensen)); passing only com.xyz.Person is a shortcut for (objectClass=com.xyz.Person). The filter syntax specification is available at http://www.ietf.org/rfc/rfc1960.txt
   scope: equinox
   parameters:
      String[]   Optional filter for filtering the displayed services. Examples for the filter: (objectClass=com.xyz.Person); (&(objectClass=com.xyz.Person)(sn=Jensen)); passing only com.xyz.Person is a shortcut for (objectClass=com.xyz.Person). The filter syntax specification is available at http://www.ietf.org/rfc/rfc1960.txt

stop - stop bundles
   scope: felix
   flags:
      -t, --transient   stop bundle transiently
   parameters:
      Bundle[]   target bundles

sto - stop the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to stop

install - install and optionally start bundle from the given URL
   scope: equinox
   flags:
      -start   specify if the bundle should be started after installation
   parameters:
      String   Location of bundle to install

man
   scope: equinox
   parameters:
      CommandSession   
      String[]   

fork - execute a command in a separate process
   scope: equinox
   parameters:
      String   command to be executed

enable
   scope: scr
   parameters:
      String   

help - displays available commands
   scope: felix

help - displays information about a specific command
   scope: felix
   parameters:
      String   target command

setfwsl - set the framework start level
   scope: equinox
   parameters:
      int   new start level

ss - display installed bundles (short status)
   scope: equinox
   parameters:
      String[]   [-s <comma separated list of bundle states>] [segment of bsn]

disable
   scope: scr
   parameters:
      String   

config
   scope: scr

inspect - inspects bundle dependency information
   scope: felix
   parameters:
      String   (package | bundle | fragment | service)
      String   (capability | requirement)
      Bundle[]   target bundles

bundlelevel - set bundle start level or initial bundle start level
   scope: felix
   flags:
      -i, --setinitial   set the initial bundle start level
      -s, --setlevel   set the bundle's start level
   parameters:
      int   target level
      Bundle[]   target identifiers

bundlelevel - query bundle start level
   scope: felix
   parameters:
      Bundle   bundle to query

services - display registered service details. Examples for [filter]: (objectClass=com.xyz.Person); (&(objectClass=com.xyz.Person)(sn=Jensen)); passing only com.xyz.Person is a shortcut for (objectClass=com.xyz.Person). The filter syntax specification is available at http://www.ietf.org/rfc/rfc1960.txt
   scope: equinox
   parameters:
      String[]   Optional filter for filtering the displayed services. Examples for the filter: (objectClass=com.xyz.Person); (&(objectClass=com.xyz.Person)(sn=Jensen)); passing only com.xyz.Person is a shortcut for (objectClass=com.xyz.Person). The filter syntax specification is available at http://www.ietf.org/rfc/rfc1960.txt

props - Display system properties
   scope: equinox

source
   scope: gogo
   parameters:
      CommandSession   
      String   

cd - get current directory
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session

cd - change current directory
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session
      String   target directory

getPackages - lists all packages visible from the specified bundle
   scope: equinox
   parameters:
      Bundle   bundle to list the visible packages

set
   scope: gogo
   parameters:
      CommandSession   
      String[]   

up - Update the specified bundle from the specified location
   scope: equinox
   parameters:
      Bundle   Bundle to update
      URL   Location of the new bundle content

up - update the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to update

repos - manage repositories
   scope: obr
   parameters:
      String   ( add | list | refresh | remove )
      String[]   space-delimited list of repository URLs

un - uninstall the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to uninstall

sh
   scope: gogo
   parameters:
      CommandSession   
      String[]   

grep
   scope: gogo
   parameters:
      CommandSession   
      String[]   

packages - display imported/exported package details
   scope: equinox
   parameters:
      Bundle[]   Bundle whose packages to display. If not present displays all exported packages

packages - display imported/exported package details
   scope: equinox
   parameters:
      String   Package name of the package to display

bundle - display details for the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   (<id>|<location>)

lb - list all installed bundles
   scope: felix
   flags:
      -l, --location   show location
      -s, --symbolicname   show symbolic name
      -u, --updatelocation   show update location

lb - list installed bundles matching a substring
   scope: felix
   flags:
      -l, --location   show location
      -s, --symbolicname   show symbolic name
      -u, --updatelocation   show update location
   parameters:
      String   subtring matched against name or symbolic name

refresh - refresh the packages of the specified bundles; if -all option is specified refresh packages of all installed bundles
   scope: equinox
   flags:
      -all   specify to refresh the packages of all installed bundles
   parameters:
      Bundle[]   list of bundles whose packages to be refreshed; if not present refreshes all bundles

telnetd
   scope: gogo
   parameters:
      String[]   

list
   scope: scr
   parameters:
      String   

list
   scope: scr

sta - start the specified bundle(s)
   scope: equinox
   parameters:
      Bundle[]   bundle(s) to start

headers - display bundle headers
   scope: felix
   parameters:
      Bundle[]   target bundles

exec - execute a command in a separate process and wait
   scope: equinox
   parameters:
      String   command to be executed

info - retrieve resource description from repository
   scope: obr
   parameters:
      String[]   ( <bundle-name> | <symbolic-name> | <bundle-id> )[@<version>] ...

uninstall - uninstall bundles
   scope: felix
   parameters:
      Bundle[]   target bundles

each
   scope: gogo
   parameters:
      CommandSession   
      Collection   
      Function   

source - retrieve resource source code from repository
   scope: obr
   flags:
      -x, --extract   extract source code
   parameters:
      File   local target directory
      String[]   ( <bundle-name> | <symbolic-name> | <bundle-id> )[@<version>] ...
osgi> 
