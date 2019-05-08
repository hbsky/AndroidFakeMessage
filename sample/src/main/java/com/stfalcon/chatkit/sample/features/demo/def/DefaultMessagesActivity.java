package com.stfalcon.chatkit.sample.features.demo.def;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.nvp.easypermissions.AbstractPermissionListener;
import com.stfalcon.chatkit.commons.models.Message;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures;
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity;
import com.stfalcon.chatkit.utils.SharedPref;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class DefaultMessagesActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener,
        MessagesListAdapter.OnMessageLongClickListener<Message> {

    public static void open(Context context) {
        context.startActivity(new Intent(context, DefaultMessagesActivity.class));
    }

    private MessagesList messagesList;

    private Message lastOnClickMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setNavigationBarColor(SharedPref.getInt(DefaultMessagesActivity.this, SharedPref.KEY_STATUS_COLOR));
            getWindow().setStatusBarColor(SharedPref.getInt(DefaultMessagesActivity.this, SharedPref.KEY_STATUS_COLOR));
        }

        getSupportActionBar().setBackgroundDrawable(
                new ColorDrawable(SharedPref.getInt(DefaultMessagesActivity.this, SharedPref.KEY_STATUS_COLOR)));

        getSupportActionBar().setTitle(SharedPref.getString(DefaultMessagesActivity.this, SharedPref.KEY_CHAT_NAME));

        setContentView(R.layout.activity_default_messages);

        this.messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);
        input.setTypingListener(this);
        input.setAttachmentsListener(this);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString(),
                        new Date(SharedPref.getLong(DefaultMessagesActivity.this, SharedPref.KEY_DEFAULT_CHAT_TIME,
                                new Date().getTime()))), true
        );
        Log.e("Debug", "onSubmit -> " + input.toString());
        return true;
    }

    @Override
    public void onAddAttachments() {
        PickImageDialog.build(getPickerImage())
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(final PickResult r) {
                        requestPermission(new AbstractPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                super.onPermissionGranted();

                                hasImageFolder();

                                String fileName = FOLDER_IMAGE + "avatar_" + ".png";
                                try (FileOutputStream out = new FileOutputStream(fileName)) {
                                    r.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);

                                    Message message = MessagesFixtures.getImageMessageBitmap(fileName);

                                    messagesAdapter.addToStart(message, true);
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

    private void initAdapter() {
        final String senderId = super.senderId;
        super.messagesAdapter = new MessagesListAdapter<>(this, super.senderId, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
//        super.messagesAdapter.registerViewClickListener(R.id.mainLayout,
//                new MessagesListAdapter.OnMessageViewClickListener<Message>() {
//                    @Override
//                    public void onMessageViewClick(View view, Message message) {
//                        if (message.getUser().getId().equals(senderId)) {
//                            message.getUser().setUserId("__" + senderId);
//                        }else
//                            message.getUser().setUserId(senderId);
//
//                        messagesAdapter.notifyDataSetChanged();
//                    }
//                });

        messagesAdapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {
                if (message.getUser().getId().equals(senderId)) {
                    message.getUser().setUserId("__" + senderId);
                } else
                    message.getUser().setUserId(senderId);

                messagesAdapter.notifyDataSetChanged();
            }
        });
        messagesAdapter.customOnMessageLongClickListener = new MessagesListAdapter.CustomOnMessageLongClickListener() {
            @Override
            public void onMessageLongClickListener(Message message) {
                lastOnClickMessage = message;
            }
        };
//        messagesAdapter.setOnMessageLongClickListener(this);
        this.messagesList.setAdapter(super.messagesAdapter);
    }

    @Override
    public void onStartTyping() {
        Log.v("Typing listener", getString(R.string.start_typing_status));
    }

    @Override
    public void onStopTyping() {
        Log.v("Typing listener", getString(R.string.stop_typing_status));
    }


    @Override
    public void onMessageLongClick(Message message) {
        Toast.makeText(this, "TEST", Toast.LENGTH_SHORT).show();
    }

    public void showDateTimePicker() {
        new SingleDateAndTimePickerDialog.Builder(this)
//                .bottomSheet()
                .curved()
                .mainColor(SharedPref.getInt(this, SharedPref.KEY_CHAT_COLOR))
                .title(getResources().getString(R.string.chat_datetime))
                .titleTextColor(Color.WHITE)
                .displayMinutes(true)
                .displayHours(true)
                .displayDays(false)
                .displayMonth(true)
                .displayYears(true)
                .displayDaysOfMonth(true)
                .listener(new SingleDateAndTimePickerDialog.Listener() {
                    @Override
                    public void onDateSelected(Date date) {
                        setCreateAt(date);
                    }
                })
                .display();
    }

    private void setCreateAt(Date date) {
        for (int i = 0; i < messagesAdapter.items.size(); i++) {
            MessagesListAdapter.Wrapper item = messagesAdapter.items.get(i);

            if (item.item instanceof Message) {
                Message mess = (Message) item.item;

                if (mess.equals(lastOnClickMessage)) {
                    int selectedPos = i;
                    Log.e("ToanNM", "selectedPos -> " + selectedPos);
                    selectedPos += 1;
                    if (selectedPos < 0)
                        selectedPos = 0;

                    messagesAdapter.addDateHeader(selectedPos, date);
                    mess.setCreatedAt(date);
                    Log.e("ToanNM", "setCreatedDate -> " + selectedPos + "___" + date);

                    SharedPref.saveLong(DefaultMessagesActivity.this, SharedPref.KEY_DEFAULT_CHAT_TIME,
                            date.getTime());
                    break;

                }
            } else if (item.item instanceof Date) {
                Date mDate = (Date) item.item;

                Log.e("ToanNM", mDate + "______" + lastOnClickMessage.getCreatedAt());

//                if (mDate.getTime() == lastOnClickMessage.getCreatedAt().getTime()) {
                mDate.setTime(date.getTime());
                messagesAdapter.items.set(i, item);
                Log.e("ToanNM", "set Header -> " + date);
//                }
            }
        }
        messagesAdapter.notifyDataSetChanged();
    }
}
