package com.ef.vm.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ef.vm.delegate.MyAppRequestListener;
import com.ef.vm.delegate.MyComponentDelegate;
import com.ef.vm.delegate.MyPhoneInfoDelegate;
import com.ef.vm.delegate.MyTaskDescriptionDelegate;
import com.ef.vm.start.models.AppInfo;
import com.ef.vm.start.models.AppInfoLite;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;


import jonathanfinerty.once.Once;

/**
 * Created by admin on 2017-07-28-0028.
 *
 */

public class VMUtils {

    /**
     * 配置 VASettings
     * @param redirect
     * @param shorecut
     */
    public static void setConfig(boolean redirect, boolean shorecut){
        VASettings.ENABLE_IO_REDIRECT = redirect;
        VASettings.ENABLE_INNER_SHORTCUT = shorecut;
    }

    /**
     * VA startup
     * @param context
     */
    public static void startup(Context context){
        try {
            VirtualCore.get().startup(context);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * VA init
     * @param context
     */
    public static void initVirtual(final Context context) {
        final VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {
            @Override
            public void onMainProcess() {
                Once.initialise(context);
            }

            @Override
            public void onVirtualProcess() {
                virtualCore.setComponentDelegate(new MyComponentDelegate());
                //fake phone imei,macAddress,BluetoothAddress
                virtualCore.setPhoneInfoDelegate(new MyPhoneInfoDelegate());
                //fake task description's icon and title
                virtualCore.setTaskDescriptionDelegate(new MyTaskDescriptionDelegate());
            }

            @Override
            public void onServerProcess() {
                virtualCore.setAppRequestListener(new MyAppRequestListener(context));
            }
        });
    }

    /**
     * 判断是否安装
     * @param packageName
     * @return
     */
    public static boolean isAppInstalledAsUser(String packageName) {
        try {
            return VirtualCore.get().isAppInstalledAsUser(0, packageName);
        } catch (Exception e) {
            VirtualCore.get().closeServer();
            uninstallApp(packageName);
            return false;
        }
    }

    /**
     * 判断是否运行
     * @param packageName
     * @return
     */
    public static boolean isAppRunning(String packageName) {
        try{
            return VirtualCore.get().isAppRunning(packageName, 0);
        }catch (Exception e){
            VActivityManager.get().closeServer();
            return true;
        }
    }

    /**
     * 卸载App
     * @param packageName
     */
    public static boolean uninstallApp(String packageName){
        VirtualCore core = VirtualCore.get();
        try{
            return core.uninstallPackage(packageName);
        }catch (Exception e){
            VirtualCore.get().closeServer();
           return false;
        }
    }

    /**
     * 安装app
     */
    public static boolean installapp(String pkg) {
        VirtualCore core = VirtualCore.get();
        if (isAppInstalledAsUser(pkg)) {
            return true;
        }
        ApplicationInfo info = null;
        try {
            info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(pkg, 0);
            if (info == null || info.sourceDir == null) {
                return false;
            }
            InstallResult result = core.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
            return result.isSuccess;
        } catch (Exception e) {
            VirtualCore.get().closeServer();
            return false;
        }
    }

    @Deprecated
    public static AppInfoLite getAppInfoLiteByPkg(Context context, String packagename){
        AppInfoLite infolite = null;
        try {
            PackageInfo pkg = context.getPackageManager().getPackageInfo(packagename, 0);
            ApplicationInfo ai = pkg.applicationInfo;
            PackageManager pm = context.getPackageManager();
            String path = ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
            AppInfo info = new AppInfo();
            info.packageName = pkg.packageName;
            info.fastOpen = true;
            info.path = path;
            info.icon = ai.loadIcon(pm);
            info.name = ai.loadLabel(pm);
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(pkg.packageName, 0);
            if (installedAppInfo != null) {
                info.cloneCount = installedAppInfo.getInstalledUsers().length;
            }
            infolite = new AppInfoLite(info.packageName, info.path, info.fastOpen);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return infolite;
    }
}
