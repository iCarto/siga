
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jump.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * File-related utility functions.
 */
public class FileUtil {
    /**
     * Reads a text file.
     * @param textFileName the pathname of the file to open
     * @return the lines of the text file
     * @throws FileNotFoundException if the text file is not found
     * @throws IOException if the file is not found or another I/O error occurs
     */
    public static List getContents(String textFileName)
        throws FileNotFoundException, IOException {
        List contents = new ArrayList();
        FileReader fileReader = new FileReader(textFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();

        while (line != null) {
            contents.add(line);
            line = bufferedReader.readLine();
        }

        return contents;
    }

    /**
     * Saves the String to a file with the given filename.
     * @param textFileName the pathname of the file to create (or overwrite)
     * @param contents the data to save
     * @throws IOException if an I/O error occurs.
     */
    public static void setContents(String textFileName, String contents)
        throws IOException {
        FileWriter fileWriter = new FileWriter(textFileName, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(contents);
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
    }

	public static List getContents(InputStream inputStream) throws IOException {
		ArrayList contents = new ArrayList();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		try {
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			try {
				String line = bufferedReader.readLine();
				while (line != null) {
					contents.add(line);
					line = bufferedReader.readLine();
				}
			} finally {
				bufferedReader.close();
			}
		} finally {
			inputStreamReader.close();
		}
		return contents;
	}


    /**
     * Saves the List of Strings to a file with the given filename.
     * @param textFileName the pathname of the file to create (or overwrite)
     * @param lines the Strings to save as lines in the file
     * @throws IOException if an I/O error occurs.
     */
    public static void setContents(String textFileName, List lines)
        throws IOException {
        String contents = "";

        for (Iterator i = lines.iterator(); i.hasNext();) {
            String line = (String) i.next();
            contents += (line + System.getProperty("line.separator"));
        }

        setContents(textFileName, contents);
    }
}
