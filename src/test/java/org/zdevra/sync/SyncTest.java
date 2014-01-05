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

import junit.framework.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.*;

/**
 * @author Zdenko Vrabel (vrabel.zdenko@gmail.com)
 */
@Test
public class SyncTest {

	private File primaryDir;
	private File secondaryDir;
	private final static String SUBTESTFILE =  "folder/subtest1.txt";


	@BeforeTest
	@Parameters({"primary", "secondary"})
	public void init(
			@Optional("./target/test-dirs/primary") String primary,
			@Optional("./target/test-dirs/secondary") String secondary)
	{
		primaryDir = new File(primary);
		secondaryDir = new File(secondary);
	}


	@Test
	public void testFirstSync() throws IOException {
		//sync the folders
		Sync sync = Sync.createForFilesystem(primaryDir, secondaryDir);
		sync.sync();

		//check the results
		File primaryTestFile = new File(primaryDir, SUBTESTFILE);
		Assert.assertTrue(primaryTestFile.exists());

		File secondaryTestFile = new File(secondaryDir, SUBTESTFILE);
		Assert.assertTrue(secondaryTestFile.exists());

		long primaryTimestamp = primaryTestFile.lastModified();
		long secondaryTimestamp = secondaryTestFile.lastModified();
		Assert.assertEquals(primaryTimestamp, secondaryTimestamp);
	}


	@Test(dependsOnMethods = "testFirstSync")
	public void testSecondSync() throws IOException, InterruptedException {
		//modify subtest1.txt file in primary repository
		File subtestFile = new File(primaryDir, SUBTESTFILE);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(subtestFile, true)));
		out.println("\nmodif 1");
		out.close();

		//wait 1 second
		Thread.sleep(1000);

		//sync the folders
		Sync sync = Sync.createForFilesystem(primaryDir, secondaryDir);
		sync.sync();

		//check the result
		long originalLen = subtestFile.length();
		long secondaryLen = new File(secondaryDir, SUBTESTFILE).length();
		Assert.assertEquals(originalLen, secondaryLen);
	}


	@Test(dependsOnMethods = "testSecondSync")
	public void testThirdSync() throws IOException, InterruptedException {
		//modify subtest1.txt file in primary repository
		File subtestFile = new File(secondaryDir, SUBTESTFILE);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(subtestFile, true)));
		out.println("\nmodif 2");
		out.close();

		//wait 1 second
		Thread.sleep(1000);

		//sync the folders
		Sync sync = Sync.createForFilesystem(primaryDir, secondaryDir);
		sync.sync();

		//check the result
		long originalLen = subtestFile.length();
		long primaryLen = new File(primaryDir, SUBTESTFILE).length();
		Assert.assertEquals(originalLen, primaryLen);
	}

}