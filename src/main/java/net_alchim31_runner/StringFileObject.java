package net_alchim31_runner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import javax.tools.SimpleJavaFileObject;

import org.codehaus.plexus.util.IOUtil;


@SuppressWarnings("deprecation")
class StringFileObject extends SimpleJavaFileObject {
  private String _content;

  public StringFileObject(URI uri0) {
    super(uri0, Kind.SOURCE);
  }

  @Override
  public String toString() {
    return uri.toString();
  }
  
  @Override
  public URI toUri() {
    return uri;
  }

  @Override
  public String getName() {
    return uri.getPath();
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return new StringBufferInputStream(_content);
  }

  @Override
  public OutputStream openOutputStream() throws IOException {
    throw new UnsupportedOperationException("readonly : " + uri);
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return new StringReader(getCharContent(ignoreEncodingErrors).toString());
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    if (_content == null) {
      URL url = ("classpath".equals(uri.getScheme())) ? Thread.currentThread().getContextClassLoader().getResource(uri.getPath().substring(1)) : uri.toURL();
      try(InputStream input = url.openStream()) {
        _content = IOUtil.toString(input, "UTF-8");
      }
    }
    return _content;
  }

  @Override
  public Writer openWriter() throws IOException {
    throw new UnsupportedOperationException("readonly : " + uri);
  }

  @Override
  public long getLastModified() {
    if ("file".equals(uri.getScheme())) {
      return new File(uri.getPath()).lastModified();
    }
    // TODO throw new UnsupportedOperationException("remote : " + key); ?
    return 0;
  }

  @Override
  public boolean delete() {
    return false;
  }
};