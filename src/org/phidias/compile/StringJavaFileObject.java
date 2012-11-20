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