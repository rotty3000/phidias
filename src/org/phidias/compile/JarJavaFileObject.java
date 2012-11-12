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

import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Raymond Augé
 */
public class JarJavaFileObject extends BundleJavaFileObject {

	public JarJavaFileObject(
		URI uri, String className, URL resourceURL, String entryName) {

		super(uri, className);

		_resourceURL = resourceURL;
		_entryName = entryName;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		JarURLConnection jarUrlConnection =
			(JarURLConnection)_resourceURL.openConnection();

		JarFile jarFile = jarUrlConnection.getJarFile();

		JarEntry jarEntry = jarFile.getJarEntry(_entryName);

		return jarFile.getInputStream(jarEntry);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat("[").concat(
			_resourceURL.toString()).concat("]");
	}

	private String _entryName;
	private URL _resourceURL;

}