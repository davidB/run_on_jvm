package net_alchim31_runner;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import org.codehaus.plexus.util.IOUtil;
import java.io.InputStream;

public class Main {
	
	public static void main(String[] args) {
		try {
			run(compile(new URI(args[0])));
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}

	public static void run(RunInfo v) throws Exception {
		URL[] urls = new URL[v.classpath.length];
		for(int i = v.classpath.length -1; i > -1; i--) {
			urls[i] = v.classpath[i].toURL();
		}
		URLClassLoader cl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
		try {
			Class<?> clazz = cl.loadClass(v.className);
			Method m = clazz.getMethod("main", String[].class);
			m.invoke(null, (Object)v.args);
		} catch(Exception exc) {
			//TODO log the RunInfo to help debug cause of the exception
			throw exc;
		} finally {
			cl.close();
		}
	}
	
	public static RunInfo compile(URI v) throws Exception {
		
		return new RunInfo(new URI[]{}, "", new String[]{}, new String[]{});
	}
	
	public static String toString(URI v) throws Exception {
		InputStream is = v.toURL().openStream();
		try {
			return IOUtil.toString(is);
		} finally {
			is.close();
		}
	}

	
}

class RunInfo {
	public final String className;
	public final String[] args;
	public final URI[] classpath;
	public final String[] jvmArgs;

	public RunInfo(URI[] classpath0, String className0, String[] args0, String[] jvmArgs0) {
		super();
		this.classpath = classpath0;
		this.className = className0;
		this.args = args0;
		this.jvmArgs = jvmArgs0;
	}
}

