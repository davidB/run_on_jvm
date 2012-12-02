package net_alchim31_runner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;

class ScriptInfo {
  public static final String mainClassName = "mainClassName";
  
  public final Artifact artifact;
  public final Map<String, Object> properties = new HashMap<String, Object>();
  public final List<Dependency> dependencies = new LinkedList<Dependency>();
  public final List<Dependency> managedDependencies = new LinkedList<Dependency>();
  public final List<RemoteRepository> repositories = new LinkedList<RemoteRepository>();
  
  public ScriptInfo(Artifact artifact0) {
    super();
    this.artifact = artifact0;
  }
}

