package com.nvp.easypermissions;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class NvpPermissionActivity extends AppCompatActivity {
    public static final String EXTRA_PERMISSIONS = "permissions";
    public static final String EXTRA_RATIONALE_TITLE = "rationale_title";
    public static final String EXTRA_RATIONALE_MESSAGE = "rationale_message";
    public static final String EXTRA_DENY_TITLE = "deny_title";
    public static final String EXTRA_DENY_MESSAGE = "deny_message";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_SETTING_BUTTON = "setting_button";
    public static final String EXTRA_SETTING_BUTTON_TEXT = "setting_button_text";
    public static final String EXTRA_RATIONALE_CONFIRM_TEXT = "rationale_confirm_text";
    public static final String EXTRA_DENIED_DIALOG_CLOSE_TEXT = "denied_dialog_close_text";

    public static final int REQ_CODE_PERMISSION_REQUEST = 10;
    public static final int REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST = 30;
    public static final int REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING = 31;

    private static Deque<PermissionListener> permissionListenerStack;

    CharSequence rationaleTitle;
    CharSequence rationale_message;
    CharSequence denyTitle;
    CharSequence denyMessage;
    String[] permissions;
    String packageName;
    boolean hasSettingButton;
    String settingButtonText;
    String deniedCloseButtonText;
    String rationaleConfirmText;
    boolean isShownRationaleDialog;

    public static void startActivity(Context context, Intent intent, PermissionListener listener) {
        if (permissionListenerStack == null) {
            permissionListenerStack = new ArrayDeque<>();
        }
        permissionListenerStack.push(listener);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setupFromSavedInstanceState(savedInstanceState);
        if (needWindowPermission()) {
            requestWindowPermission();
        } else {
            checkPermissions(false);
        }
    }

    private void setupFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            rationaleTitle = savedInstanceState.getCharSequence(EXTRA_RATIONALE_TITLE);
            rationale_message = savedInstanceState.getCharSequence(EXTRA_RATIONALE_MESSAGE);
            denyTitle = savedInstanceState.getCharSequence(EXTRA_DENY_TITLE);
            denyMessage = savedInstanceState.getCharSequence(EXTRA_DENY_MESSAGE);
            packageName = savedInstanceState.getString(EXTRA_PACKAGE_NAME);

            hasSettingButton = savedInstanceState.getBoolean(EXTRA_SETTING_BUTTON, true);

            rationaleConfirmText = savedInstanceState.getString(EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = savedInstanceState.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT);

            settingButtonText = savedInstanceState.getString(EXTRA_SETTING_BUTTON_TEXT);
        } else {
            Intent intent = getIntent();
            permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
            rationaleTitle = intent.getCharSequenceExtra(EXTRA_RATIONALE_TITLE);
            rationale_message = intent.getCharSequenceExtra(EXTRA_RATIONALE_MESSAGE);
            denyTitle = intent.getCharSequenceExtra(EXTRA_DENY_TITLE);
            denyMessage = intent.getCharSequenceExtra(EXTRA_DENY_MESSAGE);
            packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            hasSettingButton = intent.getBooleanExtra(EXTRA_SETTING_BUTTON, true);
            rationaleConfirmText = intent.getStringExtra(EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = intent.getStringExtra(EXTRA_DENIED_DIALOG_CLOSE_TEXT);
            settingButtonText = intent.getStringExtra(EXTRA_SETTING_BUTTON_TEXT);
        }
    }

    private boolean needWindowPermission() {
        for (String permission : permissions) {
            if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                return !hasWindowPermission();
            }
        }
        return false;
    }

    @TargetApi(VERSION_CODES.M)
    private boolean hasWindowPermission() {
        return Settings.canDrawOverlays(getApplicationContext());
    }

    @TargetApi(VERSION_CODES.M)
    private void requestWindowPermission() {
        Uri uri = Uri.fromParts("package", packageName, null);
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);

        if (!TextUtils.isEmpty(rationale_message)) {
            new AlertDialog.Builder(this)
                    .setMessage(rationale_message)
                    .setCancelable(false)
                    .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
                        }
                    })
                    .show()
                    .getWindow()
                    .setBackgroundDrawableResource(R.drawable.dialog_bg);
            isShownRationaleDialog = true;
        } else {
            startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
        }
    }

    private void checkPermissions(boolean fromOnActivityResult) {
        ArrayList<String> needPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                if (!hasWindowPermission()) {
                    needPermissions.add(permission);
                }
            } else {
                if (NvpPermission.isDenied(this, permission)) {
                    needPermissions.add(permission);
                }
            }
        }

        if (needPermissions.isEmpty()) {
            permissionResult(null);
        } else if (fromOnActivityResult) {
            permissionResult(needPermissions);
        } else if (needPermissions.size() == 1 && needPermissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            permissionResult(needPermissions);
        } else if (!isShownRationaleDialog && !TextUtils.isEmpty(rationale_message)) {
            showRationaleDialog(needPermissions);
        } else {
            requestPermissions(needPermissions);
        }
    }

    private void permissionResult(ArrayList<String> deniedPermissions) {
        if (permissionListenerStack != null) {
            PermissionListener listener = permissionListenerStack.pop();

            if (deniedPermissions == null || deniedPermissions.isEmpty()) {
                listener.onPermissionGranted();
            } else {
                listener.onPermissionDenied(deniedPermissions);
            }
            if (permissionListenerStack.size() == 0) {
                permissionListenerStack = null;
            }
        }

        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void showRationaleDialog(final ArrayList<String> needPermissions) {
        new AlertDialog.Builder(this)
                .setTitle(rationaleTitle)
                .setMessage(rationale_message)
                .setCancelable(false)
                .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(needPermissions);
                    }
                })
                .show()
                .getWindow()
                .setBackgroundDrawableResource(R.drawable.dialog_bg);
        isShownRationaleDialog = true;
    }

    public void requestPermissions(ArrayList<String> needPermissions) {
        ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(EXTRA_PERMISSIONS, permissions);
        outState.putCharSequence(EXTRA_RATIONALE_TITLE, rationaleTitle);
        outState.putCharSequence(EXTRA_RATIONALE_MESSAGE, rationale_message);
        outState.putCharSequence(EXTRA_DENY_TITLE, denyTitle);
        outState.putCharSequence(EXTRA_DENY_MESSAGE, denyMessage);
        outState.putString(EXTRA_PACKAGE_NAME, packageName);
        outState.putBoolean(EXTRA_SETTING_BUTTON, hasSettingButton);
        outState.putString(EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText);
        outState.putString(EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);
        outState.putString(EXTRA_SETTING_BUTTON_TEXT, settingButtonText);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> deniedPermissions = NvpPermission.getDeniedPermissions(this, permissions);

        if (deniedPermissions.isEmpty()) {
            permissionResult(null);
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }

    public void showPermissionDenyDialog(final ArrayList<String> deniedPermissions) {
        if (TextUtils.isEmpty(denyMessage)) {
            permissionResult(deniedPermissions);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(denyTitle)
                .setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionResult(deniedPermissions);
                    }
                });

        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.setting);
            }

            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NvpPermission.startSettingActivityForResult(NvpPermissionActivity.this);

                }
            });
        }

        builder.show()
                .getWindow()
                .setBackgroundDrawableResource(R.drawable.dialog_bg);
    }

    public boolean shouldShowRequestPermissionRationale(ArrayList<String> needPermissions) {
        if (needPermissions == null) {
            return false;
        }

        for (String permission : needPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(NvpPermissionActivity.this, permission)) {
                return false;
            }
        }

        return true;
    }

    public void showWindowPermissionDenyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermissions(false);
                    }
                });

        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.setting);
            }

            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @TargetApi(VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.fromParts("package", packageName, null);
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                    startActivityForResult(intent, REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING);
                }
            });

        }
        builder.show()
                .getWindow()
                .setBackgroundDrawableResource(R.drawable.dialog_bg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NvpPermission.REQ_CODE_REQUEST_SETTING:
                checkPermissions(true);
                break;
            case REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST:
                if (!hasWindowPermission() && !TextUtils.isEmpty(denyMessage)) {
                    showWindowPermissionDenyDialog();
                } else {
                    checkPermissions(false);
                }
                break;
            case REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING:
                checkPermissions(false);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
