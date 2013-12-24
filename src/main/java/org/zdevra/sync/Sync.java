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
package org.zdevra.sync;

import org.zdevra.sync.filesystem.FilesystemRepository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class Sync {

	//------------------------------------------------------------------------------------------------------------------
	// members
	//------------------------------------------------------------------------------------------------------------------

	/** the primary (usually local) repository */
	private final ISyncRepository primaryRepo;

	private List<ISyncFile> primaryFiles;

	/** the secondary (usually remote or shared) directory */
	private final ISyncRepository secondaryRepo;

	private List<ISyncFile> secondaryFiles;

	/** determines sync mode. */
	private SyncMode mode;


	//------------------------------------------------------------------------------------------------------------------
	// construction
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Construct the Sync object for filesystem directories
	 */
	public static Sync createForFilesystem(File primaryDir, File secondaryDir) {
		return new Sync(
				new FilesystemRepository(primaryDir),
				new FilesystemRepository(secondaryDir));
	}


	/**
	 * Constructor
	 */
	public Sync(ISyncRepository primary, ISyncRepository secondary) {
		this.primaryRepo = primary;
		this.secondaryRepo = secondary;
		this.mode = SyncMode.BI_DIRECTIONAL;
	}


	//------------------------------------------------------------------------------------------------------------------
	// methods
	//------------------------------------------------------------------------------------------------------------------


	public void setSyncMode(SyncMode mode) {
		this.mode = mode;
	}

	/**
	 * do synchronization
	 */
	public void sync() {
		//scan
		primaryFiles = primaryRepo.scan();
		secondaryFiles = secondaryRepo.scan();

		//go through files and merge them
		merge();
	}


	private void merge() {
		// go through primary files and merge them with secondary
		for (ISyncFile a : primaryFiles) {
			ISyncFile b = getAndRemoveFile(a, secondaryFiles);
			if (b != null) {
				merge(a, b);
			} else {
				copy(a, secondaryRepo);
			}
		}

		//copy rest of missing files from secondary to primary
		if (mode == SyncMode.BI_DIRECTIONAL) {
			for (ISyncFile b : secondaryFiles) {
				copy(b, primaryRepo);
			}
		}
	}


	private void merge(ISyncFile primary, ISyncFile secondary) {
		try {
			long primaryTime = primary.timestamp();
			long secondaryTime = secondary.timestamp();
			if (primaryTime > secondaryTime) {
				copy(primary, secondaryRepo);
			} else if (primaryTime < secondaryTime) {
				copy(secondary, primaryRepo);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void copy(ISyncFile source, ISyncRepository destinationRepo) {
		OutputStream os = null;
		try {
			//copy
			os = destinationRepo.openStream(source.path());
			source.copyTo(os);

			//touch
			ISyncFile destinationFile = destinationRepo.getFile(source.path());
			long timestamp = source.timestamp();
			destinationFile.touch(timestamp);
		} catch (IOException e) {
			throw new SyncError("error when copy " + source.path(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					throw new SyncError("Error closing the stream when copy " + source.path(), e);
				}
			}
		}
	}


	public static ISyncFile getAndRemoveFile(ISyncFile target, Collection<ISyncFile> collection) {
		String targetPath = target.path();
		for(ISyncFile f : collection) {
			if (targetPath.equalsIgnoreCase(f.path())) {
				collection.remove(f);
				return f;
			}
		}
		return null;
	}

}
