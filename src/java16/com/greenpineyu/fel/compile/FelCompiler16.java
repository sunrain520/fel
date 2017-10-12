package com.greenpineyu.fel.compile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.greenpineyu.fel.Expression;
import com.greenpineyu.fel.exception.CompileException;
import com.greenpineyu.fel.exception.FelException;

public class FelCompiler16<T> implements FelCompiler {
	private final FelCompilerClassloader classLoader;

	private final JavaCompiler compiler;

	private final List<String> options;

	private DiagnosticCollector<JavaFileObject> diagnostics;

	private final JavaFileManager javaFileManager;

	public FelCompiler16() {
		compiler = ToolProvider.getSystemJavaCompiler();

		if (compiler == null) {
			throw new IllegalStateException(
					"Cannot find the system Java compiler. "
							+ "Check that your class path includes tools.jar");
		}

		this.classLoader = new FelCompilerClassloader(this.getClass()
				.getClassLoader());
		diagnostics = new DiagnosticCollector<JavaFileObject>();
		final StandardJavaFileManager fileManager = compiler
				.getStandardFileManager(diagnostics, null, null);

		ClassLoader loader = this.classLoader.getParent();
		List<String> paths = CompileService.getClassPath(loader);
		List<File> cpFiles = new ArrayList<File>();
		if (paths != null && (!paths.isEmpty())) {
			for (String file : paths) {
				cpFiles.add(new File(file));
			}
		}
		try {
			fileManager.setLocation(StandardLocation.CLASS_PATH, cpFiles);
		} catch (IOException e) {
			throw new CompileException(FelException.getCauseMessage(e), e);
		}

		/*
		
		
		if (loader instanceof URLClassLoader
				&& (!loader.getClass().getName()
						.equals("sun.misc.Launcher$AppClassLoader"))) {
			System.out.println("..............................asdfasdf......................");
			try {
				URLClassLoader urlClassLoader = (URLClassLoader) loader;
				List<File> path = new ArrayList<File>();
				for (URL url : urlClassLoader.getURLs()) {
					File file = new File(url.getFile());
					path.add(file);
				}
				fileManager.setLocation(StandardLocation.CLASS_PATH, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Enumeration<URL> resources = null;
			try {
				resources = loader.getResources("/");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (resources != null) {
				List<File> path = new ArrayList<File>();
				while (resources.hasMoreElements()) {
					URL resource = resources.nextElement();
					File file = new File(resource.getFile());
					path.add(file);
				}
			}

		}*/

		javaFileManager = new ForwardingJavaFileManager<JavaFileManager>(
				fileManager) {
			 @Override
			 public JavaFileObject getJavaFileForOutput(Location location,
					String qualifiedName, Kind kind, FileObject outputFile)
			 throws IOException {
				// 由于编译成功后的bytecode需要放到file中，所以需要将file放到classloader中，以便读取bytecode生成Class对象.
				classLoader.add(qualifiedName, outputFile);
				return (JavaFileObject) outputFile;
			 }
		};
		this.options = new ArrayList<String>();
		// this.options.add("-O");
	}

	@Override
	public Expression compile(JavaSource src) {

		Class<T> compile = compileToClass(src);
		try {
			return (Expression) compile.newInstance();
		} catch (Exception e) {
			throw new CompileException(FelException.getCauseMessage(e), e);
		}
	}


	public synchronized Class<T> compileToClass(final JavaSource src) {
		List<JavaFileObject> compileSrcs = new ArrayList<JavaFileObject>();
		String className = src.getSimpleName();
		final FelJavaFileObject compileSrc = new FelJavaFileObject(className,
				src.getSource());
		compileSrcs.add(compileSrc);
		final CompilationTask task = compiler.getTask(null, javaFileManager,
				diagnostics, options, null, compileSrcs);
		final Boolean result = task.call();
		if (result == null || !result.booleanValue()) {
			// diagnostics.
			// 编译失败
			// diagnostics.getDiagnostics()
			throw new CompileException(src.getSource() + "\n"
					+ diagnostics.getDiagnostics().toString());
		}
		try {
			return loadClass(src.getName());
		} catch (ClassNotFoundException e) {
			throw new CompileException(FelException.getCauseMessage(e), e);
		}
	}

	@SuppressWarnings("unchecked")
	public Class<T> loadClass(final String qualifiedClassName)
			throws ClassNotFoundException {
		return (Class<T>) classLoader.loadClass(qualifiedClassName);
	}

	static URI toUri(String name) {
		try {
			return new URI(name);
		} catch (URISyntaxException e) {
			throw new CompileException(FelException.getCauseMessage(e), e);
		}
	}

}

