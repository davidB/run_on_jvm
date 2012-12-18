package net_alchim31_runner;

import java.util.Locale;

import javax.tools.Diagnostic;

public class SimpleDiagnostic<S> implements Diagnostic<S> {
  private final javax.tools.Diagnostic.Kind _kind;
  private final S _source;
  private final long _position;
  private final long _startPosition;
  private final long _endPosition;
  private final long _lineNumber;
  private final long _columnNumber;
  private final String _code;
  private final String _message;
  
  
  public SimpleDiagnostic(javax.tools.Diagnostic.Kind kind, S source, long position, long startPosition, long endPosition, long lineNumber, long columnNumber, String code, String message) {
    _kind = kind;
    _source = source;
    _position = position;
    _startPosition = startPosition;
    _endPosition = endPosition;
    _lineNumber = lineNumber;
    _columnNumber = columnNumber;
    _code = code;
    _message = message;
  }

  public SimpleDiagnostic(javax.tools.Diagnostic.Kind kind, S source, long lineNumber, long columnNumber, String code, String message) {
    this(kind, source, NOPOS, NOPOS, NOPOS, lineNumber, columnNumber, code, message);
  }

  public SimpleDiagnostic(javax.tools.Diagnostic.Kind kind, S source, long position, long startPosition, long endPosition, String code, String message) {
    this(kind, source, position, startPosition, endPosition, NOPOS, NOPOS, code, message);
  }

  public SimpleDiagnostic(javax.tools.Diagnostic.Kind kind, S source, String message) {
    this(kind, source, NOPOS, NOPOS, NOPOS, NOPOS, NOPOS, "", message);
  }

  @Override
  public javax.tools.Diagnostic.Kind getKind() {
    return _kind;
  }

  @Override
  public S getSource() {
    return _source;
  }

  @Override
  public long getPosition() {
    return _position;
  }

  @Override
  public long getStartPosition() {
    return _startPosition;
  }

  @Override
  public long getEndPosition() {
    return _endPosition;
  }

  @Override
  public long getLineNumber() {
    return _lineNumber;
  }

  @Override
  public long getColumnNumber() {
    return _columnNumber;
  }

  @Override
  public String getCode() {
    return _code;
  }

  @Override
  public String getMessage(Locale locale) {
    return _message;
  }

}
