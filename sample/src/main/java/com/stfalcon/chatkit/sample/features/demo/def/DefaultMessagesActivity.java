package com.stfalcon.chatkit.sample.features.demo.def;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.sample.R;
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.features.demo.DemoMessagesActivity;
import com.stfalcon.chatkit.sample.utils.SharedPref;

public class DefaultMessagesActivity extends DemoMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageInput.TypingListener,
        MessagesListAdapter.OnMessageLongClickListener<Message> {

    public static void open(Context context) {
        context.startActivity(new Intent(context, DefaultMessagesActivity.class));
    }

    private MessagesList messagesList;

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
                MessagesFixtures.getTextMessage(input.toString()), true);
        Log.e("Debug", "onSubmit -> " + input.toString());
        return true;
    }

    @Override
    public void onAddAttachments() {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getImageMessage(), true);
    }

    private void initAdapter() {
        final String senderId = super.senderId;
        super.messagesAdapter = new MessagesListAdapter<>(super.senderId, super.imageLoader);
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
        messagesAdapter.setOnMessageLongClickListener(this);
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
}
