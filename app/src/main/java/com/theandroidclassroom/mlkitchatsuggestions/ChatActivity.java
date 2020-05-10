package com.theandroidclassroom.mlkitchatsuggestions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.suggestionParent)
    LinearLayout mSuggestionParent;
    @BindView(R.id.message)
    EditText mMessageEt;
    @BindView(R.id.send)
    Button mSend;

    DatabaseReference mMsgRef;
    private String myMobile,myName;
    private FirebaseRecyclerOptions options;
    private List<FirebaseTextMessage> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);

        myMobile = getIntent().getStringExtra("mobile");
        myName = getIntent().getStringExtra("name");
        mMsgRef = FirebaseDatabase.getInstance().getReference().child("chat");

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessageEt.getText().toString().trim();
                Map<String, Object> params = new HashMap<>();
                MessagePojo pojo = new MessagePojo();
                pojo.setMobile(myMobile);
                pojo.setMsg(message);
                pojo.setTimestamp(System.currentTimeMillis());
                pojo.setName(myName);
                mMsgRef.push().setValue(pojo);
                mMessageEt.setText("");
            }
        });

        mMsgRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot,
                                     @Nullable String s) {



                    MessagePojo pojo = dataSnapshot.getValue(MessagePojo.class);
                    if (pojo.getMobile().equals(myMobile)) {
                        mList.add(FirebaseTextMessage
                                .createForLocalUser(pojo.getMsg(),
                                        pojo.getTimestamp()));

                    } else {
                        mList.add(FirebaseTextMessage.createForRemoteUser(
                                pojo.getMsg(), pojo.getTimestamp(), pojo.getMobile()
                        ));
                    }
                    suggestReplies();
                }



            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        options = new FirebaseRecyclerOptions.Builder<MessagePojo>()
                .setQuery(mMsgRef.orderByChild("timestamp"), MessagePojo.class)
                .build();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<MessagePojo, MyViewHolder> adapter =
                new FirebaseRecyclerAdapter<MessagePojo, MyViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyViewHolder holder, int i,
                                                    @NonNull MessagePojo pojo) {
                        holder.name.setText(pojo.getName() + ": ");

                        Log.d("MUR", "MUr MESSAGES: "+pojo.getMsg()+"\n"+pojo.getTimestamp());
                        holder.msg.setText(pojo.getMsg());

                    }

                    @NonNull
                    @Override
                    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view;
                        if (viewType == 1) {
                            view = LayoutInflater.from(getApplicationContext())
                                    .inflate(R.layout.sender_msg_layout,
                                            parent, false);

                        } else {
                            view = LayoutInflater.from(getApplicationContext())
                                    .inflate(R.layout.reciever_msg_layout,
                                            parent, false);
                        }
                        return new MyViewHolder(view);
                    }

                    @Override
                    public int getItemViewType(int position) {
                        MessagePojo pojo = getItem(position);
                        if (pojo.getMobile().equals(myMobile)) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }

                    @Override
                    public void onDataChanged() {
                        super.onDataChanged();

                        mRecyclerView.getLayoutManager().scrollToPosition(getItemCount() - 1);
                    }
                };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    private void suggestReplies() {
        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();
        smartReply.suggestReplies(mList).addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
            @Override
            public void onSuccess(SmartReplySuggestionResult smartReplySuggestionResult) {
                mSuggestionParent.removeAllViews();
                for (SmartReplySuggestion suggestion : smartReplySuggestionResult.getSuggestions()) {
                    View view = LayoutInflater.from(getApplicationContext()).
                            inflate(R.layout.smart_replies, null, false);
                    TextView reply = view.findViewById(R.id.reply);
                    reply.setText(suggestion.getText());
                    mSuggestionParent.addView(view);

                    reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMessageEt.setText(reply.getText());
                        }
                    });
                }
            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.msg)
        TextView msg;
        @BindView(R.id.name)
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}
