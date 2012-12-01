#!/usr/bin/env scala 
# If you have scala in the PATH you can call your script like this one
# if you need some jar to run the script, you have to hard code classpath with absolute path
::!#
// now pure scala code


object Main {
  def main(args : Array[String]) {
    println("A simple Scala script")
    println(args.mkString("[ ", ", ", " ]"))
  }
}

