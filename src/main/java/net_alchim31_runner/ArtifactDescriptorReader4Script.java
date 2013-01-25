package net_alchim31_runner;

import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.resolution.ArtifactDescriptorException;
import org.sonatype.aether.resolution.ArtifactDescriptorRequest;
import org.sonatype.aether.resolution.ArtifactDescriptorResult;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;

public class ArtifactDescriptorReader4Script implements ArtifactDescriptorReader, Service {
//  final static String      LAYOUT_M2   = "default";
//  final static String      LAYOUT_GIST = "gist";
//  
//  private ScriptService _scriptService;
//  private VersionResolver  _versionResolver;
//  private ArtifactResolver _artifactResolver;
//  private ArtifactDescriptorReader _defaultArtifactDescriptorReader;

  @Override
  public void initService(ServiceLocator locator) {
//    _scriptService = locator.getService(ScriptService.class);
//    // _logger = locator.getService(Logger.class);
//    // setRemoteRepositoryManager(locator.getService(RemoteRepositoryManager.class));
//    _versionResolver = locator.getService(VersionResolver.class);
//    _artifactResolver = locator.getService(ArtifactResolver.class);
//    // _artifactDescriptorReaderNext =
//    _defaultArtifactDescriptorReader = locator.getService(DefaultArtifactDescriptorReader.class);
  }

  
  @Override
  //TODO delegate non script artifact to Maven (DefaultArtifactDescriptorReader)
  public ArtifactDescriptorResult readArtifactDescriptor(RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
//    ArtifactDescriptorResult result = null;
//    HashMap<String, List<RemoteRepository>> reposc = groupByContentType(request.getRepositories());
//    if (reposc.containsKey("default")) {
//      result = _defaultArtifactDescriptorReader.readArtifactDescriptor(
//          session,
//          new ArtifactDescriptorRequest(request.getArtifact(), reposc.get("default"), request.getRequestContext())
//      );
//    }
//    if ((result == null || result.getArtifact() == null) && reposc.containsKey("raw")) {
//      result = readArtifactDescriptor4Raw(
//          session,
//          new ArtifactDescriptorRequest(request.getArtifact(), reposc.get("raw"), request.getRequestContext())
//      );
//    }
//    if (result == null) {
//      result = new ArtifactDescriptorResult(request);
//    }
//    return result;
    return new ArtifactDescriptorResult(request);
  }
  
  
//  private ArtifactDescriptorResult readArtifactDescriptor4Raw(RepositorySystemSession session, ArtifactDescriptorRequest request) throws ArtifactDescriptorException {
//    ArtifactDescriptorResult result = new ArtifactDescriptorResult(request);
//    try {
//      result = checkVersion(session, result);
//      result = loadInfoFromScriptSource(session, result);
//      return result;
//    } catch (VersionResolutionException e) {
//      result.addException(e);
//      throw new ArtifactDescriptorException(result);
//    } catch (ArtifactResolutionException e) {
//      notify(EventType.ARTIFACT_DESCRIPTOR_MISSING, session, request.getArtifact());
//      if (session.isIgnoreMissingArtifactDescriptor()) {
//        return null;
//      }
//      result.addException(e);
//      throw new ArtifactDescriptorException(result);
//    } catch (FileNotFoundException e) {
//      notify(EventType.ARTIFACT_DESCRIPTOR_MISSING, session, request.getArtifact());
//      if (session.isIgnoreMissingArtifactDescriptor()) {
//        return null;
//      }
//      result.addException(e);
//      throw new ArtifactDescriptorException(result);
//    } catch (ArtifactDescriptorException e) {
//      throw e;
//    } catch (Exception e) {
//      notify(EventType.ARTIFACT_DESCRIPTOR_INVALID, session, request.getArtifact());
//      if (session.isIgnoreInvalidArtifactDescriptor()) {
//        return null;
//      }
//      result.addException(e);
//      throw new ArtifactDescriptorException(result);
//    }
//  }
//
//  private final ArtifactDescriptorResult checkVersion(RepositorySystemSession session, ArtifactDescriptorResult result) throws Exception {
//    ArtifactDescriptorRequest request = result.getRequest();
//    Artifact artifact = request.getArtifact();
//    VersionRequest versionRequest = new VersionRequest(artifact, request.getRepositories(), request.getRequestContext());
//    _versionResolver.resolveVersion(session, versionRequest);
//    return result;
//  }
//
//  private final ArtifactDescriptorResult loadInfoFromScriptSource(RepositorySystemSession session, ArtifactDescriptorResult result) throws Exception {
//    ArtifactDescriptorRequest request = result.getRequest();
//    Artifact artifact = request.getArtifact();
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
//    return readFromScriptInfo(_scriptService.findScriptInfo(sourceArtifact.getFile().toURI()), result);
//  }
//
//  private final ArtifactDescriptorResult readFromScriptInfo(ScriptInfo info, ArtifactDescriptorResult result) throws Exception {
//    result.setArtifact(info.artifact);
//    HashMap<String, Object> m = new HashMap<>(result.getProperties());
//    m.putAll(info.artifact.getProperties());
//    result.setProperties(m);
//    result.setDependencies(info.dependencies);
//    result.setManagedDependencies(info.managedDependencies);
//    result.setRepositories(info.repositories);
//    return result;
//  }
//
//  private void notify(EventType t, RepositorySystemSession session, Artifact artifact) {
//    RepositoryListener listener = session.getRepositoryListener();
//    if (listener != null) {
//      DefaultRepositoryEvent event = new DefaultRepositoryEvent(t, session, null);
//      event.setArtifact(artifact);
//      listener.artifactDescriptorMissing(event);
//    }
//  }
}

