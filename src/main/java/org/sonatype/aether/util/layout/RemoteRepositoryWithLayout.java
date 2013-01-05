package org.sonatype.aether.util.layout;

import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.layout.RepositoryLayout;

public class RemoteRepositoryWithLayout extends RemoteRepository{
  private final RepositoryLayout _layout;
  
  /**
   * Creates a new repository with the specified properties and the default policies.
   * 
   * @param id The identifier of the repository, may be {@code null}.
   * @param type The type of the repository, may be {@code null}.
   * @param url The (base) URL of the repository, may be {@code null}.
   * @param url The layout to use to retrieve artifact and metadata on the repository.
   */
  public RemoteRepositoryWithLayout( String id, String type, String url, RepositoryLayout layout )
  {
      super(id, type, url);
      _layout = layout;
  }

  public RepositoryLayout getLayout() {
    return _layout;
  }
}
