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
import java.util.LinkedList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.MavenServiceLocator;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.maven.wagon.AhcWagon;

//TODO remove useless service, cache when no longer needed (after compile) 
public class Main {
	public static final List<String> EMPTY_LIST_STRING = Collections.emptyList();
	
	//TODO filter and use args like --roj-xxxx
	public static void main(String[] args) {
	  URI uri = null;
		try {
		  uri = new URI(args[0]);
		  if (!uri.isAbsolute()) {
		    uri = new File(args[0]).getAbsoluteFile().toURI();
		  }
			run(compile(uri).addArgs(args, 1));
		} catch(Exception exc) {
		  System.err.println("uri : '" + uri + "'");
			exc.printStackTrace();
		}
	}
	
  static ServiceLocator newServiceLocator() {
    /*
     * Aether's components implement org.eclipse.aether.spi.locator.Service to
     * ease manual wiring and using the prepopulated DefaultServiceLocator, we
     * only need to register the repository connector factories.
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
    locator.addService(CompilerService.class, CompilerService4Java.class);
    locator.addService(CompilerService.class, CompilerService4Scala_2_9.class);
    locator.addService(CompilerService.class, CompilerService4Rhino.class);
    locator.setService(DependencyService.class, DependencyService.class);
    locator.setService(ScriptService.class, ScriptService.class);
    locator.setService(DefaultArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
    locator.setService(ArtifactDescriptorReader.class, ArtifactDescriptorReader4Script.class);
    return locator;
  }
  
  
  //TODO fork a new process if JvmArg or fork options
	public static void run(RunInfo v) throws Exception {
		URL[] urls = new URL[v.classpath.size()];
		int i = 0;
		for(URI uri : v.classpath) {
			urls[i] = uri.toURL();
			i++;
		}
		try(URLClassLoader cl = new URLClassLoader(urls, null/*ClassLoader.getSystemClassLoader()*/);){
		  //TODO Switch current ClassLoader
			Class<?> clazz = cl.loadClass(v.className);
			Method m = clazz.getMethod("main", String[].class);
			m.invoke(null, (Object)v.args.toArray(new String[]{}));
		} catch(Exception exc) {
		  System.err.println("classloader's url : " + Arrays.toString(urls));
		  throw exc;
		}
	}
	
	public static RunInfo compile(URI v) throws Exception {
	  ServiceLocator locator = newServiceLocator();
	  ScriptService ss = locator.getService(ScriptService.class);
    ScriptInfo si = ss.findScriptInfo(v);
    DependencyService ds = locator.getService(DependencyService.class);
    RepositorySystemSession session = ds.newSession();

    //    File jar = new File(src.getAbsolutePath() + ".jar");
    List<File> classpath = new LinkedList<File>();
    if (si.dependencies.size() > 0) {
      //ArtifactDescriptorReader adr = locator.getService(ArtifactDescriptorReader.class);
      //ArtifactDescriptorResult ad = adr.readArtifactDescriptor(session, new ArtifactDescriptorRequest(artifact, null, null));
      DependencyService.ResolveResult r = ds.resolve(session, si.dependencies, si.managedDependencies, si.repositories);
      classpath = r.resolvedFiles;
    }
    File jar = new File(session.getLocalRepository().getBasedir(), session.getLocalRepositoryManager().getPathForLocalArtifact(si.artifact) + ".jar");
//    System.err.println("dep : " + StringUtils.join(si.dependencies.iterator(), ":"));
//    System.err.println("cp  : " + StringUtils.join(classpath.iterator(), ":"));
//    System.err.println("jar : " + jar);

    if (!jar.exists()) {
      FileObject src = ss.findFileObject(v);
      CompilerService cs = null;
      for (CompilerService cs0 : locator.getServices(CompilerService.class)) {
        if (cs0.accept(src)) {
          cs = cs0;
          break;
        }
      }
      if (cs == null) {
        throw new IllegalStateException("no compilers accept " + src);
      }
      jar.getParentFile().mkdirs();

      DiagnosticCollector<javax.tools.FileObject> diagnostics = new DiagnosticCollector<javax.tools.FileObject>();
      boolean b = cs.compileToJar(jar, src, classpath, new LinkedList<String>(), diagnostics);
      for(javax.tools.Diagnostic<? extends javax.tools.FileObject> d : diagnostics.getDiagnostics()){
          // Print all the information here.
          System.err.format("|%s:%s:%d:%d:%s\n%s\n", d.getKind(), d.getSource().getName(), d.getLineNumber(), d.getColumnNumber(), d.getMessage(null), d.getCode());
      }

      if (!b) {
        throw new Exception("Fail to compile");
      }
    }
    classpath.add(jar);
    return new RunInfo(si.properties.get(ScriptInfo.mainClassName).toString(), FileUtils.toURIs(classpath), EMPTY_LIST_STRING, EMPTY_LIST_STRING);
	}
	
	public static String toString(URI v) throws Exception {
		try(InputStream is = v.toURL().openStream()) {
			return IOUtil.toString(is);
		}
	}

	
}

class RunInfo {
	public final String className;
	public final List<String> args;
	public final List<URI> classpath;
	public final List<String> jvmArgs;

	public RunInfo(String className0, List<URI> classpath0, List<String> args0, List<String> jvmArgs0) {
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

  public RunInfo addArgs(String[] v, int offset) throws Exception {
    List<String> a = new ArrayList<String>(args.size() + v.length);
    a.addAll(args);
    for(int i = offset; i < v.length; i++) {
      a.add(v[i]);
    }
    return new RunInfo(className, classpath, a, jvmArgs);
  }
}

