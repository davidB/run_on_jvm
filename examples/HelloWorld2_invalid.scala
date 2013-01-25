//#repo central m2:http://repo1.maven.org/maven2/
//#from org.scala-lang:scala-library:2.9.2
object HelloWorld2 {
  def main(args : Array[String]) {
    // the following line in invalid zargs doesn't exist
    val arg0 = if (args.length > 0) zargs(0) else ""
    println("Hello World ! " + arg0)
  }
}