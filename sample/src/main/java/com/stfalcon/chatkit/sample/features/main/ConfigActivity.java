package com.stfalcon.chatkit.sample.features.main;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.nvp.easypermissions.AbstractPermissionListener;
import com.nvp.easypermissions.NvpPermission;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.features.demo.def.DefaultMessagesActivity;
import com.stfalcon.chatkit.sample.utils.SharedPref;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ToanNM on 3/4/2019.
 */
public class ConfigActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String FOLDER_IMAGE = Environment.getExternalStorageDirectory() + "/FakeChat/";

    private FloatingActionButton btnConfig;

    private TextInputEditText inputCarrier, inputBattery, inputNumberOfMember, inputChatName;
    private View viewStatusBarColor, viewChatColor;
    private LinearLayout userLayout;
    private TextView chatTime;

    private int chatSelectedColor, statusSelectedColor;
    private int[] mColors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mColors = getResources().getIntArray(R.array.default_rainbow);

        initViews();
    }

    private void initViews() {
        inputCarrier = findViewById(R.id.input_battery);
        inputCarrier.setHint(getResources().getString(R.string.battery));
        inputCarrier.setEnabled(false);

        inputBattery = findViewById(R.id.input_carrier);
        inputBattery.setHint(getResources().getString(R.string.carrier));
        inputCarrier.setEnabled(false);

        inputChatName = findViewById(R.id.input_chat_name);
        inputChatName.setHint(getResources().getString(R.string.chat_name));
        inputChatName.setText(SharedPref.getString(ConfigActivity.this, SharedPref.KEY_CHAT_NAME));
        saveSharedPreWhileTextChange(inputChatName, SharedPref.KEY_CHAT_NAME);

        chatTime = findViewById(R.id.chat_time);

        inputNumberOfMember = findViewById(R.id.input_number_of_member);
        inputNumberOfMember.setHint(getResources().getString(R.string.chat_number_of_member));
        saveSharedPreWhileTextChange(inputNumberOfMember, SharedPref.KEY_CHAT_NUMBER_OF_MEMBERS);

        userLayout = findViewById(R.id.user_layout);

        viewChatColor = findViewById(R.id.view_chat_color);
        viewStatusBarColor = findViewById(R.id.view_statusbar_color);

        inputNumberOfMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int numberOfUser = Integer.parseInt(inputNumberOfMember.getText().toString());
            }
        });

        int numberOfUser = Integer.parseInt(inputNumberOfMember.getText().toString());
        addUserLayout(numberOfUser);

        viewStatusBarColor.setBackgroundColor(SharedPref.getInt(ConfigActivity.this, SharedPref.KEY_STATUS_COLOR));
        findViewById(R.id.view_statusbar_color_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog dialog = getColorPicker(statusSelectedColor);
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(final int color) {

                        requestPermission(new AbstractPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                super.onPermissionGranted();

                                statusSelectedColor = color;
                                viewStatusBarColor.setBackgroundColor(color);

                                SharedPref.saveInt(ConfigActivity.this, SharedPref.KEY_STATUS_COLOR, color);
                            }
                        });
                    }
                });

                dialog.show(getFragmentManager(), "Choose Color");
            }
        });

        viewChatColor.setBackgroundColor(SharedPref.getInt(ConfigActivity.this, SharedPref.KEY_CHAT_COLOR));
        findViewById(R.id.view_chat_color_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ColorPickerDialog dialog = getColorPicker(chatSelectedColor);
                dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(final int color) {
                        requestPermission(new AbstractPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                super.onPermissionGranted();

                                chatSelectedColor = color;
                                viewChatColor.setBackgroundColor(color);
                                SharedPref.saveInt(ConfigActivity.this, SharedPref.KEY_CHAT_COLOR, color);
                            }
                        });

                    }
                });

                dialog.show(getFragmentManager(), "Choose Color");
            }
        });

        btnConfig = findViewById(R.id.btn_config);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivity.start(ConfigActivity.this);
                DefaultMessagesActivity.open(ConfigActivity.this);
            }
        });

    }

    private void addUserLayout(int numberOfUser) {
        userLayout.removeAllViews();
        final LayoutInflater layoutInflater = LayoutInflater.from(ConfigActivity.this);
        for (int i = 1; i <= numberOfUser; i++) {
            final int pos = i;
            View view = layoutInflater.inflate(R.layout.include_config_chat_user_layout, null, false);

            TextInputEditText inputUserName = view.findViewById(R.id.input_user_name);
            inputUserName.setHint(getResources().getString(R.string.chat_user));
            inputUserName.setHint(getResources().getString(R.string.chat_user) + " " + i);
            inputUserName.setText(SharedPref.getString(ConfigActivity.this, SharedPref.KEY_AVATAR + "_" + pos));

            saveSharedPreWhileTextChange(inputUserName, SharedPref.KEY_AVATAR + "_" + pos);

            final TextView textUserAvatar = view.findViewById(R.id.text_avatar);
            textUserAvatar.setText(getResources().getString(R.string.chat_user_avatar));

            final ImageView imageAvatar = view.findViewById(R.id.image_avatar);
            final String imageFilePath = SharedPref.getString(ConfigActivity.this, SharedPref.KEY_AVATAR_IMAGE + "_" + pos);
            if (imageFilePath != null && !imageFilePath.equals("")) {

                requestPermission(new AbstractPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        super.onPermissionGranted();

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, options);
                        imageAvatar.setImageBitmap(bitmap);
                        textUserAvatar.setVisibility(View.GONE);
                    }
                });

            }

            FrameLayout avatarLayout = view.findViewById(R.id.avatar_layout);
            avatarLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PickImageDialog.build(getPickerImage())
                            .setOnPickResult(new IPickResult() {
                                @Override
                                public void onPickResult(final PickResult r) {

                                    requestPermission(new AbstractPermissionListener() {
                                        @Override
                                        public void onPermissionGranted() {
                                            super.onPermissionGranted();

                                            hasImageFolder();

                                            String fileName = FOLDER_IMAGE + "avatar_" + pos + ".png";
                                            try (FileOutputStream out = new FileOutputStream(fileName)) {
                                                r.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
                                                SharedPref.saveString(ConfigActivity.this,
                                                        SharedPref.KEY_AVATAR_IMAGE + "_" + pos,
                                                        fileName);

                                                textUserAvatar.setVisibility(View.GONE);
                                                if (r.getBitmap() == null)
                                                    Toast.makeText(ConfigActivity.this, getResources().getString(R.string.error_can_not_set_video), Toast.LENGTH_LONG).show();
                                                imageAvatar.setImageBitmap(r.getBitmap());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });

                                }
                            })
                            .setOnPickCancel(new IPickCancel() {
                                @Override
                                public void onCancelClick() {

                                }
                            }).show(getSupportFragmentManager());
                }
            });

            userLayout.addView(view);
        }
    }

    private PickSetup getPickerImage() {
        return new PickSetup()
                .setTitle(getResources().getString(R.string.title_picker_image))
                .setTitleColor(Color.BLACK)
                .setBackgroundColor(Color.WHITE)
                .setProgressText(getResources().getString(R.string.title_picker_image_loading))
                .setProgressTextColor(ContextCompat.getColor(ConfigActivity.this, R.color.colorAccent))
                .setCancelText(getResources().getString(R.string.title_picker_image_cancel))
                .setCancelTextColor(Color.parseColor("#9d9d9d"))
                .setButtonTextColor(ContextCompat.getColor(ConfigActivity.this, R.color.colorAccent))
//                .setDimAmount(50)
                .setFlip(true)
//                .setMaxSize(500)
                .setPickTypes(EPickType.GALLERY, EPickType.CAMERA)
                .setCameraButtonText(getResources().getString(R.string.title_picker_image_camera))
                .setGalleryButtonText(getResources().getString(R.string.title_picker_image_gallery))
                .setIconGravity(Gravity.LEFT)
                .setButtonOrientation(LinearLayout.HORIZONTAL)
                .setSystemDialog(false)
                .setVideo(false);
//                .setGalleryIcon(yourIcon)
//                .setCameraIcon(yourIcon);
    }

    private ColorPickerDialog getColorPicker(int mSelectedColor) {
        ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                mSelectedColor,
                5, // Number of columns
                ColorPickerDialog.SIZE_SMALL,
                true // True or False to enable or disable the serpentine effect
                //0, // stroke width
                //Color.BLACK // stroke color
        );
        return dialog;
    }

    private void requestPermission(final AbstractPermissionListener permissionListener) {
        NvpPermission.with(this)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(permissionListener)
                .check();
    }

    private boolean hasImageFolder() {
        File file = new File(FOLDER_IMAGE);
        if (!file.exists())
            return new File(FOLDER_IMAGE).mkdirs();
        else return true;

    }

    private void saveSharedPreWhileTextChange(TextInputEditText input, final String key) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPref.saveString(ConfigActivity.this, key, editable.toString());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat_time:
                new SingleDateAndTimePickerDialog.Builder(this)
                        .bottomSheet()
                        .curved()
                        .displayMinutes(true)
                        .displayHours(true)
                        .displayDays(false)
                        .displayMonth(true)
                        .displayYears(true)
                        .displayDaysOfMonth(true)
                        .listener(new SingleDateAndTimePickerDialog.Listener() {
                            @Override
                            public void onDateSelected(Date date) {
                                chatTime.setText(date.toString());
                            }
                        })
                        .display();
                break;
        }
    }
}

