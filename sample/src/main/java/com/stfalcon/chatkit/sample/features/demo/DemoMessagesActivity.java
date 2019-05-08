package com.stfalcon.chatkit.sample.features.demo;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nvp.easypermissions.AbstractPermissionListener;
import com.nvp.easypermissions.NvpPermission;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.Message;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.utils.AppUtils;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.enums.EPickType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Created by troy379 on 04.04.17.
 */
public abstract class DemoMessagesActivity extends AppCompatActivity
        implements MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener {

    public static final String FOLDER_IMAGE = Environment.getExternalStorageDirectory() + "/FakeChat/";

    private static final int TOTAL_MESSAGES_COUNT = 100;

    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;

    private Menu menu;
    private int selectionCount;
    private Date lastLoadedDate;

    public void showDateTimePicker() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.with(DemoMessagesActivity.this).load(url).into(imageView);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
//        messagesAdapter.addToStart(MessagesFixtures.getTextMessage(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_datetime:
                showDateTimePicker();
                break;
            case R.id.action_delete:
                messagesAdapter.deleteSelectedMessages();
                break;
            case R.id.action_copy:
                messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
                AppUtils.showToast(this, R.string.copied_message, true);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.i("TAG", "onLoadMore: " + page + " " + totalItemsCount);
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            loadMessages();
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_change_datetime).setVisible(count > 0);
        menu.findItem(R.id.action_delete).setVisible(count > 0);
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    protected void loadMessages() {
//        new Handler().postDelayed(new Runnable() { //imitation of internet connection
//            @Override
//            public void run() {
//                ArrayList<Message> messages = MessagesFixtures.getMessages(lastLoadedDate);
//                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
//                messagesAdapter.addToEnd(messages, false);
//            }
//        }, 1000);
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public PickSetup getPickerImage() {
        return new PickSetup()
                .setTitle(getResources().getString(R.string.title_picker_image))
                .setTitleColor(Color.BLACK)
                .setBackgroundColor(Color.WHITE)
                .setProgressText(getResources().getString(R.string.title_picker_image_loading))
                .setProgressTextColor(ContextCompat.getColor(DemoMessagesActivity.this, R.color.colorAccent))
                .setCancelText(getResources().getString(R.string.title_picker_image_cancel))
                .setCancelTextColor(Color.parseColor("#9d9d9d"))
                .setButtonTextColor(ContextCompat.getColor(DemoMessagesActivity.this, R.color.colorAccent))
//                .setDimAmount(50)
                .setFlip(true)
//                .setMaxSize(500)
                .setPickTypes(EPickType.GALLERY, EPickType.CAMERA)
                .setCameraButtonText(getResources().getString(R.string.title_picker_image_camera))
                .setGalleryButtonText(getResources().getString(R.string.title_picker_image_gallery))
                .setIconGravity(Gravity.LEFT)
                .setButtonOrientation(LinearLayout.HORIZONTAL)
                .setSystemDialog(false)
                .setVideo(true);
//                .setGalleryIcon(yourIcon)
//                .setCameraIcon(yourIcon);
    }

    public void requestPermission(final AbstractPermissionListener permissionListener) {
        NvpPermission.with(this)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(permissionListener)
                .check();
    }

    public boolean hasImageFolder() {
        File file = new File(FOLDER_IMAGE);
        if (!file.exists())
            return new File(FOLDER_IMAGE).mkdirs();
        else return true;

    }
}
