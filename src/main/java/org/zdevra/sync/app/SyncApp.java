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

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.zdevra.sync.Sync;
import org.zdevra.sync.SyncError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class SyncApp implements ActionListener {

	static Logger log = Logger.getLogger(SyncApp.class);

	private static SyncPreferences configuration;

	private MenuItem preferencesItem;
	private MenuItem helpItem;
	private MenuItem aboutItem;
	private MenuItem closeItem;
	private MenuItem syncItem;

	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private DecimalFormat format = new DecimalFormat("#.#");


	public static void main(String[] args) {

		//init log4j
		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile(SyncConstants.LOG_FILE.getAbsolutePath());
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();
		Logger.getRootLogger().addAppender(fa);

		//check JAVA version
		log.info("java.version=" + System.getProperty("java.version"));
		log.info("java.home=" + System.getProperty("java.home"));

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error("Error in initalization", e);
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
			log.info("start app");

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

		} catch (Exception e) {
			log.error("Error:" + e.getMessage(), e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			throw new SyncError("Error", e);
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
		JOptionPane.showMessageDialog(null, SyncConstants.APPNAME + " version " + SyncConstants.VERSION, "About", JOptionPane.PLAIN_MESSAGE);
	}


	private void onClickHelp() {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null) {
			try {
				desktop.browse(URI.create(SyncConstants.HOMEPAGE));
			} catch (IOException e) {
				log.error("IO Error:" + e.getMessage(), e);
			}
		}
	}


	private void onClickPreferences() {
		PreferencesDialog dialog = new PreferencesDialog();
	}


	private void onSyncStart() {
		log.info("start sync.");

		syncItem.setEnabled(false);
		syncItem.setLabel("Synchronizing ");

		executor.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					//check the configuration whether is valid
					getConfiguration().validate();

					//do sync
					Sync sync = Sync.createForFilesystem(getConfiguration().getPrimaryDir(), getConfiguration().getSecondaryDir());
					sync.addEventListener(new SyncProgressBar() {
						@Override
						protected void processing(double percentage) {
							syncItem.setLabel("Synchronizing (" + format.format(percentage) + "%)");
						}
					});
					sync.sync();

				} catch (Throwable e) {
					log.error("Sync error:" + e.getMessage(), e);
					JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
				}

				onSyncEnd();
				return null;
			}
		});
	}


	protected void onSyncEnd() {
		log.info("end sync.");
		syncItem.setLabel("Synchronize");
		syncItem.setEnabled(true);
	}


	private void onClose() {
		log.info("end");
		System.exit(0);
	}
}
