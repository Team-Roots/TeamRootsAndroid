package com.layer.quick_start_android;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.layer.atlas.AtlasMessageComposer;
import com.layer.atlas.AtlasMessagesList;
import com.layer.atlas.AtlasParticipantPicker;
import com.layer.atlas.AtlasTypingIndicator;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by adityaaggarwal on 2/16/15.
 */
public class ViewMessagesActivity extends ActionBarActivity  {


        private AtlasMessagesList messagesList;
        private AtlasParticipantPicker participantPicker;
        private AtlasTypingIndicator typingIndicator;
        private AtlasMessageComposer atlasComposer;
        private Conversation conversation;
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_messages_view);


            //get current conversation
            Uri id = getIntent().getParcelableExtra("conversation-id");
            if(id != null)
                conversation = ConversationListActivity.layerClient.getConversation(id);


            //set message list
            messagesList = (AtlasMessagesList) findViewById(R.id.messageslist);
            messagesList.init(ConversationListActivity.layerClient, ConversationListActivity.participantProvider);
            messagesList.setConversation(conversation);

            //a view with dynamic filtering of a list that allows you to add participants
            participantPicker = (AtlasParticipantPicker) findViewById(R.id.participantpicker);
            String[] currentUser = {ConversationListActivity.layerClient.getAuthenticatedUserId()};
            participantPicker.init(currentUser, ConversationListActivity.participantProvider);
            if(conversation != null)
                participantPicker.setVisibility(View.GONE);

            //to inform user if someone on the receiving end is typing
            typingIndicator = (AtlasTypingIndicator) findViewById(R.id.typingindicator);
            typingIndicator.init(conversation, new AtlasTypingIndicator.Callback(){
                public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
                }
            });

            //used to create and send messages
            atlasComposer = (AtlasMessageComposer) findViewById(R.id.textinput);
            atlasComposer.init(ConversationListActivity.layerClient, conversation);
            atlasComposer.setListener(new AtlasMessageComposer.Listener(){
                //if returns false means the message will not send and participants not entered
                //in new conversation
                public boolean beforeSend(Message message) {
                    if(conversation == null){
                        //does not include sender only reciever
                        String[] participants = participantPicker.getSelectedUserIds();

                        if(participants.length > 0){



                            //load Vanillicon
                            byte[] bytesofTest = ConversationListActivity.layerClient.getAuthenticatedUserId().getBytes();
                            MessageDigest messageDigest = null;
                            try {
                                messageDigest = MessageDigest.getInstance("MD5");
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            byte[] thedigest = messageDigest.digest(bytesofTest);

                            StringBuffer sb = new StringBuffer();
                            for (byte b : thedigest) {
                                sb.append(String.format("%02x", b & 0xff));
                            }
                            String vanilliconLink="http://vanillicon.com/"+sb.toString()+".png";




                            //set MetaData to Conversations
                            HashMap<String,HashMap<String, String>> metadataMap=new HashMap<String, HashMap<String, String>>();
                            HashMap<String, String> counselor=new HashMap<String, String>();
                            HashMap<String, String> student=new HashMap<String, String>();
                            counselor.put("name",ConversationListActivity.participantProvider.getParticipant(participants[0]).getFirstName());
                            counselor.put("ID",ConversationListActivity.participantProvider.getParticipant(participants[0]).getID());
                            counselor.put("avatarString",ConversationListActivity.participantProvider.getParticipant(participants[0]).getAvatarString());
                            student.put("name","Anonymous User 123");
                            student.put("ID",ConversationListActivity.layerClient.getAuthenticatedUserId());
                            student.put("avatarString",vanilliconLink);
                            metadataMap.put("counselor",counselor);
                            metadataMap.put("student",student);





                            participantPicker.setVisibility(View.GONE);
                            conversation = ConversationListActivity.layerClient.newConversation(participants);

                            //set metatdata
                            conversation.putMetadata((Map)metadataMap, false);
                            Log.d("getting Metadata", "MetaData:" + conversation.getMetadata().toString());


                            messagesList.setConversation(conversation);
                            atlasComposer.setConversation(conversation);
                        } else {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }

        protected void onResume() {
            super.onResume();
            ConversationListActivity.layerClient.registerEventListener(messagesList);
        }

        protected void onPause(){
            super.onPause();
            ConversationListActivity.layerClient.unregisterEventListener(messagesList);
        }



}

