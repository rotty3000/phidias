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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Date;

/**
 * @author Raymond Augé
 */
public class TPhLog {

	public PrintStream out;
	public String pattern = "%1$tH:%1$tM:%1$tS,%1$tL [%2$s:%3$d] %4$s %5$s\n";

	public void log(Object... input) {
		if (out == null) {
			return;
		}

		StackTraceElement stackTraceElement =
			Thread.currentThread().getStackTrace()[2];

		StringWriter sw1 = new StringWriter();
		PrintWriter pw1 = new PrintWriter(sw1);

		StringWriter sw2 = new StringWriter();
		PrintWriter pw2 = new PrintWriter(sw2);

		for (Object element : input) {
			if (element instanceof Throwable) {
				pw2.println();

				((Throwable)element).printStackTrace(pw2);
			}
			else {
				pw1.print(' ');
				pw1.print(element);
			}
		}

		out.printf(
			pattern, new Date(), stackTraceElement.getClassName(),
			stackTraceElement.getLineNumber(), sw1.toString(), sw2.toString());
	}

}