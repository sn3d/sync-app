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

import org.zdevra.sync.SyncError;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Provide you a global access to constants
 *
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class SyncConstants {

	public static final String APPNAME = (String)getProperties().get("application");
	public static final String VERSION = (String)getProperties().get("version");
	public static final String HOMEPAGE = (String)getProperties().get("homepage");

	public static final File PREFERENCES_FILE = getPreferencesFile();
	public static final File LOG_FILE = getLogFile();
	public static final OperationSystem OS = getOs();

	/** singletone properties of 'app.properties' */
	private static Properties properties;

	/**
	 * Enumeration for supported OS
	 */
	public static enum OperationSystem {
		MAC, LINUX, WINDOWS
	}


	/**
	 * inner method do lazy loading of properties
	 */
	private static Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
				properties.load(SyncConstants.class.getResourceAsStream("/app.properties"));
			} catch (IOException e) {
				throw new IllegalStateException("cannot open app.properties on classpath", e);
			}
		}
		return properties;
	}


	/**
	 * inner method detectd the OS
	 */
	private static OperationSystem getOs() {
		String os = System.getProperty("os.name");
		if (os != null) {
			os = os.toLowerCase();
			if ( os.contains("mac") || os.contains("darwin") ) {
				return OperationSystem.MAC;
			} else if (os.contains("win")) {
				return OperationSystem.WINDOWS;
			}
		}
		throw new SyncError("Unsupported OS:" + os);
	}


	/**
	 * inner method returns preferences file based on OS you're running
	 */
	private static File getPreferencesFile() {
		File home = new File(System.getProperty("user.home"));
		switch (getOs()) {
			case MAC:
				return new File(home, "/Library/Preferences/Sync/sync.properties");
			default:
				return null;
		}
	}


	/**
	 * inner method returns log file based on OS you're running
	 */
	private static File getLogFile() {
		File home = new File(System.getProperty("user.home"));
		switch (getOs()) {
			case MAC:
				return new File(home, "/Library/Logs/SyncApp/sync.log");
			default:
				return null;
		}
	}
}
