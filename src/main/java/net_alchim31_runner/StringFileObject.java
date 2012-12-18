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

import javax.tools.SimpleJavaFileObject;


@SuppressWarnings("deprecation")
class StringFileObject extends SimpleJavaFileObject {
  private String _content;

  public StringFileObject(URI uri0, String content) {
    super(uri0, Kind.SOURCE);
    _content = content;
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
    return new StringReader(_content);
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
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