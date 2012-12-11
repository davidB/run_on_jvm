package examples;

import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

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
  public void run_HelloworldJava_WithArgs() throws Exception {
    String s = String.valueOf(Math.random());
    Assert.assertEquals("Hello World ! "+ s +"\n", runAndCaptureOut(new File(_basedir, "HelloWorld.java").getPath(), s));
  }
  
  
  @Test
  public void run_escape_html_with_deps() throws Exception {
    Assert.assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;\n", runAndCaptureOut(new File(_basedir, "escape_html_with_deps.java").getPath(), "\"bread\" & \"butter\""));
  }
}
