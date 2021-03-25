package com.gmail.comcorecrew.comcore;

import android.content.Context;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.drivers.CacheDriver;
import com.gmail.comcorecrew.comcore.drivers.CacheableDriver;
import com.gmail.comcorecrew.comcore.caching.Cacheable;

import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Caching Tests. Makes sure that the cached data is in the correct format.
 *
 * NOTE: uncaching tests work under the assumption that the caching tests work.
 *
 */
public class CachingUnitTest {

    File testDir = new File("src/test/java/com/gmail/comcorecrew/comcore/drivers/expected");
    File cacheDir = new File("src/test/java/com/gmail/comcorecrew/comcore/drivers/cacheDir");

    @Test
    public void cacheSingleLine() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello World!"));
            driver.setData(data);
            driver.toCache();

            File actualFile = new File(AppData.cacheDir, "0/test0");
            File expectedFile = new File(testDir, "test1.txt");

            PrintWriter writer = new PrintWriter(expectedFile);

            writer.print((char) 0);
            writer.print((char) 1);
            writer.print((char) 0);
            writer.print((char) 13);
            writer.print("Hello, World!");

            FileInputStream actualStream = new FileInputStream(actualFile);
            FileInputStream expectedStream = new FileInputStream(expectedFile);
            int expectedByte = expectedStream.read();
            while (expectedByte != -1) {
                assertEquals(expectedByte, actualStream.read());
                expectedByte = expectedStream.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void cacheManyLines() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello, World!"));
            data.add(new CacheableDriver("Hello again, World!"));
            data.add(new CacheableDriver("Hi, World!"));
            data.add(new CacheableDriver("What do you want, World?"));
            driver.setData(data);
            driver.toCache();

            File actualFile = new File(cacheDir, "0/test0");
            File expectedFile = new File(testDir, "test2.txt");

            PrintWriter writer = new PrintWriter(expectedFile);

            writer.print((char) 0);
            writer.print((char) 4);
            writer.print((char) 0);
            writer.print((char) 13);
            writer.print("Hello, World!");
            writer.print((char) 0);
            writer.print((char) 19);
            writer.print("Hello again, World!");
            writer.print((char) 0);
            writer.print((char) 10);
            writer.print("Hi, World!");
            writer.print((char) 0);
            writer.print((char) 24);
            writer.print("What do you want, World?");
            writer.close();

            FileInputStream actualStream = new FileInputStream(actualFile);
            FileInputStream expectedStream = new FileInputStream(expectedFile);
            int expectedByte = expectedStream.read();
            while (expectedByte != -1) {
                assertEquals(expectedByte, actualStream.read());
                expectedByte = expectedStream.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void cacheNullLine() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(""));
            driver.setData(data);
            driver.toCache();

            File actualFile = new File(cacheDir, "0/test0");
            File expectedFile = new File(testDir, "test3.txt");

            PrintWriter writer = new PrintWriter(expectedFile);
            writer.print((char) 0);
            writer.print((char) 1);
            writer.print((char) 0);
            writer.print((char) 0);
            writer.close();

            FileInputStream actualStream = new FileInputStream(actualFile);
            FileInputStream expectedStream = new FileInputStream(expectedFile);
            int expectedByte = expectedStream.read();

            while (expectedByte != -1) {
                assertEquals(expectedByte, actualStream.read());
                expectedByte = expectedStream.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void cacheLargeLine() {
        try {
            AppData.cacheDir = cacheDir;
            StringBuilder lineToCache = new StringBuilder();
            for (int i = 0; i < 0x00010000; i++) {
                lineToCache.append("H");
            }
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(lineToCache.toString()));
            driver.setData(data);
            driver.toCache();

            File actualFile = new File(AppData.cacheDir, "0/test0");
            File expectedFile = new File(testDir, "test4.txt");

            PrintWriter writer = new PrintWriter(expectedFile);

            writer.print((char) 0);
            writer.print((char) 1);
            writer.print((char) 1);
            writer.print((char) 0);
            writer.print(lineToCache);
            writer.close();

            FileInputStream actualStream = new FileInputStream(actualFile);
            FileInputStream expectedStream = new FileInputStream(expectedFile);
            int expectedByte = expectedStream.read();
            while (expectedByte != -1) {
                assertEquals(expectedByte, actualStream.read());
                expectedByte = expectedStream.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void cacheTooLarge() {
        try {
            AppData.cacheDir = cacheDir;
            char[] line = new char[0x01000000];


            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(new String(line)));
            driver.setData(data);
            driver.toCache();
            assert(false);
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }

    @Test
    public void uncacheSingleLine() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello World!"));
            driver.setData(data);
            driver.toCache();

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache();
            ArrayList<Cacheable> actualData = newDriver.getData();
            for (int i = 0; i < data.size(); i++) {
                assertEquals(((CacheableDriver) actualData.get(i)).getData(),
                        ((CacheableDriver) data.get(i)).getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void uncacheManyLines() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello, World!"));
            data.add(new CacheableDriver("Hello again, World!"));
            data.add(new CacheableDriver("Hi, World!"));
            data.add(new CacheableDriver("What do you want, World?"));
            driver.setData(data);
            driver.toCache();

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache();
            ArrayList<Cacheable> actualData = newDriver.getData();
            for (int i = 0; i < data.size(); i++) {
                assertEquals(((CacheableDriver) actualData.get(i)).getData(),
                        ((CacheableDriver) data.get(i)).getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void uncacheNullLine() {
        try {
            AppData.cacheDir = cacheDir;
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(""));
            driver.setData(data);
            driver.toCache();

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache();
            ArrayList<Cacheable> actualData = newDriver.getData();
            for (int i = 0; i < data.size(); i++) {
                assertEquals(((CacheableDriver) actualData.get(i)).getData(),
                        ((CacheableDriver) data.get(i)).getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void uncacheLargeLine() {
        try {
            AppData.cacheDir = cacheDir;
            StringBuilder lineToCache = new StringBuilder();
            for (int i = 0; i < 0x00010000; i++) {
                lineToCache.append("H");
            }
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(lineToCache.toString()));
            driver.setData(data);
            driver.toCache();

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache();
            ArrayList<Cacheable> actualData = newDriver.getData();
            for (int i = 0; i < data.size(); i++) {
                assertEquals(((CacheableDriver) actualData.get(i)).getData(),
                        ((CacheableDriver) data.get(i)).getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }
}
