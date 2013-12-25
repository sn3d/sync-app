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

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
public interface SyncEvent {

	/**
	 * This event is invoked when sync is started and
	 * carry the information about total sum of files
	 * to merge
	 */
	public static class StartEvent implements SyncEvent {

		private final long count;

		StartEvent(long count) {
			this.count = count;
		}

		public long count() {
			return count;
		}
	}

	/**
	 * this event is invoked for each file in
	 * repositories doesn't matter whether file
	 * will be synced or not
	 */
	public static class ProcessFileEvent implements SyncEvent {

		private final ISyncFile file;

		ProcessFileEvent(ISyncFile file) {
			this.file = file;
		}

		public ISyncFile getFile() {
			return file;
		}
	}

	/**
	 * This event is invoked when file is copied or
	 * somehow merged from source to target repository
	 */
	public static class CopyEvent implements SyncEvent {

		private final ISyncFile syncFile;
		private final ISyncRepository destination;

		CopyEvent(ISyncFile syncFile, ISyncRepository destination) {
			this.syncFile = syncFile;
			this.destination = destination;
		}

		public ISyncFile getSyncFile() {
			return syncFile;
		}

		public ISyncRepository getDestination() {
			return destination;
		}
	}

}
