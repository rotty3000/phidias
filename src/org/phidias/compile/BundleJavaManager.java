/**
 * Copyright (c) 2000-2012 Raymond Augé All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package org.phidias.compile;

import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public class BundleJavaManager
	extends ForwardingJavaFileManager<JavaFileManager>
	implements Constants {

	public BundleJavaManager(
			Bundle bundle, JavaFileManager javaFileManager)
		throws IOException {

		this(bundle, javaFileManager, null);
	}

	public BundleJavaManager(
			Bundle bundle, JavaFileManager javaFileManager,
			List<String> options)
		throws IOException {

		super(javaFileManager);

		_bundle = bundle;

		setOptions(options);

		if (_verbose) {
			System.err.println(
				"[PHIDIAS] Initializing compilation in OSGi for bundle " +
					_bundle.getSymbolicName() + "-" + _bundle.getVersion());
		}

		_javaFileManager = javaFileManager;

		BundleWiring bundleWiring = (BundleWiring)_bundle.adapt(
			BundleWiring.class);

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

	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		if (location != StandardLocation.CLASS_PATH) {
			return _javaFileManager.getClassLoader(location);
		}

		return getClassLoader();
	}

	@Override
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

		return _javaFileManager.inferBinaryName(location, file);
	}

	@Override
	public Iterable<JavaFileObject> list(
			Location location, String packageName, Set<Kind> kinds,
			boolean recurse)
		throws IOException {

		List<JavaFileObject> javaFileObjects =
			new ArrayList<JavaFileObject>();

		if (_verbose && (location == StandardLocation.CLASS_PATH)) {
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

		// This ensures that if a standard classpath location has been provided
		// we include it. It allows the framework to compile against libraries
		// not deployed as OSGi bundles. This is also needed in cases where the
		// system.bundle exports extra packages via the property
		// 'org.osgi.framework.system.packages.extra' or via bundle fragments
		// which only supplement its 'Export-Package' directive.

		if (javaFileObjects.isEmpty()) {
			for (JavaFileObject javaFileObject : _javaFileManager.list(
					location, packageName, kinds, recurse)) {

				if (_verbose && (location == StandardLocation.CLASS_PATH)) {
					System.err.println("\t" + javaFileObject);
				}

				javaFileObjects.add(javaFileObject);
			}
		}

		return javaFileObjects;
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
			Map<String, String> directives = packageRequirement.getDirectives();

			String filterSpec = directives.get(FILTER);

			try {
				if ((filterSpec != null) &&
					FrameworkUtil.createFilter(filterSpec).matches(
						attributes)) {

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
	private JavaFileManager _javaFileManager;
	private List<String> _options = new ArrayList<String>();
	private List<BundleRequirement> _packageRequirements;
	private boolean _verbose = false;

}