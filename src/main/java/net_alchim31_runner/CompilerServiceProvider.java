package net_alchim31_runner;

import java.io.File;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;

import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;

public class CompilerServiceProvider implements Service{
  private ScriptService _ss;

  //TODO add an option to not search/resolve/create dependency (OPTIM, user place every jar manually in roj's classpath)
  //TODO may be define compiler as a dependency and use same classpath for compile and run ?
  @SuppressWarnings("resource")
  private CompilerContext find(FileObject src, List<File> classpath) throws Exception {
    CompilerContext b = null;
    String path = src.toUri().getPath();
    if (path.endsWith(".java")) {
      b = new CompilerContext();
      b.cs = new CompilerService4Java();
      b.loader = Thread.currentThread().getContextClassLoader();
    } else if (path.endsWith(".js")) {
      Properties p = new Properties(System.getProperties());
      p.setProperty("rojVersion", "0.3.0-SNAPSHOT");
      ScriptInfo siCompiler = _ss.findScriptInfo(new URI("classpath:/net_alchim31_runner/CompilerService4Rhino.java"), p);
      List<File> cpCompiler = _ss.newClasspath(siCompiler);
      ClassLoader cl = new URLClassLoader(FileUtils.toURLs(cpCompiler), Thread.currentThread().getContextClassLoader());
      b = new CompilerContext();
      b.cs = (CompilerService) cl.loadClass("net_alchim31_runner.CompilerService4Rhino").newInstance();
      b.loader = cl;
    } else if (path.endsWith(".scala")) {
      String scalaVersion = findScalaVersion(src.toUri(), classpath);
      Properties p = new Properties(System.getProperties());
      p.setProperty("rojVersion", "0.3.0-SNAPSHOT");
      p.setProperty("scalaVersion", scalaVersion);
      String scalaSuffix = scalaVersion.substring(0, scalaVersion.indexOf('.', 2)).replace('.', '_');
      ScriptInfo siCompiler = _ss.findScriptInfo(new URI("classpath:/net_alchim31_runner/CompilerService4Scala_" + scalaSuffix + ".java"), p);
      List<File> cpCompiler = _ss.newClasspath(siCompiler);
      //Collections.reverse(cpCompiler);
      ClassLoader cl = new URLClassLoader(FileUtils.toURLs(cpCompiler), Thread.currentThread().getContextClassLoader());
      b = new CompilerContext();
      b.cs = (CompilerService) cl.loadClass("net_alchim31_runner.CompilerService4Scala_" + scalaSuffix).newInstance();
      b.loader = cl;
    }
    return b;
  }

  private String findScalaVersion(URI uri, List<File> classpath) throws Exception {
    String scalaVersion = null;
    Pattern p = Pattern.compile("scala-library-(.*)\\.jar");
    for (File f: classpath) {
      Matcher m = p.matcher(f.getName());
      if (m.matches()) {
        scalaVersion = m.group(1);
      }
    }
    if (scalaVersion == null) {
      throw new IllegalStateException(uri + " should depend of scala-library");
    }
    return scalaVersion;
  }
  
//  private CompilerContext loadFrom(String clazzStr, DefaultArtifact... deps) throws Exception {
//    ClassWorld w = new ClassWorld("zero", null);
//    w.newRealm("runner", getClass().getClassLoader());
//    Strategy s = new SelfFirstStrategy(w.newRealm("scalaScript", null));
//    ClassRealm rScript = s.getRealm();
//    //rScript.setParentClassLoader(getClass().getClassLoader());
//    rScript.importFrom("runner", this.getClass().getPackage().getName());
//    rScript.importFrom("runner", "javax.tools");
//    
//    DependencyService ds = _locator.getService(DependencyService.class);
//    RepositorySystemSession session = ds.newSession();
//    List<RemoteRepository> repositories = new LinkedList<>();
//    repositories.add(new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/"));
//    List<Dependency> dependencies = new LinkedList<>();
//    for (DefaultArtifact dep : deps) {
//      dependencies.add(new Dependency(dep, "compile"));
//    }
//    //ArtifactDescriptorReader adr = locator.getService(ArtifactDescriptorReader.class);
//    //ArtifactDescriptorResult ad = adr.readArtifactDescriptor(session, new ArtifactDescriptorRequest(artifact, null, null));
//    DependencyService.ResolveResult r = ds.resolve(session, dependencies, new LinkedList<Dependency>(), repositories);
//    for (File f : r.resolvedFiles) {
//      rScript.addURL(f.toURI().toURL());
//    }
//    ClassLoader cl = rScript;
//    Class<CompilerService> clazz = (Class<CompilerService>)cl.loadClass(clazzStr);
//    CompilerContext cc = new CompilerContext();
//    cc.cs = clazz.newInstance();
//    cc.loader = cl;
//    return cc;
//  }
  
  boolean compileToJar(File dest, FileObject src, List<File> classpath, List<String> options, DiagnosticListener<javax.tools.FileObject> diagnostics) throws Exception {
    System.err.println("Prepare compiler...");
    CompilerContext cc = find(src, classpath);
    if (cc == null) {
      throw new IllegalStateException("no compilers accept " + src);
    }
    ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
    ClassLoader compilerCl = cc.loader;
    try {
      Thread.currentThread().setContextClassLoader(compilerCl);
      File dir = new File(dest.getAbsolutePath() + ".d");
      if (dir.isDirectory()) {
        FileUtils.cleanDirectory(dir);
      } else {
        dir.mkdirs();
      }
      System.err.println("Compiling...");
      System.err.println("try to compile " + src.toUri());
      System.err.println("classpath: \n\t" + StringUtils.join(classpath.iterator(), "\n\t"));
      ClassLoader cl0 = Thread.currentThread().getContextClassLoader();
      if (cl0 instanceof URLClassLoader) {
        URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        System.err.println("compile classpath: \n\t" + StringUtils.join(cl.getURLs(), "\n\t"));
      }
      
      
      boolean b = cc.cs.compileToDir(dir, src, classpath, options, diagnostics);
      if (b) {
        System.err.println("Making jar...");
        FileUtils.jar(dest, dir, new Manifest());
        FileUtils.deleteDirectory(dir);
      }
      return b;
    } catch(Error | Exception exc) {
      System.err.println("try to compile " + src.toUri());
      System.err.println("classpath: \n\t" + StringUtils.join(classpath.iterator(), "\n\t"));
      ClassLoader cl0 = Thread.currentThread().getContextClassLoader();
      if (cl0 instanceof URLClassLoader) {
        URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        System.err.println("compile classpath: \n\t" + StringUtils.join(cl.getURLs(), "\n\t"));
      }
      throw exc;
    } finally {
      if (currentCl != compilerCl) {
        Thread.currentThread().setContextClassLoader(currentCl);
        ((URLClassLoader)compilerCl).close();
      }
    }
  }  
  @Override
  public void initService(ServiceLocator locator) {
    _ss = locator.getService(ScriptService.class);
    
  }
  
  static class  CompilerContext{
    CompilerService cs;
    ClassLoader loader;
  }
}
