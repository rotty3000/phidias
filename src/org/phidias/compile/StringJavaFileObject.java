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

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * @author Raymond Augé
 */
public class StringJavaFileObject extends SimpleJavaFileObject
	implements Constants {

	public StringJavaFileObject(String name, String source) {
		super(
			URI.create(PROTOCOL_STRING.concat(name.replace('.', '/').concat(
				Kind.SOURCE.extension))),
			Kind.SOURCE);

		_source = source;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
		throws IOException {

		return _source;
	}

	private final String _source;

}