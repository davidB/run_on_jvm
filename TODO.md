* TBD in README'feature
* integration test
  * run samples (helloworld (nodeps), with depths, with transitive deps)
  * analyze samples : classpath, className, args
  * use script as lib/plugins
    * from another script
    * from a regular java (dynamic)
* read doc about aether connector
* make a script or launch
* ask confing as list of statement or as front-matter (json)
  * define mainClass
* conversion table : url (gist, pastebin, github, bitbucket, xxxx, relative path) => artifact + properties
  * check gist : raw url + relative path
* use scriptlib to define/provide compilers
* provide run4jl as a lib (class launcher) : test, doc, ...
* announce : psug, scala (implicit.ly, scala-tools)
* on exception, dump to stderr : log, context (classpath, dependency tree, ...)
* command to clean persistant cache (old version compiled + not compiled)
* manage several version of compiler (java_1_6, java_1_7, java_1_8, scala_2_9, scala_2_10,...)
* try to provide CompilerService for : scala, groovy, javascript (via bsf)