package com.gmail.comcorecrew.comcore.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import com.gmail.comcorecrew.comcore.server.entry.ModuleEntry;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.gmail.comcorecrew.comcore.server.id.ModuleItemID;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for scheduling notifications for future events.
 */
public final class NotificationScheduler {
    /**
     * The amount of time between a notification and the deadline.
     */
    public static final long REMINDER_TIME = 24 * 60 * 60 * 1000;

    private static final HashMap<String, ScheduledNotification> schedule = new HashMap<>();
    private static WeakReference<Context> contextWeakReference;
    private static AlarmManager alarmManager;
    private static File scheduleFile;
    private static boolean dirty = false;

    private NotificationScheduler() {}

    /**
     * Initialize the NotificationScheduler with a specific context.
     *
     * @param context the context
     */
    public static void init(Context context) {
        contextWeakReference = new WeakReference<>(context);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        scheduleFile = new File(context.getFilesDir(), "notificationSchedule");
        read();
    }

    /**
     * Reset all scheduled alarms with the operating system.
     */
    public static synchronized void resetAllAlarms() {
        for (Map.Entry<String, ScheduledNotification> entry : schedule.entrySet()) {
            setAlarm(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Schedule a notification for an entry.
     *
     * @param entry the entry to schedule a notification for
     */
    public static void add(ModuleEntry<?, ?> entry) {
        ScheduledNotification notification = entry.getScheduledNotification();
        if (notification == null) {
            return;
        }

        addByKey(keyFor(entry.id), notification);
    }

    /**
     * Cancel the notification for an item.
     *
     * @param item the item to cancel the notification for
     */
    public static void remove(ModuleItemID<?> item) {
        removeByKey(keyFor(item));
    }

    /**
     * Returns a unique key for an item.
     *
     * @param item the item to create a key for
     * @return the unique key for the item
     */
    public static String keyFor(ModuleItemID<?> item) {
        ModuleID module = item.module;
        return module.group.id + "-" + module.getType() + "-" + module.id + "-" + item.id;
    }

    /**
     * Schedule a notification with the given key.
     *
     * @param key          the key for the notification
     * @param notification the notification to schedule
     */
    public static synchronized void addByKey(String key, ScheduledNotification notification) {
        if (notification == null) {
            removeByKey(key);
            return;
        }

        ScheduledNotification oldNotification = schedule.put(key, notification);
        if (notification.equals(oldNotification)) {
            return;
        }

        if (oldNotification != null) {
            cancelAlarm(key, oldNotification);
        }

        setAlarm(key, notification);
        dirty = true;
    }

    /**
     * Cancel the notification with the given key.
     *
     * @param key the key to cancel the notification for
     */
    public static synchronized void removeByKey(String key) {
        ScheduledNotification notification = schedule.remove(key);
        if (notification != null) {
            cancelAlarm(key, notification);
            dirty = true;
        }
    }

    /**
     * Store the loaded data if necessary.
     */
    public static void store() {
        if (dirty) {
            write();
            dirty = false;
        }
    }

    /**
     * Register an alarm with the operating system.
     *
     * @param key          the key for the notification
     * @param notification the notification to schedule
     */
    private static void setAlarm(String key, ScheduledNotification notification) {
        Context context = contextWeakReference.get();
        if (context == null) {
            return;
        }

        System.err.printf("setAlarm(%s)\n", key);
        PendingIntent intent = notification.getPendingIntent(context, key);
        alarmManager.setExact(AlarmManager.RTC, notification.timestamp, intent);
    }

    /**
     * Cancel an alarm with the operating system.
     *
     * @param key          the key for the notification
     * @param notification the notification to cancel
     */
    private static void cancelAlarm(String key, ScheduledNotification notification) {
        Context context = contextWeakReference.get();
        if (context == null) {
            return;
        }

        System.err.printf("cancelAlarm(%s)\n", key);
        PendingIntent intent = notification.getPendingIntent(context, key);
        alarmManager.cancel(intent);
    }

    /**
     * Load all previously scheduled events.
     */
    private static void read() {
        if (dirty) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(scheduleFile))) {
            String line;
            long time = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                ScheduledNotification notification = ScheduledNotification.fromJson(json);
                if (notification.timestamp < time) {
                    dirty = true;
                    continue;
                }

                String key = json.get("key").getAsString();
                schedule.put(key, notification);
            }
        } catch (FileNotFoundException ignored) {
            // There is no existing schedule
        } catch (Exception e) {
            e.printStackTrace();
        }

        store();
    }

    /**
     * Store all scheduled events.
     */
    private static void write() {
        try (PrintWriter writer = new PrintWriter(scheduleFile, "UTF-8")) {
            long time = System.currentTimeMillis();
            for (Map.Entry<String, ScheduledNotification> entry : schedule.entrySet()) {
                ScheduledNotification notification = entry.getValue();
                if (notification.timestamp < time) {
                    continue;
                }

                JsonObject json = notification.toJson();
                json.addProperty("key", entry.getKey());
                writer.println(json.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}