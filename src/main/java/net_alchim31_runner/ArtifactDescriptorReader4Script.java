package net_alchim31_runner;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.VersionRequest;
import org.sonatype.aether.resolution.VersionResolutionException;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;

public class ArtifactDescriptorReader4Script implements ArtifactDescriptorReader, Service {
  private ScriptService _scriptService;
  private VersionResolver  _versionResolver;
  private ArtifactDescriptorReader _defaultArtifactDescriptorReader;

  @Override
  public void initService(ServiceLocator locator) {
    _scriptService = locator.getService(ScriptService.class);
    // _logger = locator.getService(Logger.class);
    // setRemoteRepositoryManager(locator.getService(RemoteRepositoryManager.class));
    _versionResolver = locator.getService(VersionResolver.class);
    _defaultArtifactDescriptorReader = locator.getService(DefaultArtifactDescriptorReader.class);
  }

  
  @Override
  //TODO delegate non script artifact to Maven (DefaultArtifactDescriptorReader)
  public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
    ArtifactDescriptorResult result = null;
    if (ScriptInfo.VALUE_TYPE.equals(request.getArtifact().getProperty(ScriptInfo.KEY_TYPE, null))) {
      result = readArtifactDescriptor4Raw(session, request); 
    } else {
      result = _defaultArtifactDescriptorReader.readArtifactDescriptor(session, request);
    }
    if (result == null) {
      result = new ArtifactDescriptorResult(request);
    }
    return result;
  }
  
  
  private ArtifactDescriptorResult readArtifactDescriptor4Raw(RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
    ArtifactDescriptorResult result = new ArtifactDescriptorResult(request);
    try {
      result = checkVersion(session, result);
      result = loadInfoFromScriptSource(result);
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

  private final ArtifactDescriptorResult loadInfoFromScriptSource(ArtifactDescriptorResult result) throws Exception {
    ArtifactDescriptorRequest request = result.getRequest();
    Artifact artifact = request.getArtifact();
//    Artifact sourceArtifact = artifact;
//    if ("java.jar".equals(sourceArtifact.getExtension())) {
//      sourceArtifact = new SubArtifact(sourceArtifact, sourceArtifact.getClassifier(), "java");
//    } else if ("scala.jar".equals(sourceArtifact.getExtension())) {
//      sourceArtifact = new SubArtifact(sourceArtifact, sourceArtifact.getClassifier(), "scala");
//    }
//    ArtifactRequest resolveRequest = new ArtifactRequest(sourceArtifact, request.getRepositories(), request.getRequestContext());
//    ArtifactResult resolveResult = _artifactResolver.resolveArtifact(session, resolveRequest);
//    result.setRepository(resolveResult.getRepository());
//    sourceArtifact = resolveResult.getArtifact();
    return readFromScriptInfo(_scriptService.findScriptInfo(new URI(artifact.getProperty(ScriptInfo.KEY_URI, null)), null), result);
  }

  private final ArtifactDescriptorResult readFromScriptInfo(ScriptInfo info, ArtifactDescriptorResult result) throws Exception {
    result.setArtifact(info.artifact);
    HashMap<String, Object> m = new HashMap<>(result.getProperties());
    m.putAll(info.artifact.getProperties());
    result.setProperties(m);
    result.setDependencies(info.dependencies);
    result.setManagedDependencies(info.managedDependencies);
    result.setRepositories(info.repositories);
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

