package com.md.art.sysmon;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Scanner;


public final class SystemMonitorAPI {
    private static final long MEGABYTE = 1048576L;
    private ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

    private static final int FIRST_SYS_CPU_COLUMN_INDEX = 2;

    private static final int IDLE_SYS_CPU_COLUMN_INDEX = 5;

    private int battery=0;

    public SystemMonitorAPI(Activity activity) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(this.memoryInfo);
        activity.registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

       /* IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = activity.getApplicationContext().registerReceiver(null, filter);
        double battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        double scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        this.battery = (int) (battery / scale * 100);*/
    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
            int icon_small= intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL,0);
            int level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
            boolean present= intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
            int scale= intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
            int status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
            String technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            int temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
            int voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);

            battery=(int)(level*1.0f/scale*100);

        }
    };

    /**
     * Return the first line of /proc/stat or null if failed.
     */
    public String readSystemStat() {

        RandomAccessFile reader = null;
        String load = null;

        try {
            reader = new RandomAccessFile("/proc/stat", "r");
            load = reader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Streams.close(reader);
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return load;
    }

    /**
     * Compute and return the total CPU usage, in percent.
     *
     * @param start first content of /proc/stat. Not null.
     * @param end   second content of /proc/stat. Not null.
     * @return 12.7 for a CPU usage of 12.7% or -1 if the value is not
     * available.
     * @see {@link #readSystemStat()}
     */
    public float getSystemCpuUsage(String start, String end) {
        String[] stat = start.split("\\s");
        long idle1 = getSystemIdleTime(stat);
        long up1 = getSystemUptime(stat);

        stat = end.split("\\s");
        long idle2 = getSystemIdleTime(stat);
        long up2 = getSystemUptime(stat);

        // don't know how it is possible but we should care about zero and
        // negative values.
        float cpu = -1f;
        if (idle1 >= 0 && up1 >= 0 && idle2 >= 0 && up2 >= 0) {
            if ((up2 + idle2) > (up1 + idle1) && up2 >= up1) {
                cpu = (up2 - up1) / (float) ((up2 + idle2) - (up1 + idle1));
                cpu *= 100.0f;
            }
        }

        return cpu;
    }

    /**
     * Return the sum of uptimes read from /proc/stat.
     *
     * @param stat see {@link #readSystemStat()}
     */
    public long getSystemUptime(String[] stat) {
        /*
         * (from man/5/proc) /proc/stat kernel/system statistics. Varies with
         * architecture. Common entries include: cpu 3357 0 4313 1362393
         *
         * The amount of time, measured in units of USER_HZ (1/100ths of a
         * second on most architectures, use sysconf(_SC_CLK_TCK) to obtain the
         * right value), that the system spent in user mode, user mode with low
         * priority (nice), system mode, and the idle task, respectively. The
         * last value should be USER_HZ times the second entry in the uptime
         * pseudo-file.
         *
         * In Linux 2.6 this line includes three additional columns: iowait -
         * time waiting for I/O to complete (since 2.5.41); irq - time servicing
         * interrupts (since 2.6.0-test4); softirq - time servicing softirqs
         * (since 2.6.0-test4).
         *
         * Since Linux 2.6.11, there is an eighth column, steal - stolen time,
         * which is the time spent in other operating systems when running in a
         * virtualized environment
         *
         * Since Linux 2.6.24, there is a ninth column, guest, which is the time
         * spent running a virtual CPU for guest operating systems under the
         * control of the Linux kernel.
         */

        // with the following algorithm, we should cope with all versions and
        // probably new ones.
        long l = 0L;

        for (int i = FIRST_SYS_CPU_COLUMN_INDEX; i < stat.length; i++) {
            if (i != IDLE_SYS_CPU_COLUMN_INDEX) { // bypass any idle mode. There is currently only one.
                try {
                    l += Long.parseLong(stat[i]);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    return -1L;
                }
            }
        }

        return l;
    }

    /**
     * Return the sum of idle times read from /proc/stat.
     *
     * @param stat see {@link #readSystemStat()}
     */
    public long getSystemIdleTime(String[] stat) {
        try {
            return Long.parseLong(stat[IDLE_SYS_CPU_COLUMN_INDEX]);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

        return -1L;
    }

    /**
     * Return the first line of /proc/pid/stat or null if failed.
     */
    public String readProcessStat(int pid) {

        RandomAccessFile reader = null;
        String line = null;

        try {
            reader = new RandomAccessFile("/proc/" + pid + "/stat", "r");
            line = reader.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Streams.close(reader);
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return line;
    }

    /**
     * Compute and return the CPU usage for a process, in percent.
     * <p/>
     * <p>
     * The parameters {@code totalCpuTime} is to be the one for the same period
     * of time delimited by {@code statStart} and {@code statEnd}.
     * </p>
     *
     * @param start  first content of /proc/pid/stat. Not null.
     * @param end    second content of /proc/pid/stat. Not null.
     * @param uptime sum of user and kernel times for the entire system for the
     *               same period of time.
     * @return 12.7 for a cpu usage of 12.7% or -1 if the value is not available
     * or an error occurred.
     * @see {@link #readProcessStat(int)}
     */
    public float getProcessCpuUsage(String start, String end, long uptime) {

        String[] stat = start.split("\\s");
        long up1 = getProcessUptime(stat);

        stat = end.split("\\s");
        long up2 = getProcessUptime(stat);

        float ret = -1f;
        if (up1 >= 0 && up2 >= up1 && uptime > 0.) {
            ret = 100.f * (up2 - up1) / (float) uptime;
        }

        return ret;
    }

    /**
     * Decode the fields of the file {@code /proc/pid/stat} and return (utime +
     * stime)
     *
     * @param stat obtained with {@link #readProcessStat(int)}
     */
    public long getProcessUptime(String[] stat) {
        return Long.parseLong(stat[14]) + Long.parseLong(stat[15]);
    }

    /**
     * Decode the fields of the file {@code /proc/pid/stat} and return (cutime +
     * cstime)
     *
     * @param stat obtained with {@link #readProcessStat(int)}
     */
    public long getProcessIdleTime(String[] stat) {
        return Long.parseLong(stat[16]) + Long.parseLong(stat[17]);
    }

    /**
     * Return the total CPU usage, in percent.
     * <p>
     * The call is blocking for the time specified by elapse.
     * </p>
     *
     * @param elapse the time in milliseconds between reads.
     * @return 12.7 for a CPU usage of 12.7% or -1 if the value is not
     * available.
     */
    public float syncGetSystemCpuUsage(long elapse) {

        String stat1 = readSystemStat();
        if (stat1 == null) {
            return -1.f;
        }

        try {
            Thread.sleep(elapse);
        } catch (Exception e) {
        }

        String stat2 = readSystemStat();
        if (stat2 == null) {
            return -1.f;
        }

        return getSystemCpuUsage(stat1, stat2);
    }

    /**
     * Return the CPU usage of a process, in percent.
     * <p>
     * The call is blocking for the time specified by elapse.
     * </p>
     *
     * @param pid
     * @param elapse the time in milliseconds between reads.
     * @return 6.32 for a CPU usage of 6.32% or -1 if the value is not
     * available.
     */
    public float syncGetProcessCpuUsage(int pid, long elapse) {

        String pidStat1 = readProcessStat(pid);
        String totalStat1 = readSystemStat();
        if (pidStat1 == null || totalStat1 == null) {
            return -1.f;
        }

        try {
            Thread.sleep(elapse);
        } catch (Exception e) {
            e.printStackTrace();
            return -1.f;
        }

        String pidStat2 = readProcessStat(pid);
        String totalStat2 = readSystemStat();
        if (pidStat2 == null || totalStat2 == null) {
            return -1.f;
        }

        String[] toks = totalStat1.split("\\s");
        long cpu1 = getSystemUptime(toks);

        toks = totalStat2.split("\\s");
        long cpu2 = getSystemUptime(toks);

        return getProcessCpuUsage(pidStat1, pidStat2, cpu2 - cpu1);
    }

    public String getOsVersion() {
        return "API " + Build.VERSION.SDK_INT;
    }

    public String getDeviceName() {
        return Build.MODEL;
    }

    public long getTotalMemory() {
        return memoryInfo.totalMem / MEGABYTE;
    }

    public long getFreeMemory() {
        return memoryInfo.availMem / MEGABYTE;
    }

    public int getMemoryLoad() {
        return (int)(((float)(getTotalMemory() - getFreeMemory())/getTotalMemory())*100);
    }

    public int getBattery() {
        return battery;
    }
}