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

	@Override
	public String toString() {
		return _className.concat(": ").concat(super.toString());
	}

	private String _className;

}