package net_alchim31_runner;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;

public class WorkspaceReader4Script implements WorkspaceReader, Service {
  private final WorkspaceRepository _wr = new WorkspaceRepository();
  private ServiceLocator _locator;
  
  @Override
  public void initService(ServiceLocator locator) {
    _locator = locator;
  }
  @Override
  public WorkspaceRepository getRepository() {
    return _wr;
  }

  @Override
  public File findArtifact(Artifact artifact) {
    try {
      String uri = artifact.getProperty(ScriptInfo.KEY_URI, null);
      if (uri != null && artifact.getGroupId().startsWith(ScriptInfo.GROUPID_PREFIX)) {
        return findJar(new URI(uri));
      }
    } catch(Exception exc) {
      Main.logger.warn("Failed to find " + artifact,exc);
    }
    return null;
  }

  @Override
  public List<String> findVersions(Artifact artifact) {
    Map<String, String> p = artifact.getProperties();
    if (p.containsKey("uri")) {
      return Arrays.asList(artifact.getVersion());
    }
    return Collections.emptyList();
  }

  public File findJar(URI v) throws Exception {
    ScriptService ss = _locator.getService(ScriptService.class);
    return ss.newClasspath(ss.findScriptInfo(v, null)).get(0);
  }
}
