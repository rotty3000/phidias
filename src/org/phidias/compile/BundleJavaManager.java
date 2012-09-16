package org.phidias.compile;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;


public class BundleJavaManager implements Constants, StandardJavaFileManager {


	public BundleJavaManager(
			Bundle bundle, StandardJavaFileManager standardJavaFileManager)
		throws IOException {

		this(bundle, standardJavaFileManager, null);
	}

	public BundleJavaManager(
			Bundle bundle, StandardJavaFileManager standardJavaFileManager,
			List<String> options)
		throws IOException {

		_bundle = bundle;

		setOptions(options);

		if (_verbose) {
			System.err.println(
				"[PHIDIAS] Initializing compilation in OSGi for bundle " +
					_bundle.getSymbolicName() + "-" + _bundle.getVersion());
		}

		_standardJavaFileManager = standardJavaFileManager;

		BundleWiring bundleWiring = _bundle.adapt(BundleWiring.class);

		_classLoader = bundleWiring.getClassLoader();

		_bundleWirings = new ArrayList<BundleWiring>();

		_bundleWirings.add(bundleWiring);

		_packageRequirements = bundleWiring.getRequirements(
			BundleRevision.PACKAGE_NAMESPACE);

		List<BundleWire> providedWires = bundleWiring.getRequiredWires(null);

		if (_verbose) {
			System.err.println(
				"[PHIDIAS] Dependent BundleWirings included in this " +
					"BundleJavaManager context: ");
		}

		for (BundleWire bundleWire : providedWires) {
			BundleWiring providerWiring = bundleWire.getProviderWiring();

			if (_bundleWirings.contains(providerWiring)) {
				continue;
			}

			if (_verbose) {
				Bundle curBundle = providerWiring.getBundle();

				System.err.println(
					"\t" + curBundle.getSymbolicName() + "-" +
						curBundle.getVersion());
			}

			_bundleWirings.add(providerWiring);
		}
	}

	public void close() throws IOException {
		_standardJavaFileManager.close();
	}

	public void flush() throws IOException {
		_standardJavaFileManager.flush();
	}

	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	public ClassLoader getClassLoader(Location location) {
		if (location != StandardLocation.CLASS_PATH) {
			return _standardJavaFileManager.getClassLoader(location);
		}

		return getClassLoader();
	}

	public FileObject getFileForInput(
			Location location, String packageName, String relativeName)
		throws IOException {

		return _standardJavaFileManager.getFileForInput(
			location, packageName, relativeName);
	}

	public FileObject getFileForOutput(
			Location location, String packageName, String relativeName,
			FileObject sibling)
		throws IOException {

		return _standardJavaFileManager.getFileForOutput(
			location, packageName, relativeName, sibling);
	}

	public JavaFileObject getJavaFileForInput(
		Location location, String className, Kind kind) throws IOException {

		return _standardJavaFileManager.getJavaFileForInput(
			location, className, kind);
	}

	public JavaFileObject getJavaFileForOutput(
			Location location, String className, Kind kind,
			FileObject sibling)
		throws IOException {

		return _standardJavaFileManager.getJavaFileForOutput(
			location, className, kind, sibling);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(
		File... files) {

		return _standardJavaFileManager.getJavaFileObjects(files);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(
		String... names) {

		return _standardJavaFileManager.getJavaFileObjects(names);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
		Iterable<? extends File> files) {

		return _standardJavaFileManager.getJavaFileObjectsFromFiles(files);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
		Iterable<String> names) {

		return _standardJavaFileManager.getJavaFileObjectsFromStrings(
			names);
	}

	public Iterable<? extends File> getLocation(Location location) {
		return _standardJavaFileManager.getLocation(location);
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return _standardJavaFileManager.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return _standardJavaFileManager.hasLocation(location);
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		if ((location == StandardLocation.CLASS_PATH) &&
			(file instanceof BundleJavaFileObject)) {

			BundleJavaFileObject bundleJavaFileObject =
				(BundleJavaFileObject)file;

			if (_verbose) {
				System.err.println(
					"[PHIDIAS] Infering binary name from " +
						bundleJavaFileObject);
			}

			return bundleJavaFileObject.inferBinaryName();
		}

		return _standardJavaFileManager.inferBinaryName(location, file);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		return _standardJavaFileManager.isSameFile(a, b);
	}

	public int isSupportedOption(String option) {
		return _standardJavaFileManager.isSupportedOption(option);
	}

	public Iterable<JavaFileObject> list(
			Location location, String packageName, Set<Kind> kinds,
			boolean recurse)
		throws IOException {

		List<JavaFileObject> javaFileObjects =
			new ArrayList<JavaFileObject>();

		if (_verbose) {
			System.err.println(
				"[PHIDIAS] List available sources for {location=" +
					location + ", packageName=" + packageName + ", kinds=" +
						kinds + ", recurse=" + recurse + "}");
		}

		if ((location == StandardLocation.CLASS_PATH) &&
			(!packageName.startsWith(JAVA_PACKAGE) ||
			 isBundleRequirement(packageName))) {

			int options = recurse ? BundleWiring.LISTRESOURCES_RECURSE : 0;

			packageName = packageName.replace('.', '/');

			for (Kind kind : kinds) {
				for (BundleWiring bundleWiring : _bundleWirings) {
					list(
						packageName, kind, options, bundleWiring,
						javaFileObjects);
				}
			}
		}

		if (javaFileObjects.isEmpty()) {
			for (JavaFileObject javaFileObject : _standardJavaFileManager.list(
					location, packageName, kinds, recurse)) {

				if (_verbose) {
					System.err.println("\t" + javaFileObject);
				}

				javaFileObjects.add(javaFileObject);
			}
		}

		return javaFileObjects;
	}

	public void setLocation(Location location, Iterable<? extends File> path)
		throws IOException {

		_standardJavaFileManager.setLocation(location, path);
	}

	private String getClassNameFromPath(URL resource, String packageName) {
		String className = resource.getPath();

		int x = className.indexOf(packageName);
		int y = className.indexOf('.');

		className = className.substring(x, y).replace('/', '.');

		if (className.startsWith(PERIOD)) {
			className = className.substring(1);
		}

		return className;
	}

	private boolean isBundleRequirement(String packageName) {
		Map<String,String> attributes = new HashMap<String,String>();

		attributes.put(BundleRevision.PACKAGE_NAMESPACE, packageName);

		for (BundleRequirement packageRequirement : _packageRequirements) {
			String filterSpec = packageRequirement.getDirectives().get(FILTER);

			try {
				if ((filterSpec != null) &&
					FrameworkUtil.createFilter(filterSpec).matches(attributes)) {

					return true;
				}
			}
			catch (InvalidSyntaxException e) {
				// Won't happen
			}
		}

		return false;
	}

	private void list(
		String packageName, Kind kind, int options,
		BundleWiring bundleWiring, List<JavaFileObject> javaFileObjects) {

		Collection<String> resources = bundleWiring.listResources(
			packageName, STAR.concat(kind.extension),
			options);

		if ((resources == null) || resources.isEmpty()) {
			return;
		}

		Bundle provider = bundleWiring.getBundle();

		for (String resourceName : resources) {
			URL resource = provider.getResource(resourceName);

			String className = getClassNameFromPath(resource, packageName);

			try {
				JavaFileObject javaFileObject = new BundleJavaFileObject(
					resource.toURI(), className);

				if (_verbose) {
					System.err.println("\t" + javaFileObject);
				}

				javaFileObjects.add(javaFileObject);
			}
			catch (URISyntaxException e) {
				// Can't really happen
			}
		}
	}

	private void setOptions(List<String> options) {
		if (options == null) {
			return;
		}

		_options.addAll(options);

		if (_options.contains(OPT_VERBOSE)) {
			_verbose = true;
		}
	}

	private Bundle _bundle;
	private ArrayList<BundleWiring> _bundleWirings;
	private ClassLoader _classLoader;
	private List<String> _options = new ArrayList<String>();
	private List<BundleRequirement> _packageRequirements;
	private StandardJavaFileManager _standardJavaFileManager;
	private boolean _verbose = false;

}