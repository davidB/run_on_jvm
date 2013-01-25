//#repo central m2:http://repo1.maven.org/maven2/
//#from org.scala-lang:scala-library:2.9.2
object HelloWorld2_9 {
  def main(args : Array[String]) {
    val arg0 = if (args.length > 0) args(0) else ""
    val v = scala.util.Properties.scalaPropOrElse("version.number", "unknown")
    println(v + " Hello World ! " + arg0)
  }
}