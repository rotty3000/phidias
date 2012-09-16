package org.phidias.compile;

import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * @author Raymond Augé
 */
public class BundleJavaFileObject extends SimpleJavaFileObject {

	public BundleJavaFileObject(URI uri, String name) {
		super(uri, Kind.CLASS);

		_name = name;
	}

	public String inferBinaryName() {
		return _name;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return toUri().toURL().openStream();
	}

	private String _name;

}