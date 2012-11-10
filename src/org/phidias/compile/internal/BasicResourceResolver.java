/**
 * Copyright (c) 2000-2012 Raymond Augé All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the Apache License, Version 2.0 as published by the The Apache
 * Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Apache License, Version 2.0 for more details.
 */

package org.phidias.compile.internal;

import java.net.URL;

import java.util.Collection;

import org.osgi.framework.wiring.BundleWiring;

import org.phidias.compile.ResourceResolver;

/**
 * @author Raymond Augé
 */
public class BasicResourceResolver implements ResourceResolver {

	public URL getResource(BundleWiring bundleWiring, String name) {
		return bundleWiring.getBundle().getResource(name);
	}

	public Collection<String> resolveResources(
		BundleWiring bundleWiring, String path, String filePattern,
		int options) {

		return bundleWiring.listResources(path, filePattern, options);
	}

}