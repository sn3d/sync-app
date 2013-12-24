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
package org.zdevra.sync.app;

import org.zdevra.sync.SyncMode;

import java.io.*;
import java.util.Properties;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class SyncPreferences {

	private File primaryDir;
	private File secondaryDir;
	private SyncMode syncMode = SyncMode.BI_DIRECTIONAL;

	public File getPrimaryDir() {
		return primaryDir;
	}

	public String getPrimaryDirAsString() {
		if (primaryDir == null) {
			return "none";
		}
		return primaryDir.getAbsolutePath();
	}

	public void setPrimaryDir(File primaryDir) {
		this.primaryDir = primaryDir;
	}

	public File getSecondaryDir() {
		return secondaryDir;
	}

	public String getSecondaryDirAsString() {
		if (secondaryDir == null) {
			return "none";
		}
		return secondaryDir.getAbsolutePath();
	}

	public void setSecondaryDir(File secondaryDir) {
		this.secondaryDir = secondaryDir;
	}

	public SyncMode getSyncMode() {
		return syncMode;
	}

	public void setSyncMode(SyncMode syncMode) {
		this.syncMode = syncMode;
	}

	public boolean load() throws IOException {
		File preferencesFile = getPreferencesFile();
		if (!preferencesFile.exists()) {
			return false;
		}

		Properties properties = new Properties();
		properties.load(new FileInputStream(preferencesFile));
		primaryDir = new File((String)properties.get("primary.dir"));
		secondaryDir = new File((String)properties.get("secondary.dir"));
		syncMode = SyncMode.from((String) properties.get("syncmode"));

		return true;
	}


	public void save() throws IOException {
		Properties properties = new Properties();
		properties.put("primary.dir", primaryDir.getAbsolutePath());
		properties.put("secondary.dir", secondaryDir.getAbsolutePath());
		properties.put("syncmode", syncMode.toString());

		File preferencesFile = getPreferencesFile();
		if (!preferencesFile.getParentFile().exists()) {
			preferencesFile.getParentFile().mkdirs();
		}

		if (!preferencesFile.exists()) {
			preferencesFile.createNewFile();
		}
		properties.store(new FileOutputStream(preferencesFile), "");
	}


	protected File getPreferencesFile() {
		File home = new File(System.getProperty("user.home"));
		String os = System.getProperty("os.name");
		os = os.toLowerCase();

		if (os != null) {
			if ( os.contains("mac") || os.contains("darwin") ) {
				return new File(home, "/Library/Preferences/Sync/sync.properties");
			} else if (os.contains("win")) {
				//TODO: path to windows properties
			} else {

			}
		}
		return null;
	}
}
