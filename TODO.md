* Optimisation
  * [ ] Cache classpath result and reuse it for next run (+ option to force recomputation)
  * [ ] Optimize startup time
  * [ ] Reduce size (2.5M)
* Ecosystem integration
  * [ ] Support maven' settings.xml (proxy, local repo location)
  * [ ] Provide a mini-gui text editor to ease experiment, ...
  * [ ] Add-ons for IDE, Text Editor 
  * [ ] Support Windows (to test + to fix)
  * [ ] provide roj as a lib (class launcher) : test, doc, ...
* UI : Options, Commands
  * [x] --roj-verbose : log level INFO
  * [x] --roj-quiet : log level ERROR/SEVERE
  * [x] --roj-debug : log level ALL
  * [ ] --roj-help display usage
  * [ ] -Jxxx to provide jvm args
  * [ ] --roj-clean-cache : to clean persistant cache (old version compiled + not compiled)
  * [ ] --roj-display-cmd : display the running classpath + mainClass + args
  * [ ] --roj-dependency-tree: display dependency tree
* UI : Misc
  * [ ] Add logs for download
  * [ ] conversion table : url (gist, pastebin, github, bitbucket, xxxx, relative path) => artifact + properties ?
  * [ ] on exception, dump to stderr : log, context (classpath, dependency tree, ...)
* CompilerService for : 
  * [x] java (version of the current jvm)
  * [x] scala 2.9.x
  * [ ] scala 2.10.x
  * [x] javascript via rhinojs
  * [ ] javascript (via bsf) ?
  * [ ] beanshell ?
  * [ ] groovy ?
  * [ ] closure ?
* Promote, documentation, ...
  * [ ] announce : psug, scala (implicit.ly, scala-tools), tweet, ...
  * [ ] add donation links (README, doc, help)
* Integration test
  * [ ] analyze samples : classpath, className, args
  * [ ] use script as lib/plugins
    * [ ] from another script
    * [ ] from a regular java (dynamic)
