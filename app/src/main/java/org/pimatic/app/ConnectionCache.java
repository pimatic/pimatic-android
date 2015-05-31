package org.pimatic.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.pimatic.model.ConnectionOptions;
import org.pimatic.model.Device;
import org.pimatic.model.DeviceManager;
import org.pimatic.model.DevicePage;
import org.pimatic.model.DevicePageManager;
import org.pimatic.model.Group;
import org.pimatic.model.GroupManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by h3llfire on 31.05.15.
 */
public class ConnectionCache {

    private static String GLOBAL_SETTINGS = "org.pimatic.global";
    private static String CONNECTION_CACHE_PREFIX = "connection_";
    private static SharedPreferences globalSettings;
    private static Set<String> accountsSet;

    public static void init(Context context, String[] currentAccounts) {
        globalSettings = context.getSharedPreferences(GLOBAL_SETTINGS, context.MODE_PRIVATE);
        Set<String> accountsCaches = globalSettings.getStringSet("accountsCaches", new HashSet<String>());
        accountsSet = new HashSet<String>(Arrays.asList(currentAccounts));
        //cleanup unused caches
        for (String accountName : accountsCaches) {
            if (!accountsSet.contains(accountName)) {
                context.deleteFile(CONNECTION_CACHE_PREFIX + accountName);
            }
        }
        commitAccountSets();
    }

    private static void commitAccountSets() {
        globalSettings.edit().putStringSet("accountsCaches", accountsSet).commit();
    }

    private static String getCacheFilename(Context context, ConnectionOptions conOps) {
        String accountName = conOps.getAccountName();
        if (!accountsSet.contains(accountName)) {
            accountsSet.add(accountName);
            commitAccountSets();
        }
        return CONNECTION_CACHE_PREFIX + accountName;
    }

    public static void saveToCache(Context context, ConnectionOptions conOps) {
        String filename = getCacheFilename(context, conOps);
        CacheHolder holder = new CacheHolder(
                DeviceManager.getDevices(),
                DevicePageManager.getDevicePages(),
                GroupManager.getGroups()
        );
        save(context, holder, filename);
    }

    public static void loadFromCache(Context context, ConnectionOptions conOps) {
        String filename = getCacheFilename(context, conOps);
        CacheHolder holder = load(context, filename);
        Log.v("Cache", "Holder" + holder);
        if (holder != null) {
            DeviceManager.setDevices(holder.device);
            DevicePageManager.setPages(holder.pages);
            GroupManager.setGroups(holder.groups);
        }

    }

    private static boolean save(Context context, CacheHolder obj, String filename) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;

        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
            keep = false;
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
                if (!keep) {
                    context.deleteFile(filename);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return keep;
    }

    public static CacheHolder load(Context context, String filename) {
        CacheHolder result = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = context.openFileInput(filename);
            is = new ObjectInputStream(fis);
            result = (CacheHolder) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (is != null) is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static class CacheHolder implements Serializable {
        public List<Device> device;
        public List<DevicePage> pages;
        public List<Group> groups;

        public CacheHolder(List<Device> device, List<DevicePage> pages, List<Group> groups) {
            this.device = device;
            this.pages = pages;
            this.groups = groups;
        }

    }

}
