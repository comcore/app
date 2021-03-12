package com.gmail.comcorecrew.comcore.caching;

import android.content.Context;

import com.gmail.comcorecrew.comcore.classes.Helper;
import com.gmail.comcorecrew.comcore.interfaces.Module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Cacher {

    //Caches the given data in the modules cache file.
    public static boolean cacheData(ArrayList<Cacheable> data, Module module, Context context) {
        try {
            //Sets up cache file, creates new file, and sets up print writer.
            File cacheDir = new File(context.getCacheDir(), module.getGroupId());
            if ((!cacheDir.exists()) && (!cacheDir.mkdir())) {
                return false;
            }
            String filename = module.getMdid() + module.getMnum();
            File cacheFile = new File(cacheDir, filename);
            if ((!cacheFile.exists()) && (!cacheDir.createNewFile())) {
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
                if (lineLength > Helper.maxData) {
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

    public static char[][] uncacheData(Module module, Context context) {
        try {
            //Retrieves cache file, opens cache file, and reads the number of cache lines.
            String filename = module.getGroupId() + "/" + module.getMdid() + module.getMnum();
            File cacheFile = new File(context.getCacheDir(), filename);
            BufferedReader br = new BufferedReader(new FileReader(cacheFile));
            int size = br.read();
            size = (size << 16) | br.read();

            //Creates array of chars and reads each line into the array.
            char[][] data = new char[size][];
            for (int i = 0; i < size; i++) {
                int lineSize = br.read();
                lineSize = (lineSize << 16) | br.read();
                if (lineSize > Helper.maxData) {
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
