//CalendarActivity = 세부 일정 추가(내용, 친구, 위치)

package com.test.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.chatapp.adapters.ChatAdapter;
import com.test.chatapp.databinding.ActivityCalendarAddBinding;
import com.test.chatapp.models.Calendar;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.HashMap;

public class CalendarAddActivity extends AppCompatActivity {

    private ActivityCalendarAddBinding binding;

    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;


    static public LatLng latLng = null;
    static public Calendar calendar = new Calendar();


    private int RESULT_OK = 1000;   //친구를 누를 때?
    private int RESULT_LOCATION = 2000; //지도를 누를 때?


    private int GOOGLE_MAP_SELECT_LOCATION_CALENDAR = 200;  //구글맵으로 이동할 때?



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        listener();
    }


    //초기화
    public void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());

        database = FirebaseFirestore.getInstance();
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            //추가되어 있는 일정 클릭 했을 때 관련된 정보를 가져옴
            if (getIntent.getStringExtra(Constants.KEY_CALENDARDOCUMENT) != null) {
                calendar.lat = getIntent.getExtras().getDouble(Constants.KEY_CALENDARLAT);
                calendar.lon = getIntent.getExtras().getDouble(Constants.KEY_CALENDARLON);
                calendar.date = getIntent.getStringExtra(Constants.KEY_CALENDARDATE);
                calendar.friend = getIntent.getStringExtra(Constants.KEY_CALENDARFRIEND);
                calendar.documentid = getIntent.getStringExtra(Constants.KEY_CALENDARDOCUMENT);
                calendar.friend_name = getIntent.getStringExtra(Constants.KEY_FRIENDNAME);
                calendar.me = getIntent.getStringExtra(Constants.KEY_CALENDARME);
                calendar.my_name = getIntent.getStringExtra(Constants.KEY_MYNAME);
                calendar.message = getIntent.getStringExtra(Constants.KEY_CALENDARCONTENT);
                calendar.explain = getIntent.getStringExtra(Constants.KEY_CALENDAR_EXPLAIN);
                calendar.starthour = getIntent.getStringExtra(Constants.KEY_STARTCALENDAR_HOUR);
                calendar.startminute = getIntent.getStringExtra(Constants.KEY_STARTCALENDAR_MINUTE);
                calendar.endhour = getIntent.getStringExtra(Constants.KEY_ENDCALENDAR_HOUR);
                calendar.endminute = getIntent.getStringExtra(Constants.KEY_ENDCALENDAR_MINUTE);



                int int_hour = Integer.parseInt(calendar.starthour);
                int int_minute = Integer.parseInt(calendar.startminute);

                binding.inviteFriend.setText("  " + calendar.friend_name);
                binding.writeCalendar.setText("  " + calendar.message);
                binding.calendarExplain.setText("  " + calendar.explain);
                binding.timePicker.setHour(int_hour);
                binding.timePicker.setMinute(int_minute);




                String result = ChatAdapter.getAddress(this, calendar.lat, calendar.lon);
                binding.calendarLocation.setText("  " + result);
            } else {   //(+)버튼을 눌렀을때만 관련된 정보를 가져옴
                calendar.lat = getIntent.getExtras().getDouble(Constants.KEY_CALENDARLAT);
                calendar.lon = getIntent.getExtras().getDouble(Constants.KEY_CALENDARLON);
                calendar.date = getIntent.getStringExtra(Constants.KEY_CALENDARDATE);
            }
        }
        //친구 닉네임 찾기
        if (calendar.friend != null) {
            findName();
        }

    }

    //클릭 이벤트 처리
    private void listener() {
        binding.startLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.startTimeValue.setTextColor(Color.parseColor("#FFFFFF"));
                binding.startTimeName.setTextColor(Color.parseColor("#FFFFFF"));
                binding.endTimeName.setTextColor(Color.parseColor("#80000000"));
                binding.endTimeValue.setTextColor(Color.parseColor("#80000000"));

                binding.timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        String startHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                        String startMiute = minute < 10 ? "0" + minute : "" + minute;
                        if (hourOfDay >= 12) {
                            binding.startTimeValue.setText("오후 " + startHourOfDay + " : " + startMiute);
                            hourOfDay += 1;
                            if (hourOfDay == 24) {
                                String EndstartHourOfDay = "00";
                                binding.endTimeValue.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                            } else {
                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                binding.endTimeValue.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                            }

                        } else {
                            binding.startTimeValue.setText("오전 " + startHourOfDay + " : " + startMiute);
                            hourOfDay += 1;
                            if (hourOfDay == 12) {
                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                binding.endTimeValue.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                            } else {
                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                binding.endTimeValue.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                            }
                        }
                    }
                });
            }
        });

        binding.endLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.startTimeValue.setTextColor(Color.parseColor("#80000000"));
                binding.startTimeName.setTextColor(Color.parseColor("#80000000"));
                binding.endTimeName.setTextColor(Color.parseColor("#FFFFFF"));
                binding.endTimeValue.setTextColor(Color.parseColor("#FFFFFF"));


                binding.timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        String endHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                        String endMiute = minute < 10 ? "0" + minute : "" + minute;
                        if (hourOfDay >= 12) {
                            binding.endTimeValue.setText("오후 " + endHourOfDay + " : " + endMiute);
                        } else {
                            binding.endTimeValue.setText("오전 " + endHourOfDay + " : " + endMiute);
                        }
                    }
                });
            }
        });



        //체크버튼(저장) 클릭시 이벤트 처리
        binding.saveCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDiary(calendar.me);
                calendar = new Calendar();
                finish();
            }
        });
        //위치 버튼 클릭시 이벤트 처리
        binding.calendarLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleMapActivity googleMapActivity = new GoogleMapActivity();
                Intent intent = new Intent(binding.getRoot().getContext(), googleMapActivity.getClass())
                        .putExtra(Constants.KEY_CALENDARDATE, calendar.date)
                        .putExtra(Constants.KEY_CALENDARDOCUMENT, calendar.documentid);
                intent.putExtra("selectLocation", GOOGLE_MAP_SELECT_LOCATION_CALENDAR);
                startActivityForResult(intent, RESULT_LOCATION);

            }
        });

        //친구 초대 버튼 클릭시 이벤트 처리
        binding.inviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarUserInviteActivity calendarUserInviteActivity = new CalendarUserInviteActivity();
                Intent intent = new Intent(binding.getRoot().getContext(), calendarUserInviteActivity.getClass())
                        .putExtra(Constants.KEY_CALENDARDATE, calendar.date)
                        .putExtra(Constants.KEY_CALENDARDOCUMENT, calendar.documentid);

                startActivityForResult(intent, RESULT_OK);
            }
        });

        //뒤로가기 버튼 클릭시 이벤트 처리
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalendarAddActivity.this, CalendarActivity.class);
                calendar = new Calendar();
                startActivity(intent);
            }
        });


        //일정삭제(휴지통) 버튼 클릭시 이벤트 처리
        binding.deleteCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                database.collection(Constants.KEY_COLLECTION_CALENDAR).document(calendar.documentid).delete();

                Intent intent = new Intent(CalendarAddActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        binding.startLayout.performClick();
    }

    //인텐트 종료 시 반환 확인
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_OK) {
            try {
                //친구 이름 띄워주기
                binding.inviteFriend.setText("  " + calendar.friend_name);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "불러오는 도중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RESULT_LOCATION) {
            try {
                //위치정보 띄워주기
                String result = ChatAdapter.getAddress(this, latLng.latitude, latLng.longitude);
                binding.calendarLocation.setText("  " + result);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "위치 정보에 오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    //친구 닉네임 찾기
    private void findName() {
        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_USERS).document(calendar.friend);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    calendar.friend_name = document.get(Constants.KEY_NAME).toString();

                }
            }
        });
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(CalendarAddActivity.this, CalendarActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //다이어리 저장 함수
    private Boolean saveDiary(String readDay) {
        String user_key = preferenceManager.getString(Constants.KEY_USER_ID);
        String user_name = preferenceManager.getString(Constants.KEY_NAME);
        String content = binding.writeCalendar.getText().toString();
        String explain = binding.calendarExplain.getText().toString();
        String[] startTime = binding.startTimeValue.getText().toString().split(" ");
        String[] endTime = binding.endTimeValue.getText().toString().split(" ");

        startTime[1] = startTime[0].equals("오전")? startTime[1] : Integer.parseInt(startTime[1]) == 12? Integer.parseInt(startTime[1])+"" : Integer.parseInt(startTime[1]) + 12 +"";
        endTime[1] = endTime[0].equals("오전")? endTime[1] :  Integer.parseInt(endTime[1]) == 12? Integer.parseInt(endTime[1])+"" : Integer.parseInt(endTime[1]) + 12 +"";


        //지도에서 위치정보를 가져왔으면 대입
        if (latLng != null) {
            calendar.lat = latLng.latitude;
            calendar.lon = latLng.longitude;
        }

        //정보를 넣기 위한 틀
        HashMap<String, Object> existcal = new HashMap<>();
        HashMap<String, Object> cal = new HashMap<>();

        //put =  파이어베이스에 데이터 저장
        cal.put(Constants.KEY_CALENDARME, user_key);
        existcal.put(Constants.KEY_MYNAME, user_name);
        cal.put(Constants.KEY_MYNAME, user_name);
        cal.put(Constants.KEY_FRIENDNAME, calendar.friend_name);
        existcal.put(Constants.KEY_FRIENDNAME, calendar.friend_name);
        cal.put(Constants.KEY_CALENDARFRIEND, calendar.friend);
        existcal.put(Constants.KEY_CALENDARFRIEND, calendar.friend);
        if (latLng != null) {
            cal.put(Constants.KEY_CALENDARLON, latLng.longitude);
            existcal.put(Constants.KEY_CALENDARLON, latLng.longitude);
            cal.put(Constants.KEY_CALENDARLAT, latLng.latitude);
            existcal.put(Constants.KEY_CALENDARLAT, latLng.latitude);
        }
        cal.put(Constants.KEY_CALENDARCONTENT, content);
        existcal.put(Constants.KEY_CALENDARCONTENT, content);
        cal.put(Constants.KEY_CALENDAR_EXPLAIN, explain);
        existcal.put(Constants.KEY_CALENDAR_EXPLAIN, explain);
        cal.put(Constants.KEY_STARTCALENDAR_HOUR, startTime[1]);
        existcal.put(Constants.KEY_STARTCALENDAR_HOUR, startTime[1]);
        cal.put(Constants.KEY_STARTCALENDAR_MINUTE, startTime[3]);
        existcal.put(Constants.KEY_STARTCALENDAR_MINUTE, startTime[3]);
        cal.put(Constants.KEY_ENDCALENDAR_HOUR, endTime[1]);
        existcal.put(Constants.KEY_ENDCALENDAR_HOUR, endTime[1]);
        cal.put(Constants.KEY_ENDCALENDAR_MINUTE, endTime[3]);
        existcal.put(Constants.KEY_ENDCALENDAR_MINUTE, endTime[3]);
        cal.put(Constants.KEY_TIMESTAMP, startTime[1]+":"+startTime[3]+":"+endTime[1]+":"+endTime[3]);
        existcal.put(Constants.KEY_TIMESTAMP, startTime[1]+":"+startTime[3]+":"+endTime[1]+":"+endTime[3]);


        cal.put(Constants.KEY_CALENDARLON, calendar.lon);
        existcal.put(Constants.KEY_CALENDARLON, calendar.lon);
        cal.put(Constants.KEY_CALENDARLAT, calendar.lat);
        existcal.put(Constants.KEY_CALENDARLAT, calendar.lat);
        cal.put(Constants.KEY_CALENDARDATE, calendar.date);
        //기존에 정보가 없다면 새 정보를 만들겠다
        if (calendar.documentid == null) {
            database.collection(Constants.KEY_COLLECTION_CALENDAR)
                    .add(cal);
            return true;
        }
        //기존에 정보가 있으면 업데이트 하겠다
        else {
            database.collection(Constants.KEY_COLLECTION_CALENDAR).document(calendar.documentid).update(existcal);
            return true;
        }
    }

}