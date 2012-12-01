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

