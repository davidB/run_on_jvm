package net_alchim31_runner;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.codehaus.plexus.util.IOUtil;

public class FileUtils extends org.codehaus.plexus.util.FileUtils {

  public static File jar(File dest, File srcDir, Manifest manifest) throws Exception {
    try (
      FileOutputStream stream = new FileOutputStream(dest);
      JarOutputStream out = new JarOutputStream(stream, manifest);
    ) {
      addToJar(out, srcDir, srcDir.getAbsolutePath().length(), null);
    }
    return dest;
  }

  private static void addToJar(JarOutputStream out, File src, int prefixLg, FileFilter filter) throws Exception {
    if (src.isDirectory()) {
      for (File f : src.listFiles(filter)) {
        addToJar(out, f, prefixLg, filter);
      }
    } else if (src.isFile()) {
      // Add archive entry
      JarEntry jarAdd = new JarEntry(src.getAbsolutePath().substring(prefixLg + 1));
      jarAdd.setTime(src.lastModified());
      out.putNextEntry(jarAdd);

      try (FileInputStream in = new FileInputStream(src)) {
        IOUtil.copy(in, out);
      }
    }
  }

}
