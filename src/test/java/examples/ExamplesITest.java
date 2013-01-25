package examples;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net_alchim31_runner.Main;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

//TODO add test to check failure (missing repository, missing dependency, dependency not found, compilation error)
public class ExamplesITest {

  private File _basedir = new File(System.getProperty("user.dir"), "examples");

  private String runAndCaptureOut(String... args) throws Exception {
    PrintStream out = System.out;
    PipedOutputStream pipeOut = new PipedOutputStream();
    PipedInputStream pipeIn = new PipedInputStream(pipeOut);
    try {
      System.setOut(new PrintStream(pipeOut));
      Main.main(args);
      pipeOut.flush();
      IOUtil.close(pipeOut);
      return IOUtil.toString(pipeIn);
    } finally {
      IOUtil.close(pipeIn);
      System.setOut(out);
    }
  }

  @Test
  public void run_HelloworldJava_NoArgs() throws Exception {
    Assert.assertEquals("Hello World ! \n", runAndCaptureOut(new File(_basedir, "HelloWorld.java").getPath()));
  }
  
  @Test
  public void run_HelloworldJava_NoArgs_from_gist() throws Exception {
    Assert.assertEquals("Hello World ! \n", runAndCaptureOut("https://gist.github.com/raw/4183893/69eb99e07c936c9b5315cb3c23bff9eee20d7c5a/HelloWorld.java"));
  }

  @Test(expected=IllegalStateException.class)
  public void run_InvalidUrl_500_from_gist() throws Exception {
    runAndCaptureOut("https://gist.github.com/raw/4183893/69eb99e07c936c9b5315cb3c23bff9eee20d7c5a33/HelloWorld.java");
  }

  @Test
  public void run_HelloworldJava_WithArgs() throws Exception {
    String s = String.valueOf(Math.random());
    Assert.assertEquals("Hello World ! " + s + "\n", runAndCaptureOut(new File(_basedir, "HelloWorld.java").getPath(), s));
  }

  @Test
  public void run_escape_html_with_deps() throws Exception {
    Assert.assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;\n", runAndCaptureOut(new File(_basedir, "escape_html_with_deps.java").getPath(), "\"bread\" & \"butter\""));
  }
  @Test
  public void run_main_with_local_deps() throws Exception {
    Assert.assertEquals("6\n", runAndCaptureOut(new File(_basedir, "main_with_local_deps.java").getPath(), "3"));
  }
  
  @Test
  public void run_HelloworldJavascript_NoArgs() throws Exception {
    Assert.assertEquals("Hello World ! \n", runAndCaptureOut(new File(_basedir, "HelloWorld3.js").getPath()));
  }
  
  @Test
  public void run_HelloworldScala_WithArgs() throws Exception {
    String s = String.valueOf(Math.random());
    Assert.assertEquals("Hello World ! " + s + "\n", runAndCaptureOut(new File(_basedir, "HelloWorld2.scala").getPath(), s));
  }
  
  @Test
  public void run_HelloworldScala_WithArgs_from_gist() throws Exception {
    String s = String.valueOf(Math.random());
    Assert.assertEquals("Hello World ! " + s + "\n", runAndCaptureOut("https://gist.github.com/raw/4183893/307b2453782e2cbadb2ff11b09cd12b7d598f7d2/HelloWorld2.scala", s));
  }
  

  public void run_HelloworldScala_invalid() throws Exception {
    String s = String.valueOf(Math.random());
    Assert.assertEquals("Hello World ! " + s + "\n", runAndCaptureOut(new File(_basedir, "HelloWorld2_invalid.scala").getPath(), s));
  }
  
  //Test time need to evaluate javascript (lot of shorter than compile (Rhino) + make jar + run)
  @Test
  public void t() throws Exception {
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    try {
      jsEngine.eval("java.lang.System.out.println(2*2)");
    } catch (ScriptException ex) {
      ex.printStackTrace();
    }
  }
}
