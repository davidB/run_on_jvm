package net_alchim31_runner;

import java.io.File;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;

//#set scalaVersion 2.9.2
//#repo central m2:http://repo1.maven.org/maven2/
//#from org.scala-lang:scala-compiler:${scalaVersion}
import scala.collection.immutable.Nil;
import scala.tools.nsc.Global;
import scala.tools.nsc.Settings;
import scala.tools.nsc.io.VirtualFile;
import scala.tools.nsc.reporters.AbstractReporter;
import scala.tools.nsc.reporters.Reporter;
import scala.tools.nsc.util.BatchSourceFile;
import scala.tools.nsc.util.Position;
import scala.tools.nsc.util.SourceFile;

//#set rojVersion 0.3.0-SNAPSHOT
//#repo lib m2:file://${app.home}/lib
//#from net.alchim31.runner:run_on_jvm:${rojVersion}
/**
 * Not ThreadSafe
 * 
 * @author dwayne
 */
// TODO remove useless args from the "help"
public class CompilerService4Scala_2_9 implements CompilerService {

  // TODO support options (parse into settings)
  @Override
  public boolean compileToDir(File dest, final FileObject src, List<File> classpath, List<String> options, final DiagnosticListener<FileObject> diagnostics) throws Exception {
    final Settings settings = new Settings();
    settings.usejavacp().tryToSetFromPropertyValue("true");
    // settings.stopAfter().tryToSetColon(Nil.$colon$colon("jvm"));
    settings.debug().tryToSetFromPropertyValue("false");
    settings.outputDirs().setSingleOutput(dest.getAbsolutePath());
    settings.d().tryToSetFromPropertyValue(dest.getAbsolutePath());
    System.err.println("out : " + dest.getAbsolutePath());
    // TODO fix creation of the classpath
    scala.collection.immutable.List<String> cp = null;
    for (File f : classpath) {
      String path = f.getAbsolutePath();
      cp = (cp == null) ? Nil.$colon$colon(path) : cp.$colon$colon(path);
    }
    if (cp != null) {
      settings.classpath().tryToSet(cp);
    }
    // val ss = new Settings(scalacError)
    // reporter = new ConsoleReporter(ss)
    // val command = new CompilerCommand(args.toList, ss)
    // val settings = command.settings
    Reporter reporter = new AbstractReporter() {

      @Override
      public void display(Position pos, String msg, Severity severity) {
        severity.count_$eq(severity.count() + 1);
        // severity
        Kind k = Kind.NOTE;
        if (ERROR().equals(severity)) k = Kind.ERROR;
        else if (WARNING().equals(severity)) k = Kind.WARNING;
        if (pos.isDefined()) {
          diagnostics.report(new SimpleDiagnostic<FileObject>(k, src, pos.line(), pos.column(), pos.lineContent(), msg));
        } else {
          diagnostics.report(new SimpleDiagnostic<FileObject>(k, src, Diagnostic.NOPOS, Diagnostic.NOPOS, null, msg));
        }

        //super.display(pos, msg, severity);
      }

      @Override
      public void displayPrompt() {
        System.err.println("displayPrompt");
      }

      @Override
      public Settings settings() {
        return settings;
      }

    };
    // reporter = new ConsoleReporter(settings);
    Global compiler = new Global(settings, reporter);
    if (reporter.hasErrors()) {
      reporter.flush();
      return false;
    }
    Global.Run run = compiler.new Run();
    //run.compile(Nil.$colon$colon(src.getName()));
    SourceFile sf = new BatchSourceFile(new VirtualFile(src.toUri().toString()), src.getCharContent(true).toString().toCharArray());
    run.compileSources(Nil.$colon$colon(sf));
    // scala.tools.nsc.Main.process(new String[]{"-cp", StringUtils.join(FileUtils.toStrings(classpath).iterator(),
    // File.pathSeparator), "-d", dest.getAbsolutePath(), src.getName()});
    // boolean success = !scala.tools.nsc.Main.reporter().hasErrors();
    boolean success = !reporter.hasErrors();
    return success;
  }
}
// CODE for calling interpreter (for memory)
// final Settings settings = new Settings();
// settings.usejavacp().tryToSetFromPropertyValue("true");
// //settings.stopAfter().tryToSetColon(Nil.$colon$colon("jvm"));
// settings.debug().tryToSetFromPropertyValue("false");
// settings.outputDirs().setSingleOutput(dest.getAbsolutePath());
// settings.d().tryToSetFromPropertyValue(dest.getAbsolutePath());
// System.err.println("out : " + dest.getAbsolutePath());
// //TODO fix creation of the classpath
// scala.collection.immutable.List<String> cp = null;
// for(File f : classpath) {
// String path = f.getAbsolutePath();
// cp = (cp == null) ? Nil.$colon$colon(path) : cp.$colon$colon(path);
// }
// if (cp != null) {
// //settings.classpath().tryToSet(cp);
// }
//
// //System.out.println("About to compile " + new String(sourceUnit.getContents()));
// IMain main = new IMain(settings, new PrintWriter(System.err));
// SourceFile sf = new BatchSourceFile(new VirtualFile(src.getName()),
// src.getCharContent(true).toString().toCharArray());
// main.compileSources(Nil.$colon$colon(sf));
// //main.compileString(src.getCharContent(true).toString());
