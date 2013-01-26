package net_alchim31_runner;

import java.io.File;
import java.util.List;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;

//TODO may be use plexus-compiler-javac / plexus-compiler-manager / plexus-compiler-api
public interface CompilerService {
  abstract boolean compileToDir(File dest, FileObject src, List<File> classpath, List<String> options, DiagnosticListener<javax.tools.FileObject> diagnostics) throws Exception;
}
