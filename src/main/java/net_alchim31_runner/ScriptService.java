package net_alchim31_runner;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
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
    Pattern repoRawRegEx = Pattern.compile("repo\\s+(\\S+)\\s+raw:((http|file|dir):\\S+\\$\\{artifactId\\}\\S+)$");
    // Pattern artifactRegEx =
    // Pattern.compile("from\\s+([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+)(:([\\w\\-\\._]+))?"
    // );
    // <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
    Pattern artifactRegEx = Pattern.compile("from\\s+(\\S+)");
    String data = findContent(uri);
    StringTokenizer t = new StringTokenizer(data, "\n\r", false);
    HashMap<String, Object> props = new HashMap<String, Object>();
    props.put(ScriptInfo.mainClassName, out.artifact.getArtifactId());

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
        } else if ((m = repoRawRegEx.matcher(stmt)) != null && m.matches()) {
          String uriPattern = m.group(2);
          if (uriPattern.startsWith("dir:")) {
            String dir = uri.toString();
            dir = dir.substring(0, dir.lastIndexOf('/') + 1);
            uriPattern = dir + uriPattern.substring("dir:".length());
          }
          out.repositories.add(new RemoteRepository(m.group(1), "raw", StringUtils.interpolate(uriPattern, props)));
        } else if ((m = artifactRegEx.matcher(stmt)) != null && m.matches()) {
          out.dependencies.add(new Dependency(new DefaultArtifact(StringUtils.interpolate(m.group(1), props)), "compile"));
        }
      }
    }
    out.properties.putAll(props);
    return out;
  }

  // TODO define (plugable ?) strategy for remote uri
  // eg :
  // https://gist.github.com/raw/4183893/962aa266b58511f277ca5a163aca572206bc13ca/HelloWorld.java
  private Artifact newArtifact(URI uri) throws Exception {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    String version = "0.0.0";
    String extension = FileUtils.extension(uri.getPath());
    String artifactId = FileUtils.basename(uri.getPath(), "." + extension);
    String groupId = uri.getScheme() == "file" ? "local.script" : uri.getHost();
    Artifact b = new DefaultArtifact(groupId, artifactId, extension, version);

    Map<String, String> props = new HashMap<String,String>(b.getProperties());
    props.put("type", "script");
    props.put("language", "java"); // TODO should be change to follow the
    props.put("uri", uri.toString());

    if ("file".equals(uri.getScheme())) {
      String data = findContent(uri);
      b = b.setVersion(encode(md5.digest(data.getBytes("utf-8"))));
      b = b.setFile(new File(uri.getPath()));
      props.put("localPath", uri.getPath());
    }
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

  @SuppressWarnings({ "resource" })
  public String findContent(URI key) throws Exception {
    CacheEntry b = findOrCreate(key);
    if (b.content == null) {
      InputStream input = key.toURL().openStream();
      try {
        b.content = IOUtil.toString(input, "UTF-8");
      } finally {
        IOUtil.close(input);
      }
    }
    return b.content;
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

  @Override
  public void initService(ServiceLocator locator) {
    // TODO Auto-generated method stub
    
  }
}
