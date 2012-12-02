object HelloWorld2 {
  def main(args : String[]) {
    val arg0 = if (args.length > 0) args(0) else ""
    System.out.println("Hello World ! " + arg0)
  }
}