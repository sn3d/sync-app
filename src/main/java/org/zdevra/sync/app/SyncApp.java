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

import org.zdevra.sync.Sync;
import org.zdevra.sync.SyncError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class SyncApp implements ActionListener {

	private static SyncPreferences configuration;

	private MenuItem preferencesItem;
	private MenuItem helpItem;
	private MenuItem aboutItem;
	private MenuItem closeItem;
	private MenuItem syncItem;

	private ExecutorService executor = Executors.newFixedThreadPool(1);


	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			throw new SyncError("Error in initialization", e);
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SyncApp app = new SyncApp();
				app.createAndShowGUI();
			}
		});
	}


	public static SyncPreferences getConfiguration() {
		if (configuration == null) {
			configuration = new SyncPreferences();
		}
		return configuration;
	}


	private void createAndShowGUI() {
		try {
			//initialize Tray with Icon
			if (!SystemTray.isSupported()) {
				throw new SyncError("SystemTray is not supported.");
			}

			SystemTray tray = SystemTray.getSystemTray();
			URL imageURL = SyncApp.class.getResource("/images/icon.png");
			if (imageURL == null) {
				throw new SyncError("image /images/icon.png not found");
			}

			Image icon = (new ImageIcon(imageURL, "tray icon")).getImage();
			TrayIcon trayIcon = new TrayIcon(icon);

			//sync
			syncItem = new MenuItem("Synchronization");
			syncItem.addActionListener(this);

			//preferences
			preferencesItem = new MenuItem("Preferences...");
			preferencesItem.addActionListener(this);

			//help
			helpItem = new MenuItem("Help");
			helpItem.addActionListener(this);

			//about
			aboutItem = new MenuItem("About");
			aboutItem.addActionListener(this);

			//close
			closeItem = new MenuItem("Close");
			closeItem.addActionListener(this);

			PopupMenu popup = new PopupMenu();
			popup.add(syncItem);
			popup.addSeparator();
			popup.add(preferencesItem);
			popup.add(helpItem);
			popup.add(aboutItem);
			popup.addSeparator();
			popup.add(closeItem);

			trayIcon.setPopupMenu(popup);
			tray.add(trayIcon);

			if (!getConfiguration().load()) {
				onClickPreferences();
			}

		} catch (AWTException e) {
			throw new SyncError("Error in AWT", e);
		} catch (IOException e) {
			throw new SyncError("IO error ", e);
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == closeItem) {
			onClose();
		} else if (e.getSource() == helpItem) {
			onClickHelp();
		} else if (e.getSource() == aboutItem) {
			onClickAbout();
		} else if (e.getSource() == preferencesItem) {
			onClickPreferences();
		} else if (e.getSource() == syncItem) {
			onSyncStart();
		}
	}

	private void onClickAbout() {
		JOptionPane.showMessageDialog(null, SyncConstants.APPNAME + " version " + SyncConstants.VERSION);
	}

	private void onClickHelp() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null) {
			try {
				desktop.browse(URI.create(SyncConstants.HOMEPAGE));
			} catch (IOException e) {
				throw new SyncError("IO error", e);
			}
		}
	}

	private void onClickPreferences() {
		PreferencesDialog dialog = new PreferencesDialog();
	}

	private void onSyncStart() {
		syncItem.setEnabled(false);
		syncItem.setLabel("Synchronizing...");
		executor.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Sync sync = Sync.createForFilesystem(getConfiguration().getPrimaryDir(), getConfiguration().getSecondaryDir());
				sync.sync();
				onSyncEnd();
				return null;
			}
		});
	}

	protected void onSyncEnd() {
		syncItem.setLabel("Synchronize");
		syncItem.setEnabled(true);
	}

	private void onClose() {
		System.exit(0);
	}

}
