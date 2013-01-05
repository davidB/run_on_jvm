package org.sonatype.aether.util.layout;

import java.net.URI;
import java.net.URISyntaxException;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.metadata.Metadata;

// TODO doc
// TODO optimize
// TODO extends (may be use a richer syntax : conditional,...)
public class PatternLayout implements RepositoryLayout {
  private final String _artifactPattern;
  private final String _metadataPattern;

  /**
   * same pattern for artifact and metadata
   * @param pattern
   */
  public PatternLayout(String pattern) {
    this(pattern, pattern);
  }

  public PatternLayout(String artifactPattern, String metadataPattern) {
    _artifactPattern = artifactPattern.replaceAll("\\$\\{artifactId\\}", "%1\\$s")
        .replaceAll("\\$\\{groupId\\}", "%2\\$s")
        .replaceAll("\\$\\{version\\}", "%3\\$s")
        .replaceAll("\\$\\{classifier\\}", "%4\\$s")
        .replaceAll("\\$\\{extension\\}", "%5\\$s")
        ;
    _metadataPattern = metadataPattern.replaceAll("\\$\\{artifactId\\}", "%1\\$s")
        .replaceAll("\\$\\{groupId\\}", "%2\\$s")
        .replaceAll("\\$\\{version\\}", "%3\\$s")
        .replaceAll("\\$\\{type\\}", "%4\\$s")
        .replaceAll("\\$\\{nature\\}", "%5\\$s")
        ;
  }

  private URI toUri(String path) {
    try{
      return new URI(null, null, path, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public URI getPath(Artifact v) {
    String uri = String.format(_artifactPattern, v.getArtifactId(), v.getGroupId(), v.getVersion(), v.getClassifier(), v.getExtension());
    return toUri(uri);
  }

  @Override
  public URI getPath(Metadata v) {
    String uri = String.format(_metadataPattern, v.getArtifactId(), v.getGroupId(), v.getVersion(), v.getType(), v.getNature().name());
    return toUri(uri);
  }

}
