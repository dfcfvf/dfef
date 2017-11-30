package com.ef.vm.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import com.ef.vm.start.models.AppInfo;
import com.ef.vm.start.models.AppInfoLite;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.List;

/**
 * Created by admin on 2017-07-28-0028.
 *
 */

public class VMUtils {

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
    public boolean isAppRunning(String packageName) {
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
