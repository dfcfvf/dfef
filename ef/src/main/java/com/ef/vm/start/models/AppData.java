package com.ef.vm.start.models;

import android.graphics.drawable.Drawable;

/**
 * @author Lody
 */

public interface AppData {

    boolean isLoading();

    boolean isFirstOpen();

    Drawable getIcon();

    String getName();

    boolean canReorder();

    boolean canLaunch();

    boolean canDelete();

    boolean canCreateShortcut();
}
