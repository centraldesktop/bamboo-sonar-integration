Homepage/Wiki
=============
<http://docs.marvelution.com/display/BAMSON>

Issue Tracker
=============
<http://issues.marvelution.com/browse/BAMSON>

Continuous Builder
==================
<http://builds.marvelution.com/browse/BAMSON>

License
=======
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)


Build
=====

Go download and install the Atlassian Plugin SDK

> git clone  git://github.com/Marvelution/marvelution-pom.git

Check the version of the parent in this projects pom, make sure you check that tag out.  If you version is 17, use ...
> git checkout marvelution-17

Change the maven-site-plugin the the marvelution-pom/pom.xml project to a version that is supported by
 Atlassian's old version of maven that comes bundled with their SDK.  (Try version 2.0)

Comment out the execution with id enforce-maven-version.  This is incompatible with the atlassian maven version

@TODO Figure out how to get this jar-resource-bundles dependency

fire up a bamboo
> atlas-run