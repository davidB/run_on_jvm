package net_alchim31_runner;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.maven.wagon.AhcWagon;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

//TODO remove useless service, cache when no longer needed (after compile) 
public class Main {
  public static final List<String> EMPTY_LIST_STRING = Collections.emptyList();
  public static final Logger       logger            = LoggerFactory.getLogger("roj");

  // TODO filter and use args like --roj-xxxx
  public static void main(String[] args) throws Exception {
    URI uri = null;
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      lc.getLogger("roj").setLevel(Level.WARN);
      ArrayList<String> subArgs = new ArrayList<>(args.length);
      for (String arg : args) {
        if ("--roj-verbose".equals(arg)) {
          lc.getLogger("roj").setLevel(Level.INFO);
        } else if ("--roj-quiet".equals(arg)) {
          lc.getLogger("roj").setLevel(Level.ERROR);
        } else if ("--roj-debug".equals(arg)) {
          lc.getLogger("roj").setLevel(Level.ALL);
        } else if (arg.startsWith("--roj-")) {
          //ignore
        } else if (uri == null) {
          uri = new URI(arg);
        } else {
          subArgs.add(arg);
        }
      }
      if (uri == null) {
        logger.error("uri undefined");
        return;
      }
      if (!uri.isAbsolute()) {
        uri = new File(uri.toString()).getAbsoluteFile().toURI();
      }
      run(compile(uri).addArgs(subArgs));
    } catch (Exception exc) {
      logger.error("uri : '" + uri + "'", exc);
      throw new IllegalStateException("can't run : " + uri, exc);
    }
  }

  static ServiceLocator newServiceLocator() {
    /*
     * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
     * prepopulated DefaultServiceLocator, we only need to register the repository connector factories.
     */
    MavenServiceLocator locator = new MavenServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
    locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
    locator.setServices(WagonProvider.class, new WagonProvider() {
      @Override
      public Wagon lookup(String roleHint) throws Exception {
        if ("http".equals(roleHint)) {
          return new AhcWagon();
        }
        return null;
      }

      @Override
      public void release(Wagon wagon) {}
    });
    locator.addService(CompilerServiceProvider.class, CompilerServiceProvider.class);
    locator.setService(DependencyService.class, DependencyService.class);
    locator.setService(ScriptService.class, ScriptService.class);
    locator.setService(DefaultArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
    // locator.setService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
    locator.setService(ArtifactDescriptorReader.class, ArtifactDescriptorReader4Script.class);
    locator.setService(WorkspaceReader4Script.class, WorkspaceReader4Script.class);
    return locator;
  }

  // TODO fork a new process if JvmArg or fork options
  public static void run(RunInfo v) throws Exception {
    URL[] urls = FileUtils.toURLs(v.classpath);
    try (URLClassLoader cl = new URLClassLoader(urls, null/* ClassLoader.getSystemClassLoader() */);) {
      // TODO Switch current ClassLoader
      Class<?> clazz = cl.loadClass(v.className);
      Method m = clazz.getMethod("main", String[].class);
      m.invoke(null, (Object) v.args.toArray(new String[] {}));
    } catch (Exception exc) {
      Main.logger.error("Failed to run : {}", v.className);
      Main.logger.error("classloader's url : {}", Arrays.toString(urls));
      throw exc;
    }
  }

  public static RunInfo compile(URI v) throws Exception {
    ServiceLocator locator = newServiceLocator();
    ScriptService ss = locator.getService(ScriptService.class);
    ScriptInfo si = ss.findScriptInfo(v, null);
    List<File> classpath = ss.newClasspath(si);
    return new RunInfo(si.mainClassName, classpath, EMPTY_LIST_STRING, EMPTY_LIST_STRING);
  }

  public static String toString(URI v) throws Exception {
    try (InputStream is = v.toURL().openStream()) {
      return IOUtil.toString(is);
    }
  }

}

class RunInfo {
  public final String       className;
  public final List<String> args;
  public final List<File>   classpath;
  public final List<String> jvmArgs;

  public RunInfo(String className0, List<File> classpath0, List<String> args0, List<String> jvmArgs0) {
    super();
    this.classpath = classpath0;
    this.className = className0;
    this.args = args0;
    this.jvmArgs = jvmArgs0;
  }

  public RunInfo addArgs(List<String> v) throws Exception {
    List<String> a = new ArrayList<String>(args.size() + v.size());
    a.addAll(args);
    a.addAll(v);
    return new RunInfo(className, classpath, a, jvmArgs);
  }
}
