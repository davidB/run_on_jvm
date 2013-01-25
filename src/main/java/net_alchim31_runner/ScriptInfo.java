package net_alchim31_runner;

import java.util.LinkedList;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;

class ScriptInfo {
  public static final String KEY_URI = "uri";
  public static final String KEY_TYPE = "type";
  public static final String VALUE_TYPE = "script";  
  public static final String GROUPID_PREFIX = "script.";
  public static final String VERSION = "0.0.0-SNAPSHOT";
  //public static final String KEY_LOCALPATH = ArtifactProperties.LOCAL_PATH;
  
  public final Artifact artifact;
  public final List<Dependency> dependencies = new LinkedList<Dependency>();
  public final List<Dependency> managedDependencies = new LinkedList<Dependency>();
  public final List<RemoteRepository> repositories = new LinkedList<RemoteRepository>();
  public String mainClassName = "Main";

  public ScriptInfo(Artifact artifact0) {
    super();
    this.artifact = artifact0;
  }
}

