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

import org.apache.log4j.Logger;
import org.zdevra.sync.SyncMode;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Preferences dialog class
 *
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public class PreferencesDialog extends JFrame {

	//------------------------------------------------------------------------------------------------------------------
	// members
	//------------------------------------------------------------------------------------------------------------------

	/** log instance*/
	static Logger log = Logger.getLogger(PreferencesDialog.class);

	/** primary directory input field */
	private JTextField primaryField;

	/** secondary directory input field */
	private JTextField secondaryField;

	/** sync mode */
	private SyncMode syncMode;

	//------------------------------------------------------------------------------------------------------------------
	// methods
	//------------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public PreferencesDialog() {
		super(SyncConstants.APPNAME);
		initalize();
	}


	/**
	 * GUI Initialization method
	 */
	private void initalize() {
		try {
			setSize(400, 220);
			setLocationRelativeTo(null);
			setResizable(false);
			setAlwaysOnTop(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			//primary dir choose btn & label
			JPanel primaryPanel = new JPanel();
			primaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Primary"));
			primaryPanel.setLayout(new FlowLayout());

			primaryField = new JTextField("", 20);
			primaryField.setText(SyncApp.getConfiguration().getPrimaryDirAsString());
			primaryPanel.add(primaryField);

			JButton selectPrimaryBtn = new JButton("...");
			selectPrimaryBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClickPrimaryDir();
				}
			});
			primaryPanel.add(selectPrimaryBtn);

			//secondary dir choose btn & label
			JPanel secondaryPanel = new JPanel();
			secondaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Secondary"));
			secondaryPanel.setLayout(new FlowLayout());

			secondaryField = new JTextField("", 20);
			secondaryField.setText(SyncApp.getConfiguration().getSecondaryDirAsString());
			secondaryPanel.add(secondaryField);

			JButton selectSecondaryBtn = new JButton("...");
			selectSecondaryBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClickSecondaryDir();
				}
			});
			secondaryPanel.add(selectSecondaryBtn);

			//sync mode
			JRadioButton syncOneDirectionalRadioBtn = new JRadioButton("one directional");
			syncOneDirectionalRadioBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClickSyncMode(SyncMode.ONE_DIRECTIONAL);
				}
			});

			JRadioButton syncBiDirectionalRadioBtn = new JRadioButton("bi directional");
			syncBiDirectionalRadioBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onClickSyncMode(SyncMode.BI_DIRECTIONAL);
				}
			});

			this.syncMode = SyncApp.getConfiguration().getSyncMode();
			switch (this.syncMode) {
				case ONE_DIRECTIONAL:
					syncOneDirectionalRadioBtn.setSelected(true);
					break;
				case BI_DIRECTIONAL:
					syncBiDirectionalRadioBtn.setSelected(true);
					break;
			}

			ButtonGroup syncModeGroup = new ButtonGroup();
			syncModeGroup.add(syncOneDirectionalRadioBtn);
			syncModeGroup.add(syncBiDirectionalRadioBtn);

			JPanel syncModePanel = new JPanel();
			syncModePanel.setLayout(new FlowLayout());
			syncModePanel.add(syncOneDirectionalRadioBtn);
			syncModePanel.add(syncBiDirectionalRadioBtn);


			// close button
			JButton closeBtn = new JButton("Apply");
			closeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onApply();
				}
			});
			closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

			// main panel
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setAlignmentX(Component.CENTER_ALIGNMENT);

			panel.add(syncModePanel);
			panel.add(primaryPanel);
			panel.add(secondaryPanel);
			panel.add(closeBtn);

			add(panel);
			setVisible(true);

		} catch (Throwable e) {
			log.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * Event ivoked when sync mode is changing
	 * @param mode
	 */
	private void onClickSyncMode(SyncMode mode) {
		this.syncMode = mode;
	}


	/**
	 * Event ivoked when user clicked to choose primary directory
	 */
	private void onClickPrimaryDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int res = fc.showDialog(this, "choose");
		if (res == 0) {
			String primaryDir = fc.getSelectedFile().getAbsolutePath();
			primaryField.setText(primaryDir);
		}
	}


	/**
	 * Event ivoked when user clicked to choose secondary directory
	 */
	private void onClickSecondaryDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int res = fc.showDialog(this, "choose");
		if (res == 0) {
			String secondaryDir = fc.getSelectedFile().getAbsolutePath();
			secondaryField.setText(secondaryDir);
		}
	}

	/**
	 * Event invoked when user clicked to 'apply' button.
	 */
	private void onApply() {
		try {
			File primaryDir = new File(this.primaryField.getText());
			SyncApp.getConfiguration().setPrimaryDir(primaryDir);

			File secondaryDir = new File(this.secondaryField.getText());
			SyncApp.getConfiguration().setSecondaryDir(secondaryDir);

			SyncApp.getConfiguration().setSyncMode(this.syncMode);

			SyncApp.getConfiguration().validate();
			SyncApp.getConfiguration().save();

			this.dispose();
		} catch (Throwable e) {
			log.error(e);
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
