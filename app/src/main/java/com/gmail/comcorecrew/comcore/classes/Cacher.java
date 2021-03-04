package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.interfaces.cacheable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Cacher {

    //Caches the given data in the modules cache file.
    static boolean cacheData(ArrayList<cacheable> data, Module module, Context context) {
        try {
            //Sets up cache file, creates new file, and sets up print writer.
            String filename = module.getGroupId() + '/' + module.getMdid() + module.getMnum();
            File cacheFile = new File(context.getCacheDir(), filename);
            cacheFile.createNewFile();
            PrintWriter pw = new PrintWriter(new FileWriter(cacheFile));

            //Adds number of lines to file, and for each cacheable, adds the length
            //of the data to the file and then the string of chars.
            pw.print(data.size());
            for (cacheable group : data) {
                char[] line = group.toCache();
                pw.print(line.length);
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

    static char[][] uncacheData(Module module, Context context) {
        try {
            //Retrieves cache file, opens cache file, and reads the number of cache lines.
            String filename = module.getGroupId() + '/' + module.getMdid() + module.getMnum();
            File cacheFile = new File(context.getCacheDir(), filename);
            BufferedReader br = new BufferedReader(new FileReader(cacheFile));
            int size = br.read();

            //Creates array of chars and reads each line into the array.
            char[][] data = new char[size][];
            for (int i = 0; i < size; i++) {
                int lineSize = br.read();
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

    static private String toCharString(int num) {
        return "" + (char) (num>>16) + (char) num;
    }

    static private String toCharString(long num) {
        return "" + (char) (num>>48) + (char) (num>>32) + (char) (num>>16) + (char) num;
    }

    static private int toInt(String s) {
        if (s.length() == 0) {
            return 0;
        }
        int val = 0;
        int chars = Math.min(s.length(), 2);
        for (int i = 0; i < chars; i++) {
            val = val << 16;
            val = val | s.charAt(i);
        }
        return val;
    }

    static private long toLong(String s) {
        if (s.length() == 0) {
            return 0;
        }
        long val = 0;
        int chars = Math.min(s.length(), 4);
        for (int i = 0; i < chars; i++) {
            val = val << 16;
            val = val | s.charAt(i);
        }
        return val;
    }
}
