package com.nvp.easypermissions;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface PermissionListener {
    void onPermissionGranted();

    void onPermissionDenied(@NonNull ArrayList<String> deniedPermissions);
}
