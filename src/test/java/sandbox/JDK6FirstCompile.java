package sandbox;

import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class JDK6FirstCompile {
  public static void main(String args[]) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int results = compiler.run(null, null, null, "exemples/HelloWorld.java");
    System.out.println("Success: " + (results == 0));
  }
}