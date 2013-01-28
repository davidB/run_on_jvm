# Overview

run_on_jvm run a script written in java, javascript or scala (other jvm lang should be possible, contributions are welcome).

Some possible usages :

* run shared sample code via gist, email with all the dependencies info (cf syntax bellow)
* run build script (I will use it for [plob](https://github.com/davidB/plob) )
* run single source file tool, lib, plugin
* use the jvm like python, ruby,... for shell scripting

## Alternatives

"If you know some, tell me".

### For Scala only:

* [scalascriptengine](http://code.google.com/p/scalascriptengine/)
* [Scala for scripting](http://fr.slideshare.net/day/15-5e-scripting-drig) from Michael Dürig 2010-04-30
* [Twitter's util-eval](https://github.com/twitter/util), usage example in [Why Config?](http://robey.lag.net/2012/03/26/why-config.html) 2012-03-26
* stackoverflow :
  * [eval in scala](http://stackoverflow.com/questions/1183645/eval-in-scala)
  * [How can I get automatic dependency resolution in my scala scripts?](http://stackoverflow.com/questions/7600189/how-can-i-get-automatic-dependency-resolution-in-my-scala-scripts)
* [System Scripting with Scala](http://timperrett.com/2011/08/01/system-scripting-with-scala/) from Timothy Perrett  2011-08-01
* SBT as script runner with dependencies résolution [Scripts, REPL,and Dependencies](http://www.scala-sbt.org/release/docs/Detailed-Topics/Scripts)

### Inspirations :

* [POMStrap](http://jfluid.com/) is able to load application dependency as they are declared in Maven 2 project file, and execute each dependency in its own classloader without inhering from its parent avoiding version conflict.
* [MOP](http://mop.fusesource.org/) is a small utility for executing Java programs which are stored as artifacts like jars or bundles in a Maven repository.
* [Conscript](https://github.com/n8han/conscript) is a tool for installing and updating Scala software programs. It does less than you think, because the [sbt launcher](https://github.com/harrah/xsbt/tree/0.13/launch) does more than you think.
* Some plugins for build tool : [Exec Maven Plugin](http://mojo.codehaus.org/exec-maven-plugin/), [sbt-start-script](https://github.com/sbt/sbt-start-script)

## Features

* Compile and Run script *.java
* Compile and Run script *.scala
* Cache compilation result and reuse it for next run
* Download dependencies (direct + transitive) into local cache ($HOME/.m2/repository) from maven's repositories
* Build classpath from local cache
* Download compiler jar (scala-compiler, rhinojs) the first time a .scala or .js need to be compiled (could take few minutes)

Every files under examples directory should works !

see [TODO](TODO.md) for next release

## Install

0. Install jdk-1.7
1. Download & Unarchive in target dir:

        cd $HOME/apps
        curl -0 http://repo1.maven.org/maven2/net/alchim31/runner/run_on_jvm/0.3.0/run_on_jvm-0.3.0-app.tar.gz | tar -xz

2. Update PATH

        export PATH=$HOME/apps/run_on_jvm-0.3.0/bin:$PATH
        #or
        ln -s $HOME/apps/run_on_jvm-0.3.0/bin/roj $HOME/bin/roj

3. Optional register as env (no tested)

Play !

## Usage

### run locale script

    roj Toto.java
    # to be verbose
    roj --roj-verbose Foo.scala

### run remote script

    roj https://gist.github.com/raw/4183893/69eb99e07c936c9b5315cb3c23bff9eee20d7c5a/HelloWorld.java

### conventions

The compiled scripts are stored in a local cache (as jar) with the following rules :

* local cache = $HOME/.m2/repository
* mainClassName (in default package), can be redefined = filename - extension 
* artifactId = fileName - fileExtension
* groupId = "script.local" for local file and "script.<hostname>" for remote file
* extension = fileExtension + ".jar"
* version = <md5 (hex) of the path>-SNAPSHOT

### configuration (optional)

The configuration is a list of definitions, every definitions starts with "//#".

* Dependency : on a maven's artifact

      //#repo central m2:http://repo1.maven.org/maven2/
      //#from org.apache.commons:commons-lang3:3.1

  see [examples/escape_html_with_deps.java](examples/escape_html_with_deps.java)
  no repositories define by default    
* Dependency : other local script

      //#from file:/${user.home}/myscripts/MakeCoffee.java
      import mytools.MakeCoffee;

* Dependency : other remote script

      //#from https://gist.github.com/raw/4183893/2c2fde5efc78cec941eacb3464fe642d621b0e7d/MyLib.java
      import lib.MyLib;

* mainClassName : redefinition

      //#mainClassName HelloWorld2
      object HelloWorld2 {


### Overhead info

For an already compiled classes with no dependencies :

    ➜  run_on_jvm git:(master) ✗ time java -cp target/local-repo/HelloWorld/drVJ0dfmJbHvG1Qx.iCC9g/HelloWorld-drVJ0dfmJbHvG1Qx.iCC9g.java.jar HelloWorld
    Hello World !
    java -cp  HelloWorld  0,05s user 0,01s system 97% cpu 0,065 total

    ➜  run_on_jvm git:(master) ✗ time java -jar target/run_on_jvm-0.1.0-SNAPSHOT-onejar.jar examples/HelloWorld.java
    Hello World !
    java -jar target/run_on_jvm-0.1.0-SNAPSHOT-onejar.jar examples/HelloWorld.jav  0,20s user 0,02s system 120% cpu 0,183 total

    ➜  run_on_jvm git:(master) ✗ time target/appassembler/bin/roj examples/HelloWorld.java
    Hello World !
    target/appassembler/bin/roj examples/HelloWorld.java  0,24s user 0,03s system 132% cpu 0,206 total


# Contributing

Anyone and everyone is welcome to contribute.

* via issue, doc, blog
* via fork/pull request

## Build

You have to install maven :

    mvn package

# Motivations

I would like :
* to be able to use java/scala (or any other jvm language) as shell scripting (like python, ruby,...).
* to provide single source file tool, lib, plugin

## T0

Scala already have this kind of feature via a command line call:

    $> scala xxxx.scala

or a little more complex via a script like :

	#!/bin/sh
	exec scala -save -nc $0 $*

	# If you have scala in the PATH you can call your script like this one
	# if you need some jar to run the script, use something like the code above
	#SCRIPT="$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"
	#DIR=`dirname "${SCRIPT}"}`
	#exec scala -save -nc -cp "$DIR/lib/aaaaa.jar" $0 $*
	::!#
	// now it's pure scala code

	object Main {
	  def main(args : Array[String]) {
	    println("A simple Scala script")
	    println(args.mkString("[ ", ", ", " ]"))
	  }
	}

BUT I also would like :
* to use maven repositories, ...
* to use script from other script (as lib, plugin, ...)

I choose [Aether](http://wiki.eclipse.org/Aether) to access maven repository because, [Ivy]( (in sbt) generate lot of head-hache in my previous job.

## T1

The first version was start in 2010 an written in scala, but I don't find enough time / need.

## T2

I rewite previous code in java, to be able to run .java and .scala (any version !!), and to be lighter for possible integration.
