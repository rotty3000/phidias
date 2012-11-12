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