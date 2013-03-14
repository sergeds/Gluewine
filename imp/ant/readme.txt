Required properties:
===================
* product : the name of the product, used for various filenames
* version : the version of the product as a whole, used for names of source and javadoc zip files
* starthtml : (required for gwt projects) the html file to use
* gwt.basepackage : (required for gwt projects) the base package to "anchor" the gwt code
* framework : "osgi" or "gluewine"
* target : sets the javac target version.
* source : sets the javac source version.

Optional properties:
====================
* vcs : "git" or "svn". Used to pick how to get revision numbers. If not set, revision headers will have an empty value
* osgihost : the host the gwt code connects to. Default localhost
* osgiport : the port the gwt code connects to. Default 7777

* usesgwt : set to "false" to disable building gwt. Default true
* has_unit_tests : set to "false" to disable junit tests. Default true
* needs_pgsql_for_tests : set to "true" to enable postgresql-based junit tests. Default false

* debuginfo : set to "off" to disable javac debug info. Default on
* debuglevel : sets the javac debug info to generate. Default "source,lines,vars"
* warfile : sets the filename for the generated war file. Default ${product}.war

Extention targets:
==================
If the code needs the "bundleextra" feature (i.e. it needs to include additional files in the bundle jar), add a target named "bundleextra" to your build.xml. Make sure to specify correct dependencies. This target can e.g. copy files in place.

System-wide settings:
=====================

If a file named .antbuildsettings is found in the user's home directory, it will be read after build.xml but before everything else. Supported properties are:

* gwt.workers : set to the number of desired worker threads for gwt compilation. Default 2
