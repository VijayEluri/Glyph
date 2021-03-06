/*
 * Copyright 2010 Fae Hutter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.sparktank.glyph.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ClassPathHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	static public interface FileProcessor {
		public boolean acceptFile (String filename);
		public boolean acceptFile (File file);
		public void procFile (File file);
		public void procStream (InputStream is);
	}
	
	static public void searchAndProcessFiles (FileProcessor processor) throws ZipException, IOException {
		final StringTokenizer tokenizer = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator, false);
		
		while (tokenizer.hasMoreTokens()) {
			final String classpathElement = tokenizer.nextToken();
			final File classpathFile = new File(classpathElement);
			
			if (classpathFile.exists() && classpathFile.canRead()) {
				if (classpathElement.toLowerCase(Locale.ENGLISH).endsWith(".jar")) { // Entry is a jar.
					final ZipFile zipFile = new ZipFile(classpathFile);
					try {
    					Enumeration<? extends ZipEntry> entries = zipFile.entries();
    					while (entries.hasMoreElements()) {
    						ZipEntry entry = entries.nextElement();
    						if (!entry.isDirectory()) {
    							if (processor.acceptFile(entry.getName())) {
    								InputStream is = zipFile.getInputStream(entry);
    								try {
    									processor.procStream(is);
    								}
    								finally {
    									is.close();
    								}
    							}
    						}
    					}
					}
					finally {
						zipFile.close();
					}
				}
				else if (classpathFile.isDirectory()) { // Entry is a dir.
					Stack<File> dirStack = new Stack<File>();
					dirStack.push(classpathFile);
					
					while (!dirStack.isEmpty()) {
						File[] files = dirStack.pop().listFiles();
						for (File file : files) {
							if (file.isDirectory()) {
								dirStack.push(file);
							}
							else {
								if (processor.acceptFile(file)) {
									processor.procFile(file);
								}
							}
						}
					}
				}
			}
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
