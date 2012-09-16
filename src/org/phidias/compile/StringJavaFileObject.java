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