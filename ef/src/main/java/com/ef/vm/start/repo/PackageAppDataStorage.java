package com.ef.vm.start.repo;

import android.content.Context;

import com.ef.vm.abs.Callback;
import com.ef.vm.abs.ui.VUiKit;
import com.ef.vm.start.models.PackageAppData;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;

import org.jdeferred.DoneCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Lody
 *         <p>
 *         Cache the loaded PackageAppData.
 */
public class PackageAppDataStorage {

    private static final PackageAppDataStorage STORAGE = new PackageAppDataStorage();
    private final Map<String, PackageAppData> packageDataMap = new HashMap<>();

    public static PackageAppDataStorage get() {
        return STORAGE;
    }

    public PackageAppData acquire(Context context, String packageName) {
        PackageAppData data;
        synchronized (packageDataMap) {
            data = packageDataMap.get(packageName);
            if (data == null) {
                data = loadAppData(context, packageName);
            }
        }
        return data;
    }

    public void acquire(final Context context, final String packageName, final Callback<PackageAppData> callback) {
        /*
        VUiKit.defer()
                .when(() -> acquire(context, packageName))
                .done(callback::callback);
        */
        VUiKit.defer().when(new Callable<PackageAppData>() {
            @Override
            public PackageAppData call() throws Exception {
                return acquire(context, packageName);
            }
        }).done(new DoneCallback<PackageAppData>() {
            @Override
            public void onDone(PackageAppData result) {
                callback.callback(result);
            }
        });
    }

    private PackageAppData loadAppData(Context context, String packageName) {
        InstalledAppInfo setting = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (setting != null) {
            PackageAppData data = new PackageAppData(context, setting);
            synchronized (packageDataMap) {
                packageDataMap.put(packageName, data);
            }
            return data;
        }
        return null;
    }

}
