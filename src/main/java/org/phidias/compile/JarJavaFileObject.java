/**
 * Copyright 2012 Liferay Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

		super(uri, className, resourceURL);

		_entryName = entryName;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		JarURLConnection jarUrlConnection =
			(JarURLConnection)url.openConnection();

		JarFile jarFile = jarUrlConnection.getJarFile();

		JarEntry jarEntry = jarFile.getJarEntry(_entryName);

		return jarFile.getInputStream(jarEntry);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat("[").concat(
			url.toString()).concat("]");
	}

	private String _entryName;

}