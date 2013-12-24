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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
class FilesystemFile implements ISyncFile {

	//------------------------------------------------------------------------------------------------------------------
	// members
	//------------------------------------------------------------------------------------------------------------------

	private final Path root;
	private final Path path;
	private final Path subpath;


	//------------------------------------------------------------------------------------------------------------------
	// methods
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * extract the path from root and source
	 */
	static ISyncFile create(File root, File source) {
		int rootCount = root.toPath().getNameCount();
		int sourceCount = source.toPath().getNameCount();
		Path subpath = source.toPath().subpath(rootCount, sourceCount);
		return new FilesystemFile(root.toPath(), source.toPath(), subpath);
	}

	/**
	 * Constructor
	 *
	 * @param root
	 * @param path
	 * @param subpath
	 */
	private FilesystemFile(Path root, Path path, Path subpath) {
		this.root = root;
		this.path = path;
		this.subpath = subpath;
	}


	@Override
	public String path() {
		return this.subpath.toString();
	}


	@Override
	public long timestamp() throws IOException {
		FileTime time = Files.getLastModifiedTime(path);
		return time.toMillis();
	}


	@Override
	public void touch(long timestamp) throws IOException {
		FileTime time = FileTime.from(timestamp, TimeUnit.MILLISECONDS);
		Files.setLastModifiedTime(path, time);
	}


	@Override
	public void copyTo(OutputStream os) throws IOException {
		Files.copy(path, os);
	}


	@Override
	public String toString() {
		return path();
	}
}
