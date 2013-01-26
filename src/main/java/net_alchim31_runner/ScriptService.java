package net_alchim31_runner;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * Note Thread-Safe
 */
public class ScriptService implements Service {

  private final HashMap<URI, ScriptInfo> _map = new HashMap<URI, ScriptInfo>();
  private ServiceLocator _locator;

  public ScriptService() {
  }
  
  // TODO build a more clean/efficient parsing of the data (may be with parboiled ?)
  // HACKME protected to allow unit test
  private ScriptInfo newScriptInfo(FileObject fo) throws Exception {
    URI uri = fo.toUri();
    ScriptInfo b = new ScriptInfo(newArtifact(uri), fo);
    return parseData(b);
  }

  // TODO build a more clean/efficient parsing of the data (may be with parboiled ?)
  private ScriptInfo parseData(ScriptInfo out) throws Exception {
    Pattern setRegEx = Pattern.compile("set\\s+(\\S+)\\s+(\\S+)");
    Pattern repoM2RegEx = Pattern.compile("repo\\s+(\\S+)\\s+m2:((http|file):\\S+)");
    //Pattern repoRawRegEx = Pattern.compile("repo\\s+(\\S+)\\s+raw:((http|file|dir):\\S*\\$\\{artifactId\\}\\S+)$");
    // Pattern artifactRegEx =
    // Pattern.compile("from\\s+([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+)(:([\\w\\-\\._]+))?"
    // );
    // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
    Pattern artifactRegEx = Pattern.compile("from\\s+(\\S+)");
    Pattern mainClassRegEx = Pattern.compile("mainClassName\\s+(\\S+)");
    URI uri = out.src.toUri();
    CharSequence data = out.src.getCharContent(true);
    StringTokenizer t = new StringTokenizer(data.toString(), "\n\r", false);
    Properties props = new Properties(System.getProperties());
    out.mainClassName = FileUtils.basename2(uri.getPath());

    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int p = line.indexOf("//#");
      if (p > -1) {
        String stmt = line.substring(p + 3);
        Matcher m;
        if ((m = setRegEx.matcher(stmt)) != null && m.matches()) {
          props.put(m.group(1), m.group(2));
        } else if ((m = repoM2RegEx.matcher(stmt)) != null && m.matches()) {
          out.repositories.add(new RemoteRepository(m.group(1), "default", StringUtils.interpolate(m.group(2), props)));
        } else if ((m = artifactRegEx.matcher(stmt)) != null && m.matches()) {
          String from = m.group(1);
          if (from.startsWith("dir:")) {
            String dir = uri.toString();
            dir = dir.substring(0, dir.lastIndexOf('/') + 1);
            from = dir + from.substring("dir:".length());
          }
          from = StringUtils.interpolate(from, props);
          if (from.startsWith("http:") || from.startsWith("file:")) {
            out.dependencies.add(new Dependency(newArtifact(new URI(from)), "compile"));
          } else {
            out.dependencies.add(new Dependency(new DefaultArtifact(from), "compile"));
          }
        } else if ((m = mainClassRegEx.matcher(stmt)) != null && m.matches()) {
          out.mainClassName = m.group(1);
         }
      }
    }
    return out;
  }

  // TODO define (plugable ?) strategy for remote uri
  // eg :
  // https://gist.github.com/raw/4183893/962aa266b58511f277ca5a163aca572206bc13ca/HelloWorld.java
  private Artifact newArtifact(URI uri) throws Exception {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    String version = StringUtils.toHex(md5.digest(uri.getPath().getBytes("utf-8"))) + "-SNAPSHOT";
    String extension = FileUtils.extension(uri.getPath()) + ".jar";
    String artifactId = FileUtils.basename2(uri.getPath());
    String groupId = ScriptInfo.GROUPID_PREFIX + ("file".equals(uri.getScheme())? "local" : uri.getHost().replace('.', '_'));
    Artifact b = new DefaultArtifact(groupId, artifactId, extension, version);

    Map<String, String> props = new HashMap<String,String>(b.getProperties());
    props.put(ScriptInfo.KEY_TYPE, ScriptInfo.VALUE_TYPE);
    props.put(ScriptInfo.KEY_URI, uri.toString());
    props.put("language", FileUtils.extension(uri.getPath()));
    
//    if ("file".equals(uri.getScheme())) {
//      props.put(ScriptInfo.KEY_LOCALPATH, uri.getPath());
//    }
    b = b.setProperties(props);
    return b;
  }

  private FileObject newFileObject(final URI key) throws Exception {
    String content = "";
    try(InputStream input = key.toURL().openStream()) {
      content = IOUtil.toString(input, "UTF-8");
    }
    return new StringFileObject(key, content);
  }

  public ScriptInfo findScriptInfo(URI key) throws Exception {
    ScriptInfo b = _map.get(key);
    if (b == null) {
      b = newScriptInfo(newFileObject(key)) ;
      _map.put(key, b);
    }
    return b;
  }
  
  /**
   * Create a runtime classpath including dependencies ( + transitives) and the jar of the script (result of the compilation) 
   * * every file (jars) in the classpath are downloaded in cache if needed
   * * download jar and dependencies if needed
   * * compile the script into the jar (if jar doesn't exist or older than the (local) script (no check for remote script))
   *  
   * @param si the script (entry point)
   * @return the List of jar, the first file is the jar resulting from script compilation
   * @throws Exception
   */
  public List<File> newClasspath(ScriptInfo si) throws Exception {
    URI uri = si.src.toUri();
    DependencyService ds = _locator.getService(DependencyService.class);
    RepositorySystemSession session = ds.newSession();

    //    File jar = new File(src.getAbsolutePath() + ".jar");
    List<File> classpath = new LinkedList<File>();
    if (si.dependencies.size() > 0) {
      //ArtifactDescriptorReader adr = locator.getService(ArtifactDescriptorReader.class);
      //ArtifactDescriptorResult ad = adr.readArtifactDescriptor(session, new ArtifactDescriptorRequest(artifact, null, null));
      DependencyService.ResolveResult r = ds.resolve(session, si.dependencies, si.managedDependencies, si.repositories);
      classpath = r.resolvedFiles;
    }
    File jar = new File(session.getLocalRepository().getBasedir(), session.getLocalRepositoryManager().getPathForLocalArtifact(si.artifact));

    boolean needCompilation = !jar.exists();
    if (!needCompilation && "file".equals(uri.getScheme())) {
      // TODO use checksum instead of lastModified (check performance and quality)
      needCompilation = jar.lastModified() <= new File(uri.getPath()).lastModified();
    }
    if (needCompilation) {
      FileObject src = si.src;
      CompilerService cs = null;
      for (CompilerService cs0 : _locator.getServices(CompilerService.class)) {
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
    classpath.add(0, jar);
    return classpath;
  }

  @Override
  public void initService(ServiceLocator locator) {
    _locator = locator;
  }
}


