phidias
=======

Provide OSGi aware runtime compilation support to javax.tools

[License](/rotty3000/phidias/blob/master/src/main/resources/LICENSE.txt)

Here is a simple example executing compilation:

```java
Bundle bundle = .. // the bundle executing the compile task

assert (bundle != null);

// My tests used generated jsp source
String javaSourceName = "org/apache/jsp/view_jsp"; // without extension
String javaSource = .. // the source code we'll be compiling

try {
	// compiler options
	List<String> options = new ArrayList<String>();

	//http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javac.html#options

	options.add("-proc:none"); // don't process annotations (typical for jsps)
	options.add("-verbose"); // Phidias adds to the default verbose output

	JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

	// Diagnostics provide details about all errors/warnings observed during compilation
	DiagnosticCollector<JavaFileObject> diagnostics =
		new DiagnosticCollector<JavaFileObject>();

	// the standard file manager knows how to load libraries from disk and from the runtime
	StandardJavaFileManager standardFileManager =
		javaCompiler.getStandardFileManager(diagnostics, null, null);

	// Were using source in string format (possibly generated dynamically)
	JavaFileObject[] sourceFiles = {
		new StringJavaFileObject(javaSourceName, javaSource)};

	optional: {
		// This is only needed if you want to compile against
		// libs/classes outside of (a.k.a. not installed in) the framework
		// including resources provided (as extensions) by the system.bundle

		List<File> classPath = .. // get some paths

		standardFileManager.setLocation(
			StandardLocation.CLASS_PATH, classPath);
	}

	// the OSGi aware file manager
	BundleJavaManager bundleFileManager = new BundleJavaManager(
		bundle, standardFileManager, options);

	optional: {
		// *** New since 0.1.7 ***
		// A new, optional, ResourceResolver API

		// This is optional as a default implementation is automatically
		// provided with using the exact logic below.

		ResourceResolver resourceResolver = new ResourceResolver() {
			public URL getResource(BundleWiring bundleWiring, String name) {
				return bundleWiring.getBundle().getResource(name);
			}

			public Collection<String> resolveResources(
				BundleWiring bundleWiring, String path, String filePattern,
				int options) {

				// Use whatever magic you like here to provide additional
				// resolution, such as overcoming the fact that the
				// system.bundle won't return resources from the parent
				// classloader, even when those are exported by framework
				// extension bundles

				return bundleWiring.listResources(path, filePattern, options);
			}
		};

		bundleFileManager.setResourceResolver(resourceResolver);
	}

	// get the compilation task
	CompilationTask compilationTask = javaCompiler.getTask(
		null, bundleFileManager, diagnostics, options, null,
		Arrays.asList(sourceFiles));

	bundleFileManager.close();

	// perform the actual compilation
	if (compilationTask.call()) {
		// Success!

		return;
	}

	// Oh no, we got errors, list them
	for (Diagnostic<?> dm: diagnostics.getDiagnostics()) {
		System.err.println("COMPILE ERROR: " + dm);
	}
}
catch (IOException e) {
	e.printStackTrace();
}
```