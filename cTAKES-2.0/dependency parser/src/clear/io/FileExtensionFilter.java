/**
* Copyright (c) 2010, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This class filters out files by the extension (e.g., *.jpg, *.txt).
 * @author Jinho D. Choi
 * <b>Last update:</b> 02/12/2010
 */
public class FileExtensionFilter implements FilenameFilter
{
	/** File extension to keep (everything else gets filtered out) */
	private String ext;
	
	/**
	 * Initializses file-extension filter.
	 * @param ext extension of files to keep (everything else gets filtered out)
	 */
	public FileExtensionFilter(String ext)
	{
		this.ext = ext;
	}

	/** Returns true if the <code>name</code> ends with {@link FileExtensionFilter#ext}. */
	public boolean accept(File dir, String name)
	{
		return name.endsWith(ext);
	}

	/** Returns the filename without the extension (e.g., ab.txt -> ab). */
	static public String getFilenameWithoutExtension(String filename)
	{
		return filename.substring(0, filename.lastIndexOf("."));
	}
}
