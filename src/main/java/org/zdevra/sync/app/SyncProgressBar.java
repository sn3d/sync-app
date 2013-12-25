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
import org.zdevra.sync.ISyncFile;
import org.zdevra.sync.ISyncRepository;
import org.zdevra.sync.SyncEvent;
import org.zdevra.sync.SyncEventListener;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public abstract class SyncProgressBar implements SyncEventListener {

	/** log4j instance */
	static Logger log = Logger.getLogger(SyncProgressBar.class);

	/** sum of all files the sync will process. Needed for progress bar */
	private long filesSum;

	/** file index which file is processing. Needed for progress bar */
	private long fileIndex;


	/**
	 * Constructor
	 */
	public SyncProgressBar() {
		filesSum = 0;
		fileIndex = 0;
	}

	/**
	 * here you will implement your progressbar show code
	 * @param percentage
	 */
	protected abstract void  processing(double percentage);


	/**
	 * this implementation handling events and compute percentage
	 */
	@Override
	public void listen(SyncEvent e) {
		if (e instanceof SyncEvent.StartEvent) {
			filesSum = ((SyncEvent.StartEvent) e).count();
		} else if (e instanceof SyncEvent.ProcessFileEvent) {
			fileIndex++;
		} else if (e instanceof SyncEvent.CopyEvent) {
			ISyncFile file = ((SyncEvent.CopyEvent) e).getSyncFile();
			ISyncRepository dest = ((SyncEvent.CopyEvent) e).getDestination();
			log.info("copy " + file.path() + " -> " + dest.toString());
			double percentage = (fileIndex * 100.0d) / filesSum;
			processing(percentage);
		}
	}
}
