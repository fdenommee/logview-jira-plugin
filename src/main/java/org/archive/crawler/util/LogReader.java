/* Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.crawler.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.archive.util.ArchiveUtils;

/**
 * This class contains a variety of methods for reading log files (or other text 
 * files containing repeated lines with similar information).
 * <p>
 * All methods are static.
 *
 * @author Kristinn Sigurdsson
 */

public class LogReader
{
    public static String buildDisplayingHeader(int len, long logsize)
    {
        double percent = 0.0;
        if (logsize != 0) {
            percent = ((double) len/logsize) * 100;
        }
        return "Displaying: " + ArchiveUtils.doubleToString(percent,1) +
            "% of " + ArchiveUtils.formatBytesForDisplay(logsize);
    }

    /**
     * Implementation of a unix-like 'tail -n' command
     *
     * @param aFileName a file name String
     * @param n int number of lines to be returned
     * @return An array of two strings is returned. At index 0 the String
     *         representation of at most n last lines is located.
     *         At index 1 there is an informational string about how large a
     *         segment of the file is being returned.
     *         Null is returned if errors occur (file not found or io exception)
     */
    public static String[] tail(String aFileName, int n) {
        try {
            return tail(new RandomAccessFile(new File(aFileName),"r"),n);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Implementation of a unix-like 'tail -n' command
     *
     * @param raf a RandomAccessFile to tail
     * @param n int number of lines to be returned
     * @return An array of two strings is returned. At index 0 the String
     *         representation of at most n last lines is located.
     *         At index 1 there is an informational string about how large a
     *         segment of the file is being returned.
     *         Null is returned if errors occur (file not found or io exception)
     */
    public static String[] tail(RandomAccessFile raf, int n) {
        int BUFFERSIZE = 1024;
        long pos;
        long endPos;
        long lastPos;
        int numOfLines = 0;
        String info=null;
        byte[] buffer = new byte[BUFFERSIZE];
        StringBuffer sb = new StringBuffer();
        try {
            endPos = raf.length();
            lastPos = endPos;

            // Check for non-empty file
            // Check for newline at EOF
            if (endPos > 0) {
                byte[] oneByte = new byte[1];
                raf.seek(endPos - 1);
                raf.read(oneByte);
                if ((char) oneByte[0] != '\n') {
                    numOfLines++;
                }
            }

            do {
                // seek back BUFFERSIZE bytes
                // if length of the file if less then BUFFERSIZE start from BOF
                pos = 0;
                if ((lastPos - BUFFERSIZE) > 0) {
                    pos = lastPos - BUFFERSIZE;
                }
                raf.seek(pos);
                // If less then BUFFERSIZE avaliable read the remaining bytes
                if ((lastPos - pos) < BUFFERSIZE) {
                    int remainer = (int) (lastPos - pos);
                    buffer = new byte[remainer];
                }
                raf.readFully(buffer);
                // in the buffer seek back for newlines
                for (int i = buffer.length - 1; i >= 0; i--) {
                    if ((char) buffer[i] == '\n') {
                        numOfLines++;
                        // break if we have last n lines
                        if (numOfLines > n) {
                            pos += (i + 1);
                            break;
                        }
                    }
                }
                // reset last postion
                lastPos = pos;
            } while ((numOfLines <= n) && (pos != 0));

            // print last n line starting from last postion
            for (pos = lastPos; pos < endPos; pos += buffer.length) {
                raf.seek(pos);
                if ((endPos - pos) < BUFFERSIZE) {
                    int remainer = (int) (endPos - pos);
                    buffer = new byte[remainer];
                }
                raf.readFully(buffer);
                sb.append(new String(buffer));
            }

            info = buildDisplayingHeader(sb.length(), raf.length());
        } catch (FileNotFoundException e) {
            sb = null;
        } catch (IOException e) {
            e.printStackTrace();
            sb = null;
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(sb==null){
            return null;
        }
        String[] tmp = {sb.toString(),info};
        return tmp;
    }
}
