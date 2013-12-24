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
import org.zdevra.sync.ISyncRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class FilesystemRepository implements ISyncRepository {

	//------------------------------------------------------------------------------------------------------------------
	// members
	//------------------------------------------------------------------------------------------------------------------
	private final File rootDir;


	//------------------------------------------------------------------------------------------------------------------
	// methods
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor
	 *
	 * @param rootDir
	 */
	public FilesystemRepository(File rootDir) {
		this.rootDir = rootDir;
	}


	@Override
	public OutputStream openStream(String path) throws IOException {
		File file = new File(rootDir, path);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			file.createNewFile();
		}

		return new FileOutputStream(file);
	}


	@Override
	public String toString() {
		return "file:/" + rootDir.toString();
	}


	@Override
	public ISyncFile getFile(String path) {
		return FilesystemFile.create(rootDir, new File(rootDir, path));
	}


	@Override
	public List<ISyncFile> scan() {
		return scanDir(rootDir, rootDir);
	}


	private static List<ISyncFile> scanDir(File root, File dir) {
		File[] files = dir.listFiles();
		List<ISyncFile> out = new LinkedList<ISyncFile>();
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					ISyncFile file = FilesystemFile.create(root, f);
					out.add(file);
				} else if (f.isDirectory()) {
					List<ISyncFile> items = scanDir(root, f);
					out.addAll(items);
				}
			}
		}
		return out;
	}
}
