//파이어베이스 정보 입력키
package com.test.chatapp.utilities;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_AGE = "age";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PRIVATEKEY = "privatekey";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_STATE = "state";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_FRIEND = "friend";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_MESSAGE_CHECK = "messagecheck";
    public static final String KEY_CHATBOT = "@zelly";

    public static final String KEY_COLLECTION_POST = "post";
    public static final String KEY_POSTCONTENTS = "postcontents";
    public static final String KEY_POSTIMAGE = "postimage";
    public static final String KEY_POSTLAT = "postlat";
    public static final String KEY_POSTLON = "postlon";
    public static final String KEY_POSTING_HASHTAGS = "postHashtags";
    public static final String KEY_PLACENAME = "placeName";

    public static final String KEY_COLLECTION_CALENDAR = "calendar";
    public static final String KEY_CALENDARCONTENT = "calendarContent";
    public static final String KEY_CALENDARDATE = "calendarDate";
    public static final String KEY_CALENDARME = "calendarMe";
    public static final String KEY_CALENDARFRIEND = "calendarFriend";
    public static final String KEY_CALENDARLAT = "calendarLat";
    public static final String KEY_CALENDARLON = "calendarLon";
    public static final String KEY_CALENDARDOCUMENT = "calendarDocument";
    public static final String KEY_FRIENDNAME = "FriendName";
    public static final String KEY_MYNAME = "MyName";
    public static final String KEY_CALENDAR_EXPLAIN = "calendarExplain";
    public static final String KEY_STARTCALENDAR_HOUR = "startcalendarTimeHour";
    public static final String KEY_STARTCALENDAR_MINUTE = "startcalendarTimeMinute";
    public static final String KEY_ENDCALENDAR_HOUR = "endcalendarTimeHour";
    public static final String KEY_ENDCALENDAR_MINUTE = "endcalendarTimeMinute";
    public static final String KEY_SCHEDULE = "schedule";
    public static final String KEY_SCHEDULE_DAYS = "schedule";
    public static final String KEY_SCHEDULE_REPEAT = "repeat";
    public static HashMap<String, String> remoteMsgHeaders = null;

    public static ArrayList<String> postingHashTag = new ArrayList<String>(
            Arrays.asList("#맛집", "#명소", "#여행", "#일상", "#카페", "#셀카", "#쇼핑", "#운동", "#직장", "#뷰티", "#동물"));

    //메시지 알림 관련
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "파이어베이스 키"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
