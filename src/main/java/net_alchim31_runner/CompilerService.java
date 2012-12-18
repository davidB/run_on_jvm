package net_alchim31_runner;

import java.io.File;
import java.util.List;
import java.util.jar.Manifest;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;

//TODO may be use plexus-compiler-javac / plexus-compiler-manager / plexus-compiler-api
abstract public class CompilerService {
  abstract boolean compileToDir(File dest, FileObject src, List<File> classpath, List<String> options, DiagnosticListener<javax.tools.FileObject> diagnostics) throws Exception;
  abstract boolean accept(FileObject f) throws Exception;
  
  boolean compileToJar(File dest, FileObject src, List<File> classpath, List<String> options, DiagnosticListener<javax.tools.FileObject> diagnostics) throws Exception {
    File dir = new File(dest.getAbsolutePath() + ".d");
    if (dir.isDirectory()) {
      FileUtils.cleanDirectory(dir);
    } else {
      dir.mkdirs();
    }
    boolean b = compileToDir(dir, src, classpath, options, diagnostics);
    if (b) {
      FileUtils.jar(dest, dir, new Manifest());
    }
    FileUtils.deleteDirectory(dir);
    return b;
  }
}
