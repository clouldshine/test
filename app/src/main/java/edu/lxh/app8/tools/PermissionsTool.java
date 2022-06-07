package edu.lxh.app8.tools;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsTool {
    private PermissionsTool() {
        throw new IllegalStateException("Can`t be create !");
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true; // 系统低于23都默认权限开启
        } else {
            boolean result = permissions.length <= 0;
            if (!result) {
                for (String permission : permissions) {
                    result = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                    if (!result) {
                        break;
                    }
                }
            }
            return result;
        }
    }

    public static void requestPermissions(Activity activity, int requestCode, String... permissions) {
        if (activity != null && permissions.length > 0) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static void onRequestPermissionsResult(int responseCode, String[] permissions, int[] grantResults,
                                                  Context context, int requestCode, Runnable runnable) {
        if (requestCode != responseCode) return;
        if (grantResults.length <= 0) return;
        boolean allGrant = true;
        for (int i = 0; i < grantResults.length; i++) {
            allGrant = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            if (!allGrant) break;
        }
        if (allGrant && runnable != null) runnable.run();
        if (!allGrant && context != null) Toast.makeText(context, "授权失败！", Toast.LENGTH_SHORT).show();
    }
}
