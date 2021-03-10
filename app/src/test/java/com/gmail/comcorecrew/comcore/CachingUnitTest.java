package com.gmail.comcorecrew.comcore;

import android.content.Context;

import com.gmail.comcorecrew.comcore.drivers.CacheDriver;
import com.gmail.comcorecrew.comcore.drivers.CacheableDriver;
import com.gmail.comcorecrew.comcore.caching.Cacheable;

import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

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
@RunWith(MockitoJUnitRunner.class)
public class CachingUnitTest {
    @Mock
    Context context;

    File testDir = new File("src/test/java/com/gmail/comcorecrew/comcore/drivers/expected");

    @Test
    public void cacheSingleLine() {
        try {
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello World!"));
            driver.setData(data);
            driver.toCache(context);

            File actualFile = new File(context.getCacheDir(), "0/test0");
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
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello, World!"));
            data.add(new CacheableDriver("Hello again, World!"));
            data.add(new CacheableDriver("Hi, World!"));
            data.add(new CacheableDriver("What do you want, World?"));
            driver.setData(data);
            driver.toCache(context);

            File actualFile = new File(context.getCacheDir(), "0/test0");
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
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(""));
            driver.setData(data);
            driver.toCache(context);

            File actualFile = new File(context.getCacheDir(), "0/test0");
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
            StringBuilder lineToCache = new StringBuilder();
            for (int i = 0; i < 0x00010000; i++) {
                lineToCache.append("H");
            }
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(lineToCache.toString()));
            driver.setData(data);
            driver.toCache(context);

            File actualFile = new File(context.getCacheDir(), "0/test0");
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
            char[] line = new char[0x01000000];


            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(new String(line)));
            driver.setData(data);
            driver.toCache(context);
            assert(false);
        } catch (IllegalArgumentException e) {
            assert true;
        }
    }

    @Test
    public void uncacheSingleLine() {
        try {
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello World!"));
            driver.setData(data);
            driver.toCache(context);

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache(context);
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
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver("Hello, World!"));
            data.add(new CacheableDriver("Hello again, World!"));
            data.add(new CacheableDriver("Hi, World!"));
            data.add(new CacheableDriver("What do you want, World?"));
            driver.setData(data);
            driver.toCache(context);

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache(context);
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
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(""));
            driver.setData(data);
            driver.toCache(context);

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache(context);
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
            StringBuilder lineToCache = new StringBuilder();
            for (int i = 0; i < 0x00010000; i++) {
                lineToCache.append("H");
            }
            CacheDriver driver = new CacheDriver();
            ArrayList<Cacheable> data = new ArrayList<>();
            data.add(new CacheableDriver(lineToCache.toString()));
            driver.setData(data);
            driver.toCache(context);

            CacheDriver newDriver = new CacheDriver();
            newDriver.fromCache(context);
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
