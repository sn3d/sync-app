/*****************************************************************************
 * Copyright 2013 Zdenko Vrabel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/
package org.zdevra.sync.filesystem;

import org.zdevra.sync.ISyncFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
class FilesystemFile implements ISyncFile {

	//------------------------------------------------------------------------------------------------------------------
	// members
	//------------------------------------------------------------------------------------------------------------------

	private final File root;
	private final File path;
	private final String subpath;


	//------------------------------------------------------------------------------------------------------------------
	// methods
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * extract the path from root and source
	 */
	static ISyncFile create(File root, File source) {
		String pathStr = source.getAbsolutePath();
		String rootStr = root.getAbsolutePath();
		String subpath = pathStr.substring(rootStr.length());
		return new FilesystemFile(root, source, subpath);
	}

	/**
	 * Constructor
	 *
	 * @param root
	 * @param path
	 * @param subpath
	 */
	private FilesystemFile(File root, File path, String subpath) {
		this.root = root;
		this.path = path;
		this.subpath = subpath;
	}


	@Override
	public String path() {
		return this.subpath;
	}


	@Override
	public long timestamp() throws IOException {
		return path.lastModified();
	}


	@Override
	public void touch(long timestamp) throws IOException {
		path.setLastModified(timestamp);
	}


	@Override
	public void copyTo(OutputStream destination) throws IOException {
		FileInputStream source = new FileInputStream(path);
		try {
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = source.read(buf)) > 0) {
				destination.write(buf, 0, bytesRead);
			}
		} finally {
			source.close();
			destination.close();
		}
	}


	@Override
	public String toString() {
		return path();
	}
}
