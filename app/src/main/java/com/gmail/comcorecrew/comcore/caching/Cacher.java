package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.classes.AppData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Cacher {

    /**
     * Caches the given list of cacheables into the modules cache file
     *
     * @param data data to be cached
     * @param module module that owns the data
     * @return true if data is cached; false if error occurs
     */
    public static boolean cacheData(ArrayList<Cacheable> data, Module module) {
        try {
            //Sets up cache file, creates new file, and sets up print writer.
            File cacheDir = new File(AppData.cacheDir, module.getGroupIdString());
            if ((!cacheDir.exists()) && (!cacheDir.mkdir())) {
                return false;
            }
            String filename = module.getLocatorString();
            File cacheFile = new File(AppData.cacheDir, filename);
            if ((!cacheFile.exists()) && (!cacheFile.createNewFile())) {
                return false;
            }
            PrintWriter pw = new PrintWriter(new FileWriter(cacheFile));

            //Adds number of lines to file, and for each cacheable, adds the length
            //of the data to the file and then the string of chars.
            int size = data.size();
            pw.print((char) (size >> 16));
            pw.print((char) size);
            for (Cacheable group : data) {
                char[] line = group.toCache();
                int lineLength = line.length;
                if (lineLength > AppData.maxData) {
                    throw new IllegalArgumentException();
                }
                pw.print((char) (lineLength >> 16));
                pw.print((char) lineLength);
                pw.print(line);
            }

            //Closes writer and returns true to indicate a success.
            pw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads the data stored in a modules cache file
     *
     * @param module the module that contains the data
     * @return the data in the file
     */
    public static char[][] uncacheData(Module module) {
        try {
            //Retrieves cache file, opens cache file, and reads the number of cache lines.
            String filename = module.getLocatorString();
//            System.out.println(filename);
            File cacheFile = new File(AppData.cacheDir, filename);
            BufferedReader br = new BufferedReader(new FileReader(cacheFile));
            int size = br.read();
            size = (size << 16) | br.read();

            //Creates array of chars and reads each line into the array.
            char[][] data = new char[size][];
            for (int i = 0; i < size; i++) {
                int lineSize = br.read();
                lineSize = (lineSize << 16) | br.read();
                if (lineSize > AppData.maxData) {
                    throw new IllegalArgumentException();
                }
                data[i] = new char[lineSize];

                //If there is an error in the formatting, returns null to indicate an empty cache.
                if (br.read(data[i], 0 , lineSize) != lineSize) {
                    return null;
                }
            }
            return data;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
