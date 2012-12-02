package net_alchim31_runner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.codehaus.plexus.util.StringUtils;

public class CompilerService {
  public CompilerService() {
  }
  
  boolean accept(File f) throws Exception {
    return f.getName().endsWith(".java");
  }
  
  void compileToJar(File dest, File src, List<File> classpath, List<String> options) throws Exception {
    File dir = new File(dest.getAbsolutePath() + ".d");
    if (dir.isDirectory()) {
      FileUtils.cleanDirectory(dir);
    } else {
      dir.mkdirs();
    }
    compileToDir(dir, src, classpath, options);
    FileUtils.jar(dest, dir, new Manifest());
    FileUtils.deleteDirectory(dir);
  }
  
  //TODO add reporter of error, ...
  void compileToDir(File dest, File src, List<File> classpath, List<String> options) throws Exception {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    try {
      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(src));
      List<String> optionsL = new ArrayList<>(options.size() + 4);
      optionsL.addAll(options);
      optionsL.add("-d");
      optionsL.add(dest.getAbsolutePath());
      if (classpath.size() > 0) {
        optionsL.add("-classpath");
        optionsL.add(StringUtils.join(classpath.iterator(), File.pathSeparator));
      }
      JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionsL, null, compilationUnits);
      task.call();
    } finally {
      fileManager.close();
    }
  }
}
