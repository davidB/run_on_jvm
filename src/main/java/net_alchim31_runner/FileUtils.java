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

  @SuppressWarnings("resource")
  public static File jar(File dest, File srcDir, Manifest manifest) throws Exception {
    FileOutputStream stream = null;
    JarOutputStream out = null;
    try {
      // Open archive file
      stream = new FileOutputStream(dest);
      out = new JarOutputStream(stream, manifest);
      addToJar(out, srcDir, srcDir.getAbsolutePath().length(), null);
    } finally {
      IOUtil.close(out);
      IOUtil.close(stream);
    }
    return dest;
  }
  
  @SuppressWarnings("resource")
  private static void addToJar(JarOutputStream out, File src, int prefixLg, FileFilter filter) throws Exception {
    if (src.isDirectory()) {
      for(File f : src.listFiles(filter)) {
        addToJar(out, f, prefixLg, filter);
      }
    } else if (src.isFile()) {
      // Add archive entry
      JarEntry jarAdd = new JarEntry(src.getAbsolutePath().substring(prefixLg + 1));
      jarAdd.setTime(src.lastModified());
      out.putNextEntry(jarAdd);
      
      FileInputStream in = new FileInputStream(src);
      try {
        IOUtil.copy(in, out);
      } finally {
        IOUtil.close(in);
      }
    }
  }
  
}
