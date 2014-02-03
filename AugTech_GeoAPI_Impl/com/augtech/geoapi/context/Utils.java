/*
 * Copyright 2014, Augmented Technologies Ltd.
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
 */
package com.augtech.geoapi.context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opengis.context.Content;
import org.opengis.context.Offering;
import org.opengis.context.Operation;

/** A few utilities to aid processing Context Document information
 * 
 * @author Augmented Technologies Ltd.
 *
 */
public class Utils {
	static final int BUFFER = 1024;
	
	/** Copy all in-line contents or results from the supplied {@link Offering} to
	 * a single file.
	 * 
	 * @param offering The Offering to process
	 * @param outFile The resultant file
	 * @throws Exception
	 */
	public static void contextContentsToFile(Offering offering, File outFile) 
			throws Exception {
		
		if (offering.getContentsCount()>0) {

			/* TODO: Can the Parsers handle potentially multiple collections
			 * within a single file ? */
			for (Content c : offering.getContents()) {

				if (c.getURI()!=null) {
					// Referenced content
					throw new IllegalArgumentException("Referenced Context Content not implemented yet!");
				} else {
					// In-line content
					streamCopy(
							new ByteArrayInputStream(c.getContent().getBytes()), 
							new FileOutputStream(outFile, true)
							);
				}
			}
		} else {
			// No contents, so must be 'result'
			for (Operation o : offering.getOperations()) {
				if (o.getResult().getURI()!=null) {
					// referenced result
					throw new IllegalArgumentException("Referenced Context Result not implemented yet!");
				} else {
					// in-line result
					o.getResult().getContent();

					streamCopy(
							new ByteArrayInputStream(o.getResult().getContent().getBytes()), 
							new FileOutputStream(outFile, true)
							);

				}

			}
		}
	}
	
	/** Copy an inputstream to an output stream. Generally used for saving 
	 * to disk from download. Both streams are closed on completion.
	 * 
	 * @param in InputStream  Source
	 * @param out OutputStream  Destination
	 * @throws IOException
	 */
    public static void streamCopy(InputStream in, OutputStream out) throws IOException{
        byte[] b = new byte[BUFFER];
        int read;
        while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
        }
        out.close();
        in.close();
    }
	
}
