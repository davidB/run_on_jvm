package net_alchim31_runner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

//TODO may be use plexus-compiler-javac / plexus-compiler-manager / plexus-compiler-api
public class CompilerService4Java implements CompilerService{
  public CompilerService4Java() {
  }
  
  //TODO add reporter of error, ...
  //TODO provide src as String because it's already read and in memory (to extract classpath,...)
  //  see http://www.accordess.com/wpblog/an-overview-of-java-compilation-api-jsr-199/
  //TODO find how to run in isolated classloader
  @Override
  public boolean compileToDir(File dest, FileObject src, List<File> classpath, List<String> options, DiagnosticListener<javax.tools.FileObject> diagnostics) throws Exception {
//    ClassWorld world = new ClassWorld();
//    ClassRealm javacRealm = world.newRealm("javac");
//    //javacRealm.addConstituent( containerJarUrl );
//    System.err.println("javac0 : " + Arrays.toString(javacRealm.getURLs()));
    final ClassLoader cl0 = Thread.currentThread().getContextClassLoader();
    try {
//      Class<javax.tools.ToolProvider> containerClass = javacRealm.loadClass( "javax.tools.ToolProvider" );
//      Thread.currentThread().setContextClassLoader( javacRealm.getSystemClassLoader() );
//    javax.tools.JavaCompiler compiler = (javax.tools.JavaCompiler) containerClass.getMethod("getSystemJavaCompiler").invoke(null);
      Thread.currentThread().setContextClassLoader(javax.tools.ToolProvider.getSystemToolClassLoader());
      javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
      javax.tools.StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
      fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(dest));
      fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, Collections.<File>emptyList());
      fileManager.setLocation(StandardLocation.SOURCE_PATH, Collections.<File>emptyList());
//      fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.EMPTY_LIST);
      try {
        Iterable<? extends javax.tools.JavaFileObject> compilationUnits = Arrays.asList((JavaFileObject)src);//fileManager.getJavaFileObjectsFromFiles();
        List<String> optionsL = new ArrayList<>(options.size() + 4);
        optionsL.addAll(options);
//        optionsL.add("-d");
//        optionsL.add(dest.getAbsolutePath());
//        if (classpath.size() > 0) {
//          optionsL.add("-classpath");
//          optionsL.add(StringUtils.join(classpath.iterator(), File.pathSeparator));
//        }
        javax.tools.JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionsL, null, compilationUnits);
        task.setProcessors(Collections.<Processor>emptyList());
        return task.call();
      } finally {
        fileManager.close();
      }
    } finally {
      Thread.currentThread().setContextClassLoader(cl0);
    }
  }
}
