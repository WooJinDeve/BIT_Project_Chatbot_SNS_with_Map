package com.test.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.test.chatapp.R;
import com.test.chatapp.activities.CalendarActivity;
import com.test.chatapp.activities.DetailImage;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.databinding.ItemContainerChatbotAutoCalendarBinding;
import com.test.chatapp.databinding.ItemContainerChatbotAutoCalendarCheckBinding;
import com.test.chatapp.databinding.ItemContainerChatbotAutoCalendarResultBinding;
import com.test.chatapp.databinding.ItemContainerChatbotAutoCalendarTimeBinding;
import com.test.chatapp.databinding.ItemContainerChatbotBinding;
import com.test.chatapp.databinding.ItemContainerChatbotCalendarRecyclerBinding;
import com.test.chatapp.databinding.ItemContainerChatbotCalendarSaveBinding;
import com.test.chatapp.databinding.ItemContainerChatbotRecyclerBinding;
import com.test.chatapp.databinding.ItemContainerChatbotWeatherBinding;
import com.test.chatapp.databinding.ItemContainerReceivedImageBinding;
import com.test.chatapp.databinding.ItemContainerReceivedMessageBinding;
import com.test.chatapp.databinding.ItemContainerSendImageBinding;
import com.test.chatapp.databinding.ItemContainerSentMessageBinding;
import com.test.chatapp.models.ChatMessage;
import com.test.chatapp.models.Place;
import com.test.chatapp.models.Restaurant;
import com.test.chatapp.models.Schedule;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    private final String receiverId;
    private static String friend_name;
    private Bitmap receiverProfileImage;
    private String[] weekdays = {"월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};

    static public LatLng latLng = null;
    static public String senderId_static = null;
    static public String receiverId_static = null;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_IMAGE_SENT = 3;
    public static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
    public static final int VIEW_TYPE_CHATBOT_TEXT = 5;
    public static final int VIEW_TYPE_CHATBOT_WEATHER = 6;
    public static final int VIEW_TYPE_CHATBOT_RECYCLER = 7;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_SAVE = 8;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_LOAD = 9;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE = 10;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_TIME = 11;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_RESULT = 12;
    public static final int VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_CHECK = 13;
    public static PreferenceManager preferenceManager;
    public static FirebaseFirestore database;

    public static final int GOOGLE_MAP_SELECT_LOCATION = 100;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    //초기화
    public ChatAdapter(List<ChatMessage> chatMessages, String senderId, String receiverId, Bitmap receiverProfileImage, String friend_name) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverProfileImage = receiverProfileImage;
        senderId_static = senderId;
        receiverId_static = receiverId;
        this.friend_name = friend_name;
    }

    //채팅 내 올려질 아이템 구분
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            return new SentImageViewHolder(
                    ItemContainerSendImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_TEXT) {
            return new ChatbotViewHolder(
                    ItemContainerChatbotBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );

        } else if (viewType == VIEW_TYPE_CHATBOT_WEATHER) {
            return new ChatbotWeatherViewHolder(
                    ItemContainerChatbotWeatherBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_SAVE) {
            return new CalendarSaveViewHolder(
                    ItemContainerChatbotCalendarSaveBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE) {
            return new ScheduleMessageViewHolder(
                    ItemContainerChatbotAutoCalendarBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_TIME) {
            return new ScheduleMessageTimeViewHolder(
                    ItemContainerChatbotAutoCalendarTimeBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_CHECK) {
            return new ScheduleMessageCheckViewHolder(
                    ItemContainerChatbotAutoCalendarCheckBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_RESULT) {
            return new ScheduleMessageResultViewHolder(
                    ItemContainerChatbotAutoCalendarResultBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_CALENDAR_LOAD) {
            return new CalendarRecyclerViewHolder(
                    ItemContainerChatbotCalendarRecyclerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_CHATBOT_RECYCLER) {
            return new ChatbotRecyclerViewHolder(
                    ItemContainerChatbotRecyclerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
            return new ReceivedImageViewHolder(
                    ItemContainerReceivedImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }


    //채팅 내 올려질 아이템의 뷰홀더에 해당 채팅 데이터 넣어주기
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_IMAGE_SENT) {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_TEXT) {
            ((ChatbotViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_WEATHER) {
            ((ChatbotWeatherViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_SAVE) {
            ((CalendarSaveViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_LOAD) {
            ((CalendarRecyclerViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE) {
            ((ScheduleMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_TIME) {
            ((ScheduleMessageTimeViewHolder) holder).setData(chatMessages.get(position));
        }else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_CHECK) {
            ((ScheduleMessageCheckViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_RESULT) {
            ((ScheduleMessageResultViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_CHATBOT_RECYCLER) {
            ((ChatbotRecyclerViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_IMAGE_RECEIVED) {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    //말풍선 갯수
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    //메시지 종류 구분(젤리의 톡인지, 사진인지)
    @Override
    public int getItemViewType(int position) {
        String message = chatMessages.get(position).message;
        if (chatMessages.get(position).message.contains("@Zelly ")) {
            if (chatMessages.get(position).message.contains("날씨")) {
                return VIEW_TYPE_CHATBOT_WEATHER;
            } else if (chatMessages.get(position).message.contains("맛집") || chatMessages.get(position).message.contains("명소")) {
                return VIEW_TYPE_CHATBOT_RECYCLER;
            } else if (chatMessages.get(position).message.contains("schedules"))
                return VIEW_TYPE_CHATBOT_CALENDAR_LOAD;
            else if (chatMessages.get(position).message.contains("calendar_save")) {
                return VIEW_TYPE_CHATBOT_CALENDAR_SAVE;
            } else if (chatMessages.get(position).message.contains("ChatbotCalendar")) {
                return VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE;
            } else if (chatMessages.get(position).message.contains("ChatbotAutoCalendar")) {
                return VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_TIME;
            } else if (chatMessages.get(position).message.contains("ChatbotAutoCheck")) {
                return VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_CHECK;
            }else if (chatMessages.get(position).message.contains("ChatbotAutoScheduleResult")) {
                return VIEW_TYPE_CHATBOT_CALENDAR_SCHEDULE_RESULT;
            } else {
                return VIEW_TYPE_CHATBOT_TEXT;
            }
        } else if (chatMessages.get(position).senderId.equals(senderId)) {
            if (chatMessages.get(position).message.contains("@Picture ")) {
                return VIEW_TYPE_IMAGE_SENT;
            }
            return VIEW_TYPE_SENT;
        } else {
            if (chatMessages.get(position).message.contains("@Picture ")) {
                return VIEW_TYPE_IMAGE_RECEIVED;
            }
            return VIEW_TYPE_RECEIVED;
        }
    }

    //보내는 메시지 뷰홀더
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    //보내는 이미지 뷰홀더
    static class SentImageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSendImageBinding binding;

        SentImageViewHolder(ItemContainerSendImageBinding itemContainerSendImageBinding) {
            super(itemContainerSendImageBinding.getRoot());
            binding = itemContainerSendImageBinding;

            binding.imageMessage.setOnClickListener(v -> detailImage(binding.imageMessage.getContext(), DetailImage.class, binding.imageMessage));
        }


        void setData(ChatMessage chatMessage) {

            firebaseImageLoader(binding.imageMessage.getContext(), SplitPictureText(chatMessage.message), binding.imageMessage);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    //수신 메시지 뷰홀더
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }


    //스케줄 뷰홀더
    class ScheduleMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotAutoCalendarBinding binding;

        ScheduleMessageViewHolder(ItemContainerChatbotAutoCalendarBinding itemContainerChatbotAutoCalendarBinding) {
            super(itemContainerChatbotAutoCalendarBinding.getRoot());
            binding = itemContainerChatbotAutoCalendarBinding;
        }

        void setData(ChatMessage chatMessage) {
            final PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            database = FirebaseFirestore.getInstance();
            Button[] buttons = new Button[7];
            buttons[0] = binding.weekday1;
            buttons[1] = binding.weekday2;
            buttons[2] = binding.weekday3;
            buttons[3] = binding.weekday4;
            buttons[4] = binding.weekday5;
            buttons[5] = binding.weekday6;
            buttons[6] = binding.weekday7;
            String textmessage = "젤리 약속 매칭 시스템입니다.\n\n";
            boolean b = true;
            boolean[][] barr = new boolean[7][25];
            String[][] dates = new String[7][2];
            String message = chatMessage.message.replace("@Zelly ", "");

            if (message.contains("null")) {
                textmessage += "최근 일주일간 일정이 없습니다.\n약속이 없을 경우 원하는 일정을 선택하기 어렵습니다.";
                b = false;
            }
            if (b) {
                binding.weekdaylayout.setVisibility(View.VISIBLE);
                String[] messages = chatMessage.message.split("[\\[\\]\\,]");

                String[] date = new String[7];
                for (int i = 0; i < 7; i++)
                    date[i] = messages[i + 1].trim();
                Arrays.sort(date);

                textmessage += date[0] + "부터 " + date[6] + "까지 \n" + preferenceManager.getString(Constants.KEY_NAME) + "님과 " + friend_name + "님의 " + "약속 매칭 결과입니다.";

                for (int i = 0; i < 7; i++) {

                    dates[i][0] = messages[i + 1];
                }
                for (int i = 0; i < 7; i++) {
                    dates[i][1] = messages[i + 10];
                }
                for (int i = 0; i < 7; i++) {

                    String st = dates[i][1].replaceAll("/", ":");
                    String[] arrs = st.split(":");
                    if (arrs.length == 1) {
                        continue;
                    }
                    for (int j = 0; j < arrs.length; j = j + 4) {
                        int start = Integer.parseInt(arrs[j].trim());
                        start = start == 0 ? 0 : start ;
                        int end = Integer.parseInt(arrs[j + 2].trim()) == 0 ? 24 : Integer.parseInt(arrs[j + 2].trim()) > 24 ? 24 : Integer.parseInt(arrs[j + 2].trim());
                        for (int k = start; k < end; k++) {
                            barr[i][k] = true;
                        }
                    }
                }

                int[] arr = new int[7];
                String maxday = "";
                String minday = "";
                int min = Integer.MAX_VALUE;
                int max = 0;
                for (int i = 0; i < 7; i++) {
                    int count = 0;
                    for (int j = 0; j < 24; j++)
                        if (barr[i][j])
                            count++;
                    if (count == 24)
                        arr[i] = -1;
                    else {
                        if (min > count) {
                            min = count;
                            minday = weekdays[i];
                        }
                        if (max <= count) {
                            max = count;
                            maxday = weekdays[i];
                        }
                    }
                }
                textmessage += "\n\n" + maxday + "은 일정이 많은 요일입니다.\n추천 요일은 " + minday + "로 일정이 가장 없습니다.\n\n상세 스케줄 이동 클릭 시 더 자세한 정보를 입력할 수 있습니다.";
                if (chatMessage.check.equals("false")) {
                    binding.weekdaylayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < 7; i++) {
                        if (arr[i] != -1)
                            buttons[i].setVisibility(View.VISIBLE);
                    }
                    ;
                }
            }

            final boolean[] ok = {false};
            for (int i = 0; i < 7; i++) {
                int finalI = i;
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ok[0] == false) {
                            buttons[finalI].setTextColor(Color.WHITE);
                            buttons[finalI].setBackgroundResource(R.drawable.shadow_round_color);

                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, chatMessage.receiverId);
                            String msg = "@Zelly " + getMessage(barr[finalI], finalI, dates[finalI][0]);
                            message.put(Constants.KEY_MESSAGE, msg);
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            message.put(Constants.KEY_MESSAGE_CHECK, "false");

                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.documentId).update(Constants.KEY_MESSAGE_CHECK, "true");
                            ok[0] = true;
                        } else {
                            Toast.makeText(binding.getRoot().getContext(), "요일은 한번만 클릭할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            binding.otherSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(binding.getRoot().getContext(), CalendarActivity.class);
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
            binding.textMessage.setText(textmessage);
            binding.textDateTime.setText(chatMessage.dateTime);
        }

        private String getMessage(boolean[] times, int idx, String date) {
            StringBuilder sb = new StringBuilder();
            String day = weekdays[idx];
            sb.append("{\'ChatbotAutoCalendar\':[");

            for (int i = 0; i < 24; i++) {
                if (i < 23)
                    sb.append(times[i] == true ? 1 : 0).append(", ");
                else
                    sb.append(times[i] == true ? 1 : 0).append("], \"state\":");
            }
            sb.append("\"ChatbotAutoCalendar\", \"day\":\"").append(day).append("\", \"date\" : \"").append(date).append("\"}");


            return sb.toString();
        }
    }

    static class ScheduleMessageTimeViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotAutoCalendarTimeBinding binding;

        ScheduleMessageTimeViewHolder(ItemContainerChatbotAutoCalendarTimeBinding itemContainerChatbotAutoCalendarTimeBinding) {
            super(itemContainerChatbotAutoCalendarTimeBinding.getRoot());
            binding = itemContainerChatbotAutoCalendarTimeBinding;
        }

        void setData(ChatMessage chatMessage) {
            final PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            database = FirebaseFirestore.getInstance();

            String message = chatMessage.message.replace("@Zelly ", "");
            String[] messages = message.split("[\\[\\]\\,]");
            boolean[] time = new boolean[24];
            int[] clock = new int[4];
            boolean[][] enabletime = new boolean[4][7];
            String[] tem = messages[27].split("\"");
            String[] date = messages[messages.length - 1].split("\"");
            String dates = date[3].trim();
            Button[] buttons = new Button[4];
            buttons[0] = binding.time1;
            buttons[1] = binding.time2;
            buttons[2] = binding.time3;
            buttons[3] = binding.time4;

            String text = dates + "(" + tem[3] + ")을 선택을 선택하셨습니다.\n\n";

            for (int i = 1; i <= 24; i++) {
                boolean b = messages[i - 1].equals("1") || messages[i - 1].equals(" 1");
                if (i == 1)
                    b = messages[24].equals("1") || messages[24].equals(" 1");
                time[i - 1] = b;
            }

            for (int i = 0; i < 4; i++) {
                int temp = i * 6 + 6;
                for (int j = i * 6; j < temp; j++) {
                    if (!time[j]) {
                        if (j < 6) {
                            enabletime[i][j] = true;
                            clock[i]++;
                        } else if (j < 12) {
                            enabletime[i][j - 6] = true;
                            clock[i]++;
                        } else if (j < 18) {
                            enabletime[i][j - 12] = true;
                            clock[i]++;
                        } else if (j < 24) {
                            enabletime[i][j - 18] = true;
                            clock[i]++;
                        }
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                if (clock[i] >= 3) {
                    enabletime[i][6] = true;
                    if (i == 0)
                        text += "새벽 ";
                    else if (i == 1)
                        text += "아침 ";
                    else if (i == 2)
                        text += "점심 ";
                    else if (i == 3)
                        text += "저녁 ";
                }
            }

            text += "중 하나를 선택하시면 자동으로 약속이 매칭됩니다.\n\n상세 스케줄 이동 클릭 시 더 자세한 정보를 입력할 수 있습니다.";

            String[] timeclock = new String[4];
            for (int i = 0; i < 4; i++) {
                int timetemp = Integer.MAX_VALUE;
                for (int j = 0; j < 6; j++) {
                    if (timetemp < j)
                        continue;
                    if (enabletime[i][6]) {
                        if (enabletime[i][j] && enabletime[i][j + 1]) {
                            switch (i) {
                                case 0:
                                    timetemp = j;
                                    timeclock[0] = "0" + j + ":00:" + "0" + (j + 1) + ":00";
                                    break;
                                case 1:
                                    timetemp = j;
                                    if (j < 3)
                                        timeclock[1] = "0" + (j + 6) + ":00:" + "0" + (j + 7) + ":00";
                                    else if (j == 3)
                                        timeclock[1] = "0" + (j + 6) + ":00:" + (j + 7) + ":00";
                                    else timeclock[1] = (j + 6) + ":00:" + (j + 7) + ":00";
                                    break;

                                case 2:
                                    timeclock[2] = (j + 12) + ":00:" + (j + 13) + ":00";
                                    timetemp = j;
                                    break;
                                case 3:
                                    timetemp = j;
                                    timeclock[3] = (j + 18) + ":00:" + (j + 24) + ":00";
                                    break;
                            }
                        }
                    }
                }
            }

            final boolean[] ok = {false};
            for (int i = 0; i < 4; i++) {
                if (chatMessage.check.equals("false")) {
                    binding.timelayout.setVisibility(View.VISIBLE);
                    if (!enabletime[i][6]) {
                        buttons[i].setVisibility(View.GONE);
                        continue;
                    }
                    buttons[i].setVisibility(View.VISIBLE);
                }

                int finalI = i;
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ok[0] == false) {
                            String[] times = timeclock[finalI].split(":");
                            buttons[finalI].setTextColor(Color.WHITE);
                            buttons[finalI].setBackgroundResource(R.drawable.shadow_round_color);


                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, chatMessage.receiverId);
                            String msg = getMessage(dates, tem[3], times, timeclock[finalI]);
                            message.put(Constants.KEY_MESSAGE, msg);
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            message.put(Constants.KEY_MESSAGE_CHECK, "false");

                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.documentId).update(Constants.KEY_MESSAGE_CHECK, "true");

                            ok[0] = true;
                        } else {
                            Toast.makeText(binding.getRoot().getContext(), "시간은 한번만 클릭할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            binding.otherSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(binding.getRoot().getContext(), CalendarActivity.class);
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
            binding.textMessage.setText(text);
            binding.textDateTime.setText(chatMessage.dateTime);
        }

        public String getMessage(String date, String week, String[] times, String timestamp) {
            String time = times[0] + ":" + times[1] + " ~ " + times[2] + ":" + times[3];
            return "@Zelly [" + date + "," + week + "," + time +","+ timestamp +"] ChatbotAutoCheck";
        }
    }


    class ScheduleMessageCheckViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerChatbotAutoCalendarCheckBinding binding;
        private PreferenceManager preferenceManager;
        ScheduleMessageCheckViewHolder(ItemContainerChatbotAutoCalendarCheckBinding itemContainerChatbotAutoCalendarCheckBinding) {
            super(itemContainerChatbotAutoCalendarCheckBinding.getRoot());
            binding = itemContainerChatbotAutoCalendarCheckBinding;
        }

        void setData(ChatMessage chatMessage) {
            preferenceManager = new PreferenceManager(binding.getRoot().getContext());

            String message = chatMessage.message.replace("@Zelly ", "");
            String[] messages = message.split("[\\[\\]\\,]");
            //날짜 , 요일 , 시간
            String member = "인원 : " + preferenceManager.getString(Constants.KEY_NAME)+"님, " + friend_name +"님 (2명)";
            String date = "날짜 : " + messages[1] + " (" +messages[2]+")";
            String time = "시간 : " + messages[3];
            String timeStamp = messages[4];
            final boolean[] ok = {false};

            binding.textMessage.setText(friend_name + "님이 약속을 매칭하셨습니다.");
            binding.textMember.setText(member);
            binding.textDate.setText(date);
            binding.textTime.setText(time);

            if(chatMessage.check.equals("true"))
                binding.scheduleButtonLayout.setVisibility(View.GONE);

            binding.scheduleFalseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(chatMessage.senderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                        Toast.makeText(binding.getRoot().getContext(), "상대방만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }else{
                        if(!ok[0]){
                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, chatMessage.senderId);
                            String msg = getText(preferenceManager.getString(Constants.KEY_NAME));
                            message.put(Constants.KEY_MESSAGE, msg);
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            message.put(Constants.KEY_MESSAGE_CHECK, "false");

                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.documentId).update(Constants.KEY_MESSAGE_CHECK, "true");
                            ok[0] = true;
                        }else
                            Toast.makeText(binding.getRoot().getContext(),"한번만 클릭할 수 있습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            binding.scheduleTrueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(chatMessage.senderId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                        Toast.makeText(binding.getRoot().getContext(), "상대방만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }else {
                        if (!ok[0]) {
                            String[] times = timeStamp.split(":");

                            HashMap<String, Object> cal = new HashMap<>();
                            cal.put(Constants.KEY_CALENDARME, chatMessage.senderId);
                            cal.put(Constants.KEY_MYNAME, preferenceManager.getString(Constants.KEY_NAME));
                            cal.put(Constants.KEY_FRIENDNAME, friend_name);
                            cal.put(Constants.KEY_CALENDARFRIEND, chatMessage.receiverId);
                            cal.put(Constants.KEY_STARTCALENDAR_HOUR, times[0]);
                            cal.put(Constants.KEY_STARTCALENDAR_MINUTE, times[1]);
                            cal.put(Constants.KEY_ENDCALENDAR_HOUR, times[2]);
                            cal.put(Constants.KEY_ENDCALENDAR_MINUTE, times[3]);
                            cal.put(Constants.KEY_TIMESTAMP, timeStamp);
                            cal.put(Constants.KEY_CALENDARDATE, messages[1]);
                            cal.put(Constants.KEY_CALENDARLON, 0);
                            cal.put(Constants.KEY_CALENDARLAT, 0);
                            cal.put(Constants.KEY_CALENDARCONTENT, "");
                            cal.put(Constants.KEY_CALENDAR_EXPLAIN, "");

                            HashMap<String, Object> message = new HashMap<>();
                            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            message.put(Constants.KEY_RECEIVER_ID, chatMessage.senderId);
                            String msg = getMessage(date, time);
                            message.put(Constants.KEY_MESSAGE, msg);
                            message.put(Constants.KEY_TIMESTAMP, new Date());
                            message.put(Constants.KEY_MESSAGE_CHECK, "false");

                            database.collection(Constants.KEY_COLLECTION_CALENDAR).add(cal);
                            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                            database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.documentId).update(Constants.KEY_MESSAGE_CHECK, "true");
                            ok[0] = true;
                        }else
                            Toast.makeText(binding.getRoot().getContext(),"한번만 클릭할 수 있습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        public String getText(String Name){
            return "@Zelly {\"state\":\"텍스트\",\"value\":\""+ Name+"님이 약속을 취소하셨습니다.\"}";
        }

        public String getMessage(String date, String time) {
            return "@Zelly ["+ date + "," + time +"] ChatbotAutoScheduleResult";
        }
    }

    static class ScheduleMessageResultViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotAutoCalendarResultBinding binding;

        ScheduleMessageResultViewHolder(ItemContainerChatbotAutoCalendarResultBinding itemContainerChatbotAutoCalendarResultBinding) {
            super(itemContainerChatbotAutoCalendarResultBinding.getRoot());
            binding = itemContainerChatbotAutoCalendarResultBinding;
        }

        void setData(ChatMessage chatMessage) {
            final PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());

            String message = chatMessage.message.replace("@Zelly ", "");
            String[] messages = message.split("[\\[\\]\\,]");
            //날짜 , 요일 , 시간
            String member = "인원 : " + preferenceManager.getString(Constants.KEY_NAME)+"님, " + friend_name +"님 (2명)";
            String date = messages[1];
            String time = messages[2];
            binding.otherSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(binding.getRoot().getContext(), CalendarActivity.class);
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
            binding.textMessageFirst.setText("젤리 약속 자동 매칭 결과입니다.\n");
            binding.textMember.setText(member);
            binding.textDate.setText(date);
            binding.textTime.setText(time);
            binding.textMessageLast.setText("\n스케줄을 확인 및 변경을 원하시면 스케줄 확인 버튼을 눌러주세요.");
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }


    static class CalendarRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotCalendarRecyclerBinding binding;


        CalendarRecyclerViewHolder(ItemContainerChatbotCalendarRecyclerBinding itemContainerChatbotCalendarRecyclerBinding) {
            super(itemContainerChatbotCalendarRecyclerBinding.getRoot());
            binding = itemContainerChatbotCalendarRecyclerBinding;
        }

        void setData(ChatMessage chatMessage) {
            try {
                JSONObject jsonObject = new JSONObject(chatMessage.message.replace("@Zelly ", ""));
                jsonObject.getString("state").equals("schedules");
                List<Schedule> schedules = new ArrayList<>();

                JSONArray jsonArray = jsonObject.getJSONArray("schedules");

                for (int i = 0; i < jsonArray.length(); i++) {
                    Schedule schedule = new Schedule();
                    JSONObject jsonobj = jsonArray.getJSONObject(i);
                    schedule.calendarMe = jsonobj.getString("calendarMe");
                    schedule.calendarFriend = jsonobj.getString("calendarFriend");
                    String[] date = jsonobj.getString("calendarDate").split("-");
                    if (1 <= Integer.parseInt(date[1]) && Integer.parseInt(date[1]) <= 9) {
                        date[1] = String.format("%02d", Integer.parseInt(date[1]));
                        String d = String.join("-", date);
                        schedule.calendarDate = d;
                        System.out.println(d);
                    } else {
                        schedule.calendarDate = jsonobj.getString("calendarDate");
                        System.out.println(schedule.calendarDate);
                    }
                    schedule.calendarLat = jsonobj.getDouble("calendarLat");
                    schedule.calendarLon = jsonobj.getDouble("calendarLon");
                    schedule.calendarContent = jsonobj.getString("calendarContent");
                    schedule.friendName = jsonobj.getString("FriendName");
                    schedule.myName = jsonobj.getString("MyName");
                    schedule.calendarExplain = jsonobj.getString("calendarExplain");
                    schedule.startcalendarTimeHour = jsonobj.getInt("startcalendarTimeHour");
                    schedule.startcalendarTimeMinute = jsonobj.getInt("startcalendarTimeMinute");

                    schedule.calendarLocation = getAddress(binding.getRoot().getContext(), schedule.calendarLat, schedule.calendarLon);

                    schedules.add(schedule);
                }

                Collections.sort(schedules, (obj1, obj2) -> obj1.calendarDate.compareTo(obj2.calendarDate));

                ScheduleAdapter scheduleAdapter = new ScheduleAdapter(schedules);
                binding.calendarRecyclerView.setVisibility(View.VISIBLE);
                binding.calendarRecyclerView.setAdapter(scheduleAdapter);

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    //수신 이미지 뷰홀더
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedImageBinding binding;

        ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedImageBinding) {
            super(itemContainerReceivedImageBinding.getRoot());
            binding = itemContainerReceivedImageBinding;

            binding.imageMessage.setOnClickListener(v -> detailImage(binding.imageMessage.getContext(), DetailImage.class, binding.imageMessage));
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {

            firebaseImageLoader(binding.imageMessage.getContext(), SplitPictureText(chatMessage.message), binding.imageMessage);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }


    //캘린더 저장 뷰홀더
    static class CalendarSaveViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotCalendarSaveBinding binding;


        CalendarSaveViewHolder(ItemContainerChatbotCalendarSaveBinding itemContainerChatbotCalendarSaveBinding) {
            super(itemContainerChatbotCalendarSaveBinding.getRoot());
            binding = itemContainerChatbotCalendarSaveBinding;
            preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            database = FirebaseFirestore.getInstance();

            binding.goCalendarBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CalendarActivity calendarActivity = new CalendarActivity();
                    Intent intent = new Intent(binding.getRoot().getContext(), calendarActivity.getClass());
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
        }

        void setData(ChatMessage chatMessage) {
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    //위도, 경도로 한글 주소 받기
    static public String getAddress(Context mContext, double lat, double lng) {
        String nowAddr = "주소를 가져올 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    nowAddr = address.get(0).getAddressLine(0).toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nowAddr;
    }

    //날씨 알려주는 뷰홀더
    static class ChatbotWeatherViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotWeatherBinding binding;

        ChatbotWeatherViewHolder(ItemContainerChatbotWeatherBinding itemContainerChatbotWeatherBinding) {
            super(itemContainerChatbotWeatherBinding.getRoot());
            binding = itemContainerChatbotWeatherBinding;
        }

        void setData(ChatMessage chatMessage) {
            try {
                JSONObject jsonObject = new JSONObject(chatMessage.message.replace("@Zelly ", ""));

                String value = jsonObject.getString("weather_value");
                if (value.contains("흐림") || value.contains("흐린") || value.contains("흐려저")) {
                    binding.imageView.setImageResource(R.drawable.weather2);
                } else if (value.contains("비")) {
                    binding.imageView.setImageResource(R.drawable.weather3);
                } else if (value.contains("눈")) {
                    binding.imageView.setImageResource(R.drawable.weather4);
                } else if (value.contains("번개")) {
                    binding.imageView.setImageResource(R.drawable.weather5);
                } else if (value.contains("황사")) {
                    binding.imageView.setImageResource(R.drawable.weather6);
                } else if (value.contains("우박")) {
                    binding.imageView.setImageResource(R.drawable.weather7);
                } else {
                    binding.imageView.setImageResource(R.drawable.weather1);
                }
                binding.weatherLocation.setText(jsonObject.getString("location"));
                binding.weatherCelsius.setText(jsonObject.getString("temperature") + " ºC");
                binding.weatherValue.setText(value);
                binding.humidity.setText("습도 : " + jsonObject.getString("humidity"));
                binding.findDust.setText("미세먼지 : " + jsonObject.getString("dust_value"));
                binding.textDateTime.setText(chatMessage.dateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //챗봇 채팅 뷰홀더
    static class ChatbotViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotBinding binding;

        ChatbotViewHolder(ItemContainerChatbotBinding itemContainerChatbotBinding) {
            super(itemContainerChatbotBinding.getRoot());
            binding = itemContainerChatbotBinding;
        }

        void setData(ChatMessage chatMessage) {
            try {
                JSONObject jsonObject = new JSONObject(chatMessage.message.replace("@Zelly ", ""));
                binding.textMessage.setText(jsonObject.getString("value"));
                binding.textDateTime.setText(chatMessage.dateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //챗봇이 여러 개의 정보를 띄울 때 사용되는 리사이클러 뷰홀더(맛집, 명소, 일정)
    static class ChatbotRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerChatbotRecyclerBinding binding;
        ImageView[] imageViews = new ImageView[6];


        ChatbotRecyclerViewHolder(ItemContainerChatbotRecyclerBinding itemContainerChatbotRecyclerBinding) {
            super(itemContainerChatbotRecyclerBinding.getRoot());
            binding = itemContainerChatbotRecyclerBinding;
        }

        void setData(ChatMessage chatMessage) {
            try {
                JSONObject jsonObject = new JSONObject(chatMessage.message.replace("@Zelly ", ""));

                if (jsonObject.getString("state").equals("맛집")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("restaurants");

                    List<Restaurant> restaurants = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Restaurant restaurant = new Restaurant();
                        JSONObject jsonobj = jsonArray.getJSONObject(i);
                        restaurant.name = jsonobj.getString("title");
                        restaurant.imgSource = jsonobj.getString("image");
                        restaurant.rating = jsonobj.getString("rating");

                        restaurants.add(restaurant);
                    }
                    RestaurantAdapter restaurantAdapter = new RestaurantAdapter(restaurants);
                    binding.postRecyclerView.setAdapter(restaurantAdapter);
                } else if (jsonObject.getString("state").equals("명소")) {

                    JSONArray jsonArray = jsonObject.getJSONArray("places");

                    List<Place> places = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Place place = new Place();
                        JSONObject jsonobj = jsonArray.getJSONObject(i);
                        place.name = jsonobj.getString("title");
                        place.category = jsonobj.getString("category");
                        place.image = jsonobj.getString("image");
                        place.address = jsonobj.getString("addresses");
                        if (!place.name.equals("0")) {
                            places.add(place);
                        }
                    }
                    PlaceAdapter placeAdapter = new PlaceAdapter(places);
                    binding.postRecyclerView.setAdapter(placeAdapter);
                }
                try {
                    if (binding.postRecyclerView.getAdapter().getItemCount() > 0) {
                        binding.postRecyclerView.setVisibility(View.VISIBLE);
                        binding.RecyclerViewGridLayout.setVisibility(View.VISIBLE);
                        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.postRecyclerView.getLayoutManager();


                        imageViews[0] = binding.RecyclerViewbutton0;
                        imageViews[1] = binding.RecyclerViewbutton1;
                        imageViews[2] = binding.RecyclerViewbutton2;
                        imageViews[3] = binding.RecyclerViewbutton3;
                        imageViews[4] = binding.RecyclerViewbutton4;
                        imageViews[5] = binding.RecyclerViewbutton5;

                        for (int i = 0; i < binding.postRecyclerView.getAdapter().getItemCount(); i++) {
                            imageViews[i].setVisibility(View.VISIBLE);
                            imageViews[i].setBackgroundResource(R.drawable.background_image);
                        }

                        RecyclerViewButton(0);

                        binding.postRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                RecyclerViewButton(layoutManager.findLastVisibleItemPosition());
                            }
                        });
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void RecyclerViewButton(int position) {
            for (int i = 0; i < binding.postRecyclerView.getAdapter().getItemCount(); i++) {
                imageViews[i].setBackgroundColor(Color.GRAY);
            }
            imageViews[position].setBackgroundColor(Color.BLACK);
            binding.postRecyclerView.smoothScrollToPosition(position);
        }
    }

    //사진 전송 시 파이어베이스에서 저장된 사진 불러오기
    private static void firebaseImageLoader(Context context, String Path, ImageView image) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(image);
            }
        });
    }

    //사진 메시지 구분(@Picture) 자르기
    private static String SplitPictureText(String message) {
        String image;
        image = message.replace("@Picture ", "");
        return image;
    }

    //이미지 확대
    private static void detailImage(Context context, Class<DetailImage> detailActivityClass, ImageView imageView) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        float scale = (float) (720 / (float) bitmap.getWidth());
        int image_w = (int) (bitmap.getWidth() * scale);
        int image_h = (int) (bitmap.getHeight() * scale);
        Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
        resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        Intent intent = new Intent(context, detailActivityClass);
        intent.putExtra("image", byteArray);
        context.startActivity(intent);
    }
}