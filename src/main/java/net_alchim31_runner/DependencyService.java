package net_alchim31_runner;

import java.io.File;
import java.util.List;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencyManager;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.collection.DependencyTraverser;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.spi.locator.Service;
import org.sonatype.aether.spi.locator.ServiceLocator;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifactType;
import org.sonatype.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.util.graph.manager.ClassicDependencyManager;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;
import org.sonatype.aether.util.graph.selector.ExclusionDependencySelector;
import org.sonatype.aether.util.graph.selector.OptionalDependencySelector;
import org.sonatype.aether.util.graph.selector.ScopeDependencySelector;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sonatype.aether.util.graph.transformer.ConflictMarker;
import org.sonatype.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.sonatype.aether.util.graph.transformer.JavaEffectiveScopeCalculator;
import org.sonatype.aether.util.graph.transformer.NearestVersionConflictResolver;
import org.sonatype.aether.util.graph.traverser.FatArtifactTraverser;
import org.sonatype.aether.util.repository.DefaultAuthenticationSelector;
import org.sonatype.aether.util.repository.DefaultMirrorSelector;
import org.sonatype.aether.util.repository.DefaultProxySelector;

//http://git.eclipse.org/c/aether/aether-demo.git/tree/aether-demo-snippets/src/main/java/org/eclipse/aether/examples/util
public class DependencyService implements Service {

  private RepositorySystem _system;
  private WorkspaceReader4Script _workspaceReader4Script;

  public DependencyService() {
  }
  
  @Override
  public void initService(ServiceLocator locator) {
    _system = locator.getService(RepositorySystem.class);
    _workspaceReader4Script = locator.getService(WorkspaceReader4Script.class);
  }

  // see http://wiki.eclipse.org/Aether/Creating_a_Repository_System_Session
  // see
  // http://git.eclipse.org/c/aether/aether-ant.git/tree/src/main/java/org/eclipse/aether/ant/AntRepoSys.java#n305
  RepositorySystemSession newSession() {
    DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    session.setMirrorSelector(new DefaultMirrorSelector());
    session.setAuthenticationSelector(new DefaultAuthenticationSelector());
    session.setProxySelector(new DefaultProxySelector());

    DependencyTraverser depTraverser = new FatArtifactTraverser();
    session.setDependencyTraverser(depTraverser);

    DependencyManager depManager = new ClassicDependencyManager();
    session.setDependencyManager(depManager);

    DependencySelector depFilter = new AndDependencySelector(
        new ScopeDependencySelector("test", "provided"),
        new OptionalDependencySelector(),
        new ExclusionDependencySelector()
        );
    session.setDependencySelector(depFilter);

    DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
        new ConflictMarker(),
        new JavaEffectiveScopeCalculator(),
        new NearestVersionConflictResolver(),
        new JavaDependencyContextRefiner()
        );
    session.setDependencyGraphTransformer(transformer);
    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    DefaultArtifactTypeRegistry stereotypes = new DefaultArtifactTypeRegistry();
    stereotypes.add(new DefaultArtifactType("pom"));
    stereotypes.add(new DefaultArtifactType("maven-plugin", "jar", "", "java"));
    stereotypes.add(new DefaultArtifactType("jar", "jar", "", "java"));
    stereotypes.add(new DefaultArtifactType("ejb", "jar", "", "java"));
    stereotypes.add(new DefaultArtifactType("ejb-client", "jar", "client", "java"));
    stereotypes.add(new DefaultArtifactType("test-jar", "jar", "tests", "java"));
    stereotypes.add(new DefaultArtifactType("javadoc", "jar", "javadoc", "java"));
    stereotypes.add(new DefaultArtifactType("java-source", "jar", "sources", "java", false, false));
    //stereotypes.add(new DefaultArtifactType("war", "war", "", "java", false, true));
    //stereotypes.add(new DefaultArtifactType("ear", "ear", "", "java", false, true));
    //stereotypes.add(new DefaultArtifactType("rar", "rar", "", "java", false, true));
    //stereotypes.add(new DefaultArtifactType("par", "par", "", "java", false, true));
    stereotypes.add(new DefaultArtifactType("java-script", "java.jar", "", "java", true, true));
    stereotypes.add(new DefaultArtifactType("scala-script", "scala.jar", "", "scala", true, true));
    session.setArtifactTypeRegistry(stereotypes);

    // session.setIgnoreInvalidArtifactDescriptor( true );
    // session.setIgnoreMissingArtifactDescriptor( true );

    // session.setSystemProps( System.getProperties() );
    // session.setConfigProps( System.getProperties() );

    // TODO set local repo dir to maven local repo dir
    LocalRepository localRepo = new LocalRepository("target/local-repo");
    session.setWorkspaceReader(_workspaceReader4Script);
    session.setLocalRepositoryManager(_system.newLocalRepositoryManager(localRepo));
    // session.setTransferListener( new ConsoleTransferListener() );
    // session.setRepositoryListener( new ConsoleRepositoryListener() );

    return session;
  }

  ResolveResult resolve(RepositorySystemSession session, List<Dependency> dependencies, List<Dependency> managedDependencies, List<RemoteRepository> remoteRepositories) throws Exception {
    CollectRequest collectRequest = new CollectRequest(dependencies, managedDependencies, remoteRepositories);
    DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
    DependencyNode rootNode = _system.resolveDependencies(session, dependencyRequest).getRoot();
    PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
    rootNode.accept(nlg);

    return new ResolveResult(rootNode, nlg.getFiles(), nlg.getClassPath());
  }
  
  static class ResolveResult {
    public final DependencyNode root;
    public final List<File> resolvedFiles;
    public final String resolvedClassPath;
    public ResolveResult(DependencyNode root0, List<File> resolvedFiles0, String resolvedClassPath0) {
      super();
      this.root = root0;
      this.resolvedFiles = resolvedFiles0;
      this.resolvedClassPath = resolvedClassPath0;
    }
    
  }

  
  // public static File findUserSettings() {
  // File userHome = new File( System.getProperty( "user.home" ) );
  // return new File( new File( userHome, ".m2" ), "settings.xml" );
  // }
}
