package net_alchim31_runner;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;

import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
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
  private final HashMap<URI, CacheEntry> _map = new HashMap<URI, CacheEntry>();
  private ServiceLocator _locator;

  public ScriptService() {
  }
  
  // TODO build a more clean/efficient parsing of the data (may be with parboiled ?)
  // HACKME protected to allow unit test
  private ScriptInfo newScriptInfo(URI uri) throws Exception {
    ScriptInfo b = new ScriptInfo(newArtifact(uri));
    return parseData(uri, b);
  }

  // TODO build a more clean/efficient parsing of the data (may be with parboiled ?)
  private ScriptInfo parseData(URI uri, ScriptInfo out) throws Exception {
    Pattern setRegEx = Pattern.compile("set\\s+(\\S+)\\s+(\\S+)");
    Pattern repoM2RegEx = Pattern.compile("repo\\s+(\\S+)\\s+m2:((http|file):\\S+)");
    //Pattern repoRawRegEx = Pattern.compile("repo\\s+(\\S+)\\s+raw:((http|file|dir):\\S*\\$\\{artifactId\\}\\S+)$");
    // Pattern artifactRegEx =
    // Pattern.compile("from\\s+([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+)(:([\\w\\-\\._]+))?"
    // );
    // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
    Pattern artifactRegEx = Pattern.compile("from\\s+(\\S+)");
    Pattern mainClassRegEx = Pattern.compile("mainClassName\\s+(\\S+)");
    String data = findContent(uri);
    StringTokenizer t = new StringTokenizer(data, "\n\r", false);
    HashMap<String, Object> props = new HashMap<String, Object>();
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
          out.repositories.add(new RemoteRepository(m.group(1), "default", interpolate(m.group(2), props)));
        } else if ((m = artifactRegEx.matcher(stmt)) != null && m.matches()) {
          String from = m.group(1);
          if (from.startsWith("dir:")) {
            String dir = uri.toString();
            dir = dir.substring(0, dir.lastIndexOf('/') + 1);
            from = dir + from.substring("dir:".length());
          }
          from = interpolate(from, props);
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

  private String interpolate(String txt, Map<String, Object> props) {
    String b = txt;
    if (props.size() > 0 && b.indexOf("${") > 0) {
      b = StringUtils.interpolate(b, props);
    }
    if (b.indexOf("${") > 0) { 
      b = StringUtils.interpolate(b, System.getProperties());
    }
    return b;
  }
  // TODO define (plugable ?) strategy for remote uri
  // eg :
  // https://gist.github.com/raw/4183893/962aa266b58511f277ca5a163aca572206bc13ca/HelloWorld.java
  private Artifact newArtifact(URI uri) throws Exception {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    String version = "0.0.0-SNAPSHOT";
    String extension = "jar";//FileUtils.extension(uri.getPath());
    String artifactId = encode(md5.digest(uri.getPath().getBytes("utf-8")));
    String groupId = ScriptInfo.GROUPID_PREFIX + ("file".equals(uri.getScheme())? "local" : uri.getHost());
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

  //TODO to optimize
  private String encode(byte[] v) {
    byte[] e0 = Base64.encodeBase64(v);
    int padd = 0;
    for(int i= e0.length -1; i > -1; i--) {
      if (e0[i] == '+') e0[i] = '.';
      else if (e0[i] == '/') e0[i] = '-';
      else if (e0[i] == '=') padd++;
    }
    return new String(e0, 0, e0.length - padd);
  }
  
  public CacheEntry findOrCreate(URI key) throws Exception {
    CacheEntry b = _map.get(key);
    if (b == null) {
      b = new CacheEntry(key);
      _map.put(key, b);
    }
    return b;
  }

  public String findContent(URI key) throws Exception {
    CacheEntry b = findOrCreate(key);
    if (b.content == null) {
      try(InputStream input = key.toURL().openStream()) {
        b.content = IOUtil.toString(input, "UTF-8");
      }
    }
    return b.content;
  }

  public FileObject findFileObject(final URI key) throws Exception {
    return new StringFileObject(key, findContent(key));
  }

  public ScriptInfo findScriptInfo(URI key) throws Exception {
    CacheEntry b = findOrCreate(key);
    if (b.info == null) {
      b.info = newScriptInfo(b.uri);
    }
    return b.info;
  }

  private static class CacheEntry {
    URI        uri;
    String     content;
    ScriptInfo info;

    public CacheEntry(URI uri0) {
      super();
      this.uri = uri0;
    }
  }
  
  /**
   * @param si the input ScriptInfo
   * @return the List of jar, the first file is the jar resulting from si compilation
   * @throws Exception
   */
  public List<File> findClasspath(URI v) throws Exception {
    ScriptInfo si = findScriptInfo(v);
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
    if (!needCompilation && "file".equals(v.getScheme())) {
      // TODO use checksum instead of lastModified (check performance and quality) 
      needCompilation = jar.lastModified() <= new File(v.getPath()).lastModified();  
    }
    if (needCompilation) {
      FileObject src = findFileObject(v);
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


