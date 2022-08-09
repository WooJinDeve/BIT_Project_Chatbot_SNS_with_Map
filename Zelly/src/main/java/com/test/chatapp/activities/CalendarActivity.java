//CalendarActivity = 캘린더 액티비티 (달력, 일정 추가)

package com.test.chatapp.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.test.chatapp.adapters.CalendarAdpater;
import com.test.chatapp.databinding.ActivityCalendarBinding;
import com.test.chatapp.listeners.CalendarListener;
import com.test.chatapp.models.Calendar;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class CalendarActivity extends AppCompatActivity implements CalendarListener {
    private static ActivityCalendarBinding binding;
    public String readDay;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    Double gmLat; //구글 맵에서 가져온 좌표
    Double gmLon; //구글 맵에서 가져온 좌표
    private List<String> contexts;  //파이어베이스 문서이름
    private List<Calendar> calendars; //캘린더 관련 정보
    public static long startDate;

    //초기화 함수
    public void init() {
        calendars = new ArrayList<>();
        contexts = new ArrayList<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            gmLat = getIntent.getExtras().getDouble("Lat");
            gmLon = getIntent.getExtras().getDouble("Lon");
        }
        LocalDate now = LocalDate.now();
        checkDay(now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(CalendarActivity.this, MainActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        listener();
    }

    //클릭이벤트 처리 관련 함수
    private void listener() {
        binding.calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.DialogFragment dialogfragment = new DatePickerDialogTheme();
                dialogfragment.show(getFragmentManager(), "picker");
            }
        });
        //일정추가(+)버튼 클릭시 CalendarAddActivity로 전환
        binding.newCal.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), CalendarAddActivity.class)
                .putExtra(Constants.KEY_CALENDARDATE, readDay)  //putExtra = 정보 넘겨주기
                .putExtra(Constants.KEY_CALENDARLAT, gmLat) // 날짜,위치 정보 넘기기
                .putExtra(Constants.KEY_CALENDARLON, gmLon)));

        //달력 내 날짜 클릭시 날짜 정보 가져오는 함수
        binding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                //binding.diaryTextView.setVisibility(View.VISIBLE);
                //binding.diaryTextView.setText(String.format("%d. %d", month + 1, dayOfMonth));
                checkDay(year, month + 1, dayOfMonth);
            }
        });

        //뒤로가기 버튼 클릭시 이벤트 처리
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }


    //DB에서 날짜 관련 정보 가져오는 함수
    public void checkDay(int cYear, int cMonth, int cDay) {
        String Month = cMonth <= 10 ? "0" + cMonth : cMonth + "";
        String Day = cDay <= 10 ? "0" + cDay : cDay + "";
        readDay = "" + cYear + "-" + (Month) + "" + "-" + Day;    //날짜 읽기
        calendars.clear();  //캘린더 초기화
        String user_key = preferenceManager.getString(Constants.KEY_USER_ID);
        database.collection(Constants.KEY_COLLECTION_CALENDAR)
                .whereEqualTo(Constants.KEY_CALENDARME, user_key)
                .whereEqualTo(Constants.KEY_CALENDARDATE, readDay)
                .get()
                .addOnCompleteListener(task -> {
                    //for문 = 일정 관련 모든 정보를 가져오기
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Calendar calendar = new Calendar();
                        calendar.message = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARCONTENT));
                        calendar.friend = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARFRIEND));
                        calendar.friend_name = (queryDocumentSnapshot.getString(Constants.KEY_FRIENDNAME));
                        calendar.my_name = (queryDocumentSnapshot.getString(Constants.KEY_MYNAME));
                        calendar.date = readDay;
                        calendar.me = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARME));
                        calendar.lat = (queryDocumentSnapshot.getDouble(Constants.KEY_CALENDARLAT));
                        calendar.lon = (queryDocumentSnapshot.getDouble(Constants.KEY_CALENDARLON));
                        calendar.explain = (queryDocumentSnapshot.getString(Constants.KEY_CALENDAR_EXPLAIN));
                        calendar.startminute = (queryDocumentSnapshot.getString(Constants.KEY_STARTCALENDAR_MINUTE));
                        calendar.starthour = (queryDocumentSnapshot.getString(Constants.KEY_STARTCALENDAR_HOUR));
                        calendar.endminute = (queryDocumentSnapshot.getString(Constants.KEY_ENDCALENDAR_MINUTE));
                        calendar.endhour = (queryDocumentSnapshot.getString(Constants.KEY_ENDCALENDAR_HOUR));

                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        calendar.documentid = documentSnapshot.getId();
                        calendars.add(calendar);
                    }

                    //캘린더 정보 시간 순으로 정렬
                    Collections.sort(calendars, new Comparator<Calendar>() {
                        @Override
                        public int compare(Calendar o1, Calendar o2) {
                            return o1.getParseIntToHour() - o2.getParseIntToHour();
                        }
                    });

                    //모든 정보를 어댑터에 넣어서 띄워주는 부분
                    CalendarAdpater calendarAdpater = new CalendarAdpater(calendars, CalendarActivity.this);
                    binding.calendarRecyclerView.setVisibility(View.VISIBLE);
                    binding.calendarRecyclerView.setAdapter(calendarAdpater);
                    binding.progressBar.setVisibility(View.GONE);

                });
        database.collection(Constants.KEY_COLLECTION_CALENDAR)
                .whereEqualTo(Constants.KEY_CALENDARFRIEND, user_key)
                .whereEqualTo(Constants.KEY_CALENDARDATE, readDay)
                .get()
                .addOnCompleteListener(task -> {
                    //for문 = 일정 관련 모든 정보를 가져오기
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Calendar calendar = new Calendar();
                        calendar.message = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARCONTENT));
                        calendar.friend = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARFRIEND));
                        calendar.friend_name = (queryDocumentSnapshot.getString(Constants.KEY_FRIENDNAME));
                        calendar.my_name = (queryDocumentSnapshot.getString(Constants.KEY_MYNAME));
                        calendar.date = readDay;
                        calendar.me = (queryDocumentSnapshot.getString(Constants.KEY_CALENDARME));
                        calendar.lat = (queryDocumentSnapshot.getDouble(Constants.KEY_CALENDARLAT));
                        calendar.lon = (queryDocumentSnapshot.getDouble(Constants.KEY_CALENDARLON));
                        calendar.explain = (queryDocumentSnapshot.getString(Constants.KEY_CALENDAR_EXPLAIN));
                        calendar.startminute = (queryDocumentSnapshot.getString(Constants.KEY_STARTCALENDAR_MINUTE));
                        calendar.starthour = (queryDocumentSnapshot.getString(Constants.KEY_STARTCALENDAR_HOUR));
                        calendar.endminute = (queryDocumentSnapshot.getString(Constants.KEY_ENDCALENDAR_MINUTE));
                        calendar.endhour = (queryDocumentSnapshot.getString(Constants.KEY_ENDCALENDAR_HOUR));

                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        calendar.documentid = documentSnapshot.getId();
                        calendars.add(calendar);
                    }

                    //캘린더 정보 시간 순으로 정렬
                    Collections.sort(calendars, new Comparator<Calendar>() {
                        @Override
                        public int compare(Calendar o1, Calendar o2) {
                            return o1.getParseIntToHour() - o2.getParseIntToHour();
                        }
                    });

                    //모든 정보를 어댑터에 넣어서 띄워주는 부분
                    CalendarAdpater calendarAdpater = new CalendarAdpater(calendars, CalendarActivity.this);
                    binding.calendarRecyclerView.setVisibility(View.VISIBLE);
                    binding.calendarRecyclerView.setAdapter(calendarAdpater);
                    binding.progressBar.setVisibility(View.GONE);

                });
    }


    //저장한 일정 클릭시 ->CalendarAddActivity에 관련 정보 호출
    @Override
    public void onCalendarClicked(Calendar calendar) {

        Intent intent = new Intent(getApplicationContext(), CalendarAddActivity.class);

        intent.putExtra(Constants.KEY_CALENDARFRIEND, calendar.friend);
        intent.putExtra(Constants.KEY_CALENDARDOCUMENT, calendar.documentid);
        intent.putExtra(Constants.KEY_CALENDARDATE, calendar.date);
        intent.putExtra(Constants.KEY_CALENDARLAT, calendar.lat);
        intent.putExtra(Constants.KEY_CALENDARLON, calendar.lon);
        intent.putExtra(Constants.KEY_FRIENDNAME, calendar.friend_name);
        intent.putExtra(Constants.KEY_CALENDARME, calendar.me);
        intent.putExtra(Constants.KEY_MYNAME, calendar.my_name);
        intent.putExtra(Constants.KEY_CALENDARCONTENT, calendar.message);
        intent.putExtra(Constants.KEY_CALENDAR_EXPLAIN, calendar.explain);
        intent.putExtra(Constants.KEY_STARTCALENDAR_HOUR, calendar.starthour);
        intent.putExtra(Constants.KEY_STARTCALENDAR_MINUTE, calendar.startminute);
        intent.putExtra(Constants.KEY_ENDCALENDAR_HOUR, calendar.endhour);
        intent.putExtra(Constants.KEY_ENDCALENDAR_MINUTE, calendar.endminute);
        startActivity(intent);
        finish();
    }

    //데이트 피커 값받기
    public static class DatePickerDialogTheme extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        //데이트 피커 값받기
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            DatePickerDialog datepickerdialog = new DatePickerDialog(getActivity(),
                    AlertDialog.THEME_HOLO_LIGHT, this, year, month, day);

            return datepickerdialog;
        }

        //날짜 long형으로 바꾸는 코드
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String dateString = dayOfMonth + "/" + (month + 1) + "/" + year;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date();
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            CalendarActivity.startDate = date.getTime();
            binding.calendarView.setDate(startDate);
        }
    }
}