package com.ef.vm.start;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ef.lody.virtual.R;
import com.ef.vm.abs.ui.VActivity;
import com.ef.vm.abs.ui.VUiKit;
import com.ef.vm.start.models.PackageAppData;
import com.ef.vm.start.repo.PackageAppDataStorage;
import com.ef.vm.widgets.EatBeansView;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;


/**
 * @author Lody
 */

public class LoadingActivity extends VActivity {

    private static final String PKG_NAME_ARGUMENT = "MODEL_ARGUMENT";
    private static final String KEY_INTENT = "KEY_INTENT";
    private static final String KEY_USER = "KEY_USER";
    private PackageAppData appModel;
    private EatBeansView loadingView;
    private View viewload;
    private AnimationDrawable frameAnim;

    public static void launch(Context context, String packageName, int userId) {
        Intent intent = VirtualCore.get().getLaunchIntent(packageName, userId);
        if (intent != null) {
            Intent loadingPageIntent = new Intent(context, LoadingActivity.class);
            loadingPageIntent.putExtra(PKG_NAME_ARGUMENT, packageName);
            //loadingPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            loadingPageIntent.putExtra(KEY_INTENT, intent);
            loadingPageIntent.putExtra(KEY_USER, userId);
            context.startActivity(loadingPageIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        loadingView = (EatBeansView) findViewById(R.id.loading_anim);
        viewload = (View) findViewById(R.id.load_game_view);
        // 通过逐帧动画的资源文件获得AnimationDrawable示例
        frameAnim=(AnimationDrawable) getResources().getDrawable(R.drawable.loadinganim);
        // 把AnimationDrawable设置为ImageView的背景
        viewload.setBackgroundDrawable(frameAnim);
        final int userId = getIntent().getIntExtra(KEY_USER, -1);
        String pkg = getIntent().getStringExtra(PKG_NAME_ARGUMENT);
        appModel = PackageAppDataStorage.get().acquire(getApplicationContext(), pkg);
        ImageView iconView = (ImageView) findViewById(R.id.app_icon);
        iconView.setImageDrawable(appModel.icon);
        TextView nameView = (TextView) findViewById(R.id.app_name);
        //nameView.setText(String.format(Locale.ENGLISH, R.string.is_qidong+" %s...", appModel.name));
        nameView.setText("正在启动..."+appModel.name);
        final Intent intent = getIntent().getParcelableExtra(KEY_INTENT);
        if (intent == null) {
            return;
        }
        VirtualCore.get().setUiCallback(intent, mUiCallback);
        VUiKit.defer().when(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                if (!appModel.fastOpen) {
                    try {
                        VirtualCore.get().preOpt(appModel.packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                long spend = System.currentTimeMillis() - startTime;
                if (spend < 500) {
                    try {
                        Thread.sleep(500 - spend);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                VActivityManager.get().startActivity(intent, userId);
            }
        });
        /*
        VUiKit.defer().when(() -> {
            long startTime = System.currentTimeMillis();
            if (!appModel.fastOpen) {
                try {
                    VirtualCore.get().preOpt(appModel.packageName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            long spend = System.currentTimeMillis() - startTime;
            if (spend < 500) {
                try {
                    Thread.sleep(500 - spend);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            VActivityManager.get().startActivity(intent, userId);
        });
        */
    }

    private final VirtualCore.UiCallback mUiCallback = new VirtualCore.UiCallback() {

        @Override
        public void onAppOpened(String packageName, int userId) throws RemoteException {
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //loadingView.startAnim();
        frameAnim.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //loadingView.stopAnim();
        frameAnim.stop();
    }
}
