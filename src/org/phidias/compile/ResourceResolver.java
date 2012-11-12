/**
 * Copyright (c) 2012 Liferay Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the Apache License, Version 2.0 as published by the The Apache
 * Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Apache License, Version 2.0 for more details.
 */

package org.phidias.compile;

import java.net.URL;

import java.util.Collection;

import org.osgi.framework.wiring.BundleWiring;

/**
 * @author Raymond Augé
 */
public interface ResourceResolver {

	public URL getResource(BundleWiring bundleWiring, String name);

	public Collection<String> resolveResources(
		BundleWiring bundleWiring, String path, String filePattern,
		int options);

}