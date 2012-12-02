# Overview

run4jl run a script written in java or scala (other jvm lang should be possible, contributions are welcome).

Some possible usages :

* share runnable sample code via gist, email with all the dependencies info
* create build script (I will use it for [plob](https://github.com/davidB/plob) )
* provide single source file tool
* provide single source file lib, plugin

## Alternatives

"If you know some, tell me".

### Inspirations :

* [POMStrap](http://jfluid.com/) is able to load application dependency as they are declared in Maven 2 project file, and execute each dependency in its own classloader without inhering from its parent avoiding version conflict.
* [MOP](http://mop.fusesource.org/) is a small utility for executing Java programs which are stored as artifacts like jars or bundles in a Maven repository.
* [Conscript](https://github.com/n8han/conscript) is a tool for installing and updating Scala software programs. It does less than you think, because the [sbt launcher](https://github.com/harrah/xsbt/tree/0.13/launch) does more than you think.
* Some plugins for build tool : [Exec Maven Plugin](http://mojo.codehaus.org/exec-maven-plugin/), [sbt-start-script](https://github.com/sbt/sbt-start-script) 

## Features

### DONE

Nothing, I practice RDD (README Driven Development ;-) )

### TBD

* Compile and Run script *.java
* Compile and Run script *.scala
* Cache compilation result and reuse it for next run
* Download dependencies (direct + transitive) into local cache ($HOME/.m2/repository) from maven's repositories
* Build classpath from local caches
* Cache classpath result and reuse it for next run (+ option to force recomputation)
* Support Windows 

## Install

1. Install jdk
2. Download ....
3. Unarchive
4. Update PATH
5. (optional) register as env
TDB

## Usage

### run locale script

TBD

    java -jar run4jl-x.y.z-onejar.jar Toto.java

### run remote script

TDB

### conventions

TBD
* filename - extension == mainClassname (default package)
* version of local files == md5 encoding base64 of the file

# Contribute

* via issue, doc, blog
* via fork/pull requet

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
