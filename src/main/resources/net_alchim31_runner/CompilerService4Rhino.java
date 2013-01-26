package net_alchim31_runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;

//#repo central m2:http://repo1.maven.org/maven2/ 
//#from org.mozilla:rhino:1.7R4
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.optimizer.ClassCompiler;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.jsc.Main;

//#set rojVersion 0.3.0-SNAPSHOT
//#repo lib m2:file://${app.home}/lib
//#from net.alchim31.runner:run_on_jvm:${rojVersion}

/**
 * Not ThreadSafe
 * 
 * @author dwayne
 * @based org.mozilla.javascript.tools.jsc.Main
 */
// TODO remove useless args from the "help"
public class CompilerService4Rhino implements CompilerService {

  @Override
  public boolean compileToDir(File dest, final FileObject src, List<File> classpath, List<String> options, final DiagnosticListener<FileObject> diagnostics) throws Exception {
    reporter = new ErrorReporter() {

      @Override
      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        diagnostics.report(new SimpleDiagnostic<FileObject>(Kind.WARNING, src, line, lineOffset, lineSource, message));
      }

      @Override
      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        diagnostics.report(new SimpleDiagnostic<FileObject>(Kind.ERROR, src, line, lineOffset, lineSource, message));
      }

      @Override
      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
      }

    };
    compilerEnv = new CompilerEnvirons();
    compilerEnv.setErrorReporter(reporter);
    compiler = new ClassCompiler(compilerEnv);
    options.add("-nosource");
    options.add("-opt");
    options.add("9");
    options.add("-version");
    options.add("170");
    if (!processOptions(options)) {
      if (printHelp) {
        diagnostics.report(new SimpleDiagnostic<>(Kind.NOTE, src, ToolErrorReporter.getMessage("msg.jsc.usage", "org.mozilla.javascript.tools.jsc.Main")));
      }
      return false;
    }
    processSource(dest, src);
    return true; // TODO report.hasError
  }

  /**
   * Parse arguments.
   * 
   */
  public boolean processOptions(List<String> args) {
    compilerEnv.setGenerateDebugInfo(false); // default to no symbols
    for (int i = 0; i < args.size(); i++) {
      String arg = args.get(i);
      if (!arg.startsWith("-") || arg.equals("-help") || arg.equals("-h") || arg.equals("--help")) {
        printHelp = true;
        return false;
      }

      try {
        if (arg.equals("-version") && ++i < args.size()) {
          int version = Integer.parseInt(args.get(i));
          compilerEnv.setLanguageVersion(version);
          continue;
        }
        if ((arg.equals("-opt") || arg.equals("-O")) && ++i < args.size()) {
          int optLevel = Integer.parseInt(args.get(i));
          compilerEnv.setOptimizationLevel(optLevel);
          continue;
        }
      } catch (NumberFormatException e) {
        badUsage(args.get(i));
        return false;
      }
      if (arg.equals("-nosource")) {
        compilerEnv.setGeneratingSource(false);
        continue;
      }
      if (arg.equals("-debug") || arg.equals("-g")) {
        compilerEnv.setGenerateDebugInfo(true);
        continue;
      }
      if (arg.equals("-main-method-class") && ++i < args.size()) {
        compiler.setMainMethodClass(args.get(i));
        continue;
      }
      if (arg.equals("-encoding") && ++i < args.size()) {
        // characterEncoding = args.get(i);
        continue;
      }
      if (arg.equals("-o") && ++i < args.size()) {
        String name = args.get(i);
        int end = name.length();
        if (end == 0 || !Character.isJavaIdentifierStart(name.charAt(0)))
        {
          addError("msg.invalid.classfile.name", name);
          continue;
        }
        for (int j = 1; j < end; j++) {
          char c = name.charAt(j);
          if (!Character.isJavaIdentifierPart(c)) {
            if (c == '.') {
              // check if it is the dot in .class
              if (j == end - 6 && name.endsWith(".class")) {
                name = name.substring(0, j);
                break;
              }
            }
            addError("msg.invalid.classfile.name", name);
            break;
          }
        }
        // targetName = name;
        continue;
      }
      if (arg.equals("-observe-instruction-count")) {
        compilerEnv.setGenerateObserverCount(true);
      }
      if (arg.equals("-package") && ++i < args.size()) {
        String pkg = args.get(i);
        int end = pkg.length();
        for (int j = 0; j != end; ++j) {
          char c = pkg.charAt(j);
          if (Character.isJavaIdentifierStart(c)) {
            for (++j; j != end; ++j) {
              c = pkg.charAt(j);
              if (!Character.isJavaIdentifierPart(c)) {
                break;
              }
            }
            if (j == end) {
              break;
            }
            if (c == '.' && j != end - 1) {
              continue;
            }
          }
          // addError("msg.package.name", targetPackage);
          return false;
        }
        // targetPackage = pkg;
        continue;
      }
      if (arg.equals("-extends") && ++i < args.size()) {
        String targetExtends = args.get(i);
        Class<?> superClass;
        try {
          superClass = Class.forName(targetExtends);
        } catch (ClassNotFoundException e) {
          throw new Error(e.toString()); // TODO: better error
        }
        compiler.setTargetExtends(superClass);
        continue;
      }
      if (arg.equals("-implements") && ++i < args.size()) {
        // TODO: allow for multiple comma-separated interfaces.
        String targetImplements = args.get(i);
        StringTokenizer st = new StringTokenizer(targetImplements,
            ",");
        List<Class<?>> list = new ArrayList<Class<?>>();
        while (st.hasMoreTokens()) {
          String className = st.nextToken();
          try {
            list.add(Class.forName(className));
          } catch (ClassNotFoundException e) {
            throw new Error(e.toString()); // TODO: better error
          }
        }
        Class<?>[] implementsClasses = list.toArray(new Class<?>[list.size()]);
        compiler.setTargetImplements(implementsClasses);
        continue;
      }
      if (arg.equals("-d") && ++i < args.size()) {
        // destinationDir = args.get(i);
        continue;
      }
      badUsage(arg);
      return false;
    }
    return true;
  }

  /**
   * Print a usage message.
   */
  private static void badUsage(String s) {
    net_alchim31_runner.Main.logger.error(ToolErrorReporter.getMessage("msg.jsc.bad.usage", Main.class.getName(), s));
  }

  /**
   * Compile JavaScript source.
   * 
   */
  public void processSource(File targetTopDir, FileObject src) throws Exception {

    String mainClassName = "HelloWorld3";

    Object[] compiled = compiler.compileToClassFiles(src.getCharContent(true).toString(), src.getName(), 1, mainClassName);
    if (compiled == null || compiled.length == 0) {
      return;
    }

    for (int j = 0; j != compiled.length; j += 2) {
      String className = (String) compiled[j];
      byte[] bytes = (byte[]) compiled[j + 1];
      File outfile = getOutputFile(targetTopDir, className);
      try {
        FileOutputStream os = new FileOutputStream(outfile);
        try {
          os.write(bytes);
        } finally {
          os.close();
        }
      } catch (IOException ioe) {
        addFormatedError(ioe.toString());
      }
    }
  }

  private File getOutputFile(File parentDir, String className) {
    String path = className.replace('.', File.separatorChar);
    path = path.concat(".class");
    File f = new File(parentDir, path);
    String dirPath = f.getParent();
    if (dirPath != null) {
      File dir = new File(dirPath);
      if (!dir.exists()) {
        dir.mkdirs();
      }
    }
    return f;
  }

  /**
   * Verify that class file names are legal Java identifiers. Substitute illegal characters with underscores, and
   * prepend the name with an underscore if the file name does not begin with a JavaLetter.
   */

  // private String getClassName(String name) {
  // char[] s = new char[name.length() + 1];
  // char c;
  // int j = 0;
  //
  // if (!Character.isJavaIdentifierStart(name.charAt(0))) {
  // s[j++] = '_';
  // }
  // for (int i = 0; i < name.length(); i++, j++) {
  // c = name.charAt(i);
  // if (Character.isJavaIdentifierPart(c)) {
  // s[j] = c;
  // } else {
  // s[j] = '_';
  // }
  // }
  // return (new String(s)).trim();
  // }

  private void addError(String messageId, String arg) {
    String msg;
    if (arg == null) {
      msg = ToolErrorReporter.getMessage(messageId);
    } else {
      msg = ToolErrorReporter.getMessage(messageId, arg);
    }
    addFormatedError(msg);
  }

  private void addFormatedError(String message) {
    reporter.error(message, null, -1, null, -1);
  }

  private boolean          printHelp;
  private ErrorReporter    reporter;
  private CompilerEnvirons compilerEnv;
  private ClassCompiler    compiler;
}
