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

package org.phidias.compile;

import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * @author Raymond Augé
 */
public class BundleJavaFileObject extends SimpleJavaFileObject {

	public BundleJavaFileObject(URI uri, String className) {
		super(uri, Kind.CLASS);

		_className = className;
	}

	public String inferBinaryName() {
		return _className;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return toUri().toURL().openStream();
	}

	private String _className;

}