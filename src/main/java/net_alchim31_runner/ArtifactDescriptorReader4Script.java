package net_alchim31_runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;

public class ArtifactDescriptorReader4Script implements ArtifactDescriptorReader, Service {
  final static String      LAYOUT_M2   = "default";
  final static String      LAYOUT_GIST = "gist";
  
  private ScriptsCache _cache;
  private VersionResolver  _versionResolver;
  private ArtifactResolver _artifactResolver;

  @Override
  public void initService(ServiceLocator locator) {
    _cache = new ScriptsCache();
    // _logger = locator.getService(Logger.class);
    // setRemoteRepositoryManager(locator.getService(RemoteRepositoryManager.class));
    _versionResolver = locator.getService(VersionResolver.class);
    _artifactResolver = locator.getService(ArtifactResolver.class);
    // _artifactDescriptorReaderNext =
    // locator.getService(DefaultArtifactDescriptorReader.class);
  }

  @Override
  //TODO delegate non script artifact to Maven (DefaultArtifactDescriptorReader)
  public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
    ArtifactDescriptorResult result = new ArtifactDescriptorResult(request);
    try {
      result = checkVersion(session, result);
      result = loadInfoFromScriptSource(session, result);
      return result;
    } catch (VersionResolutionException e) {
      result.addException(e);
      throw new ArtifactDescriptorException(result);
    } catch (ArtifactResolutionException e) {
      notify(EventType.ARTIFACT_DESCRIPTOR_MISSING, session, request.getArtifact());
      if (session.isIgnoreMissingArtifactDescriptor()) {
        return null;
      }
      result.addException(e);
      throw new ArtifactDescriptorException(result);
    } catch (FileNotFoundException e) {
      notify(EventType.ARTIFACT_DESCRIPTOR_MISSING, session, request.getArtifact());
      if (session.isIgnoreMissingArtifactDescriptor()) {
        return null;
      }
      result.addException(e);
      throw new ArtifactDescriptorException(result);
    } catch (ArtifactDescriptorException e) {
      throw e;
    } catch (Exception e) {
      notify(EventType.ARTIFACT_DESCRIPTOR_INVALID, session, request.getArtifact());
      if (session.isIgnoreInvalidArtifactDescriptor()) {
        return null;
      }
      result.addException(e);
      throw new ArtifactDescriptorException(result);
    }
  }

  private final ArtifactDescriptorResult checkVersion(RepositorySystemSession session, ArtifactDescriptorResult result) throws Exception {
    ArtifactDescriptorRequest request = result.getRequest();
    Artifact artifact = request.getArtifact();
    VersionRequest versionRequest = new VersionRequest(artifact, request.getRepositories(), request.getRequestContext());
    _versionResolver.resolveVersion(session, versionRequest);
    return result;
  }

  private final ArtifactDescriptorResult loadInfoFromScriptSource(RepositorySystemSession session, ArtifactDescriptorResult result) throws Exception {
    ArtifactDescriptorRequest request = result.getRequest();
    Artifact artifact = request.getArtifact();
    Artifact sourceArtifact = artifact;
    if ("java.jar".equals(sourceArtifact.getExtension())) {
      sourceArtifact = new SubArtifact(sourceArtifact, sourceArtifact.getClassifier(), "java");
    } else if ("scala.jar".equals(sourceArtifact.getExtension())) {
      sourceArtifact = new SubArtifact(sourceArtifact, sourceArtifact.getClassifier(), "scala");
    }
    ArtifactRequest resolveRequest = new ArtifactRequest(sourceArtifact, request.getRepositories(), request.getRequestContext());
    ArtifactResult resolveResult = _artifactResolver.resolveArtifact(session, resolveRequest);
    result.setRepository(resolveResult.getRepository());
    sourceArtifact = resolveResult.getArtifact();
    return readInfoFromScriptSource(_cache.findContent(sourceArtifact.getFile()), result);
  }

  //TODO build a more clean/efficient parsing of the data (may be with parboiled ?)
  //HACKME protected to allow unit test
  protected final ArtifactDescriptorResult readInfoFromScriptSource(String data, ArtifactDescriptorResult result) throws Exception {
    Pattern setRegEx = Pattern.compile("set\\s+(\\S+)\\s+(\\S+)");
    Pattern repoM2RegEx = Pattern.compile("repo\\s+(\\S+)\\s+m2:((http|file):\\S+)");
    Pattern repoRawRegEx = Pattern.compile("repo\\s+(\\S+)\\s+raw:((http|file):\\S+\\$\\{artifactId\\}\\S+)$");
    //Pattern artifactRegEx = Pattern.compile("from\\s+([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+):([\\w\\-\\._\\$\\{\\}]+)(:([\\w\\-\\._]+))?" );
    //<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
    Pattern artifactRegEx = Pattern.compile("from\\s+(\\S+)" );
    StringTokenizer t = new StringTokenizer(data, "\n\r", false);
    Properties props = new Properties();
    while(t.hasMoreTokens()) {
      String line = t.nextToken();
      int p = line.indexOf("//#");
      if (p > -1) {
        String stmt = line.substring(p + 3);
        Matcher m;
        if ((m = setRegEx.matcher(stmt)) != null && m.matches()) {
          props.setProperty(m.group(1), m.group(2));
        } else if ((m = repoM2RegEx.matcher(stmt)) != null && m.matches()) {
          result.addRepository(new RemoteRepository(m.group(1), "default", StringUtils.interpolate(m.group(2), props)));
        } else if ((m = repoRawRegEx.matcher(stmt)) != null && m.matches()) {
          result.addRepository(new RemoteRepository(m.group(1), "raw", StringUtils.interpolate(m.group(2), props)));
        } else if ((m = artifactRegEx.matcher(stmt)) != null && m.matches()) {
          result.addDependency(new Dependency(new DefaultArtifact(StringUtils.interpolate(m.group(1), props)), "compile"));
        }
      }
    }
    return result;
  }

  private void notify(EventType t, RepositorySystemSession session, Artifact artifact) {
    RepositoryListener listener = session.getRepositoryListener();
    if (listener != null) {
      DefaultRepositoryEvent event = new DefaultRepositoryEvent(t, session, null);
      event.setArtifact(artifact);
      listener.artifactDescriptorMissing(event);
    }
  }
}

class ScriptsCache {
  static class Entry {
    File file;
    String content;
    public Entry(File file0, String content0) {
      super();
      this.file = file0;
      this.content = content0;
    }
  }
  private final HashMap<File, Entry> _map = new HashMap<File, Entry>();
  
  @SuppressWarnings({ "null", "resource" })
  public String findContent(File key) throws Exception {
    Entry b = _map.get(key);
    if (b == null) {
      FileInputStream input = new FileInputStream(key);
      try {
        String v = IOUtil.toString(input, "UTF-8");
        b = new Entry(key, v);
        _map.put(key, b);
      } finally {
        IOUtil.close(input);
      }
    }
    return b == null ? null : b.content;
  }
}
