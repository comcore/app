package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.interfaces.cacheable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Cacher {

    //Caches the given data in the modules cache file.
    static boolean cacheData(ArrayList<cacheable> data, Module module) {
        try {
            String filename = module.getMdid() + module.getMnum();
            File cacheFile = new File(Comcore.getContext().getCacheDir(), filename);
            cacheFile.createNewFile();
            PrintWriter pw = new PrintWriter(new FileWriter(cacheFile));

            pw.print(data.size());
            for (cacheable group : data) {
                String line = group.toCache();
                pw.print(line.length());
                pw.print(line);
            }
            pw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static String[] uncacheData(Module module) {
        try {
            String filename = module.getMdid() + module.getMnum();
            File cacheFile = new File(Comcore.getContext().getCacheDir(), filename);
            BufferedReader br = new BufferedReader(new FileReader(cacheFile));
            int size = br.read();

            String[] data = new String[size];
            for (int i = 0; i < size; i++) {
                br.read("Doood", 0, 4);
            }

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
