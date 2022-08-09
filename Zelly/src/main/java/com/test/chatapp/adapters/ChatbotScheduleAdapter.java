package com.test.chatapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.chatapp.R;
import com.test.chatapp.databinding.LayoutBottomRecyclerviewItemBinding;
import com.test.chatapp.models.UserSchedule;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatbotScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<UserSchedule> userSchedules;
    private final ChatbotScheduleAdapter chatbotScheduleAdapter = this;
    private Context context;

    public ChatbotScheduleAdapter(List<UserSchedule> userSchedules, Context context) {
        this.userSchedules = userSchedules;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutBottomRecyclerviewItemBinding layoutBottomRecyclerviewItemBinding = LayoutBottomRecyclerviewItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(layoutBottomRecyclerviewItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setData(userSchedules.get(position));
    }

    @Override
    public int getItemCount() {
        return userSchedules.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        LayoutBottomRecyclerviewItemBinding binding;
        private FirebaseFirestore database = FirebaseFirestore.getInstance();
        private PreferenceManager preferenceManager;

        ViewHolder(LayoutBottomRecyclerviewItemBinding layoutBottomRecyclerviewItemBinding) {
            super(layoutBottomRecyclerviewItemBinding.getRoot());
            binding = layoutBottomRecyclerviewItemBinding;
        }

        void setData(UserSchedule userSchedule) {
            preferenceManager = new PreferenceManager((binding.getRoot().getContext()));
            String[] time = timeParse(userSchedule.time);
            int st = Integer.parseInt(time[0].trim()), et = Integer.parseInt(time[2].trim());
            String s = st < 10 ? "0" + st : String.valueOf(st), e = et < 10 ? "0" + et : String.valueOf(et);
            if (st == 12) s = String.valueOf(st);
            if (et == 12) s = String.valueOf(et);
            if (st > 12) {
                st -= 12;
                s = "0" + st;
            }
            if (et > 12) {
                et -= 12;
                e = "0" + et;
            }
            binding.StartScheduleTime.setText(s + ":" + time[1]);
            binding.EndScheduleTime.setText(e + ":" + time[3]);
            binding.StartScheduleText.setText(time[4]);
            binding.EndScheduleText.setText(time[5]);

            TextView[] dateText = new TextView[7];
            TextView[] dateDays = new TextView[7];
            dateText[0] = binding.dateText0;
            dateText[1] = binding.dateText1;
            dateText[2] = binding.dateText2;
            dateText[3] = binding.dateText3;
            dateText[4] = binding.dateText4;
            dateText[5] = binding.dateText5;
            dateText[6] = binding.dateText6;
            dateDays[0] = binding.dateDat0;
            dateDays[1] = binding.dateDat1;
            dateDays[2] = binding.dateDat2;
            dateDays[3] = binding.dateDat3;
            dateDays[4] = binding.dateDat4;
            dateDays[5] = binding.dateDat5;
            dateDays[6] = binding.dateDat6;

            for (int i = 0; i < userSchedule.days.length; i++) {
                if (userSchedule.days[i].equals("1")) {
                    if (userSchedule.state == false) {
                        dateDays[i].setTextColor(Color.parseColor("#e9e9e9"));
                        dateText[i].setTextColor(Color.parseColor("#e9e9e9"));
                    } else {
                        dateDays[i].setTextColor(Color.parseColor("#288CFF"));
                        dateText[i].setTextColor(Color.parseColor("#288CFF"));
                    }
                } else
                    dateText[i].setTextColor(Color.parseColor("#e9e9e9"));
            }

            if (userSchedule.state == true)
                binding.scheduleButton.setChecked(true);

            binding.scheduleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                .collection(Constants.KEY_SCHEDULE)
                                .document(userSchedule.document)
                                .update(Constants.KEY_STATE, true);
                        for (int i = 0; i < userSchedule.days.length; i++) {
                            if (userSchedule.days[i].equals("1")) {
                                dateDays[i].setTextColor(Color.parseColor("#288CFF"));
                                dateText[i].setTextColor(Color.parseColor("#288CFF"));
                            }
                        }
                    } else {
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                .collection(Constants.KEY_SCHEDULE)
                                .document(userSchedule.document)
                                .update(Constants.KEY_STATE, false);
                        for (int i = 0; i < userSchedule.days.length; i++) {
                            if (userSchedule.days[i].equals("1"))
                                dateDays[i].setTextColor(Color.parseColor("#e9e9e9"));
                            dateText[i].setTextColor(Color.parseColor("#e9e9e9"));
                        }
                    }
                }
            });
            binding.deletesch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(preferenceManager.getString(Constants.KEY_USER_ID))
                            .collection(Constants.KEY_SCHEDULE)
                            .document(userSchedule.document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    for (int i = 0; i < userSchedules.size(); i++) {
                                        if (userSchedule.document.equals(userSchedules.get(i).document)) {
                                            userSchedules.remove(i);
                                            chatbotScheduleAdapter.notifyItemRemoved(i);
                                        }


                                    }
                                }
                            });
                }
            });
            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    binding.deletesch.setVisibility(View.VISIBLE);
                    return true;
                }
            });

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                            context, R.style.BottomSheetDialogTheme
                    );
                    View bottomSheetView = LayoutInflater.from(context).inflate(
                            R.layout.layout_bottom_sheet, binding.getRoot().findViewById(R.id.bottomSheetContainer)
                    );
                    Button[] buttons = new Button[7];
                    buttons[0] = bottomSheetView.findViewById(R.id.btn0);
                    buttons[1] = bottomSheetView.findViewById(R.id.btn1);
                    buttons[2] = bottomSheetView.findViewById(R.id.btn2);
                    buttons[3] = bottomSheetView.findViewById(R.id.btn3);
                    buttons[4] = bottomSheetView.findViewById(R.id.btn4);
                    buttons[5] = bottomSheetView.findViewById(R.id.btn5);
                    buttons[6] = bottomSheetView.findViewById(R.id.btn6);

                    TimePicker timePicker = bottomSheetView.findViewById(R.id.timePicker);
                    TextView Starttime = bottomSheetView.findViewById(R.id.startTimeValue);
                    TextView StartNTimeName = bottomSheetView.findViewById(R.id.startTimeName);
                    TextView Endtime = bottomSheetView.findViewById(R.id.endTimeValue);
                    TextView EndtimeName = bottomSheetView.findViewById(R.id.endTimeName);
                    Switch repeat = bottomSheetView.findViewById(R.id.timeSwitch);
                    Starttime.setText(time[4] + " " + time[0] + " : " + time[1]);
                    Endtime.setText(time[5] + " " + time[2] + " : " + time[3]);
                    if (userSchedule.repeat) {
                        repeat.setChecked(true);
                    }
                    repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                userSchedule.repeat = true;
                            } else {
                                userSchedule.repeat = false;
                            }

                        }
                    });
                    //타임피커 시작시간으로 설정
                    Starttime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StartNTimeName.setTextColor(Color.parseColor("#FFFFFF"));
                            Starttime.setTextColor(Color.parseColor("#FFFFFF"));
                            EndtimeName.setTextColor(Color.parseColor("#80000000"));
                            Endtime.setTextColor(Color.parseColor("#80000000"));

                            Starttime.setBackgroundResource(R.drawable.round_background_test);
                            Endtime.setBackgroundResource(0);
                            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                                @Override
                                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                    String startHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                    String startMiute = minute < 10 ? "0" + minute : "" + minute;
                                    if (hourOfDay >= 12) {
                                        Starttime.setText("오후 " + startHourOfDay + " : " + startMiute);
                                        hourOfDay += 1;
                                        if (hourOfDay == 24) {
                                            String EndstartHourOfDay = "00";
                                            Endtime.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                                        } else {
                                            String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                            Endtime.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                                        }

                                    } else {
                                        Starttime.setText("오전 " + startHourOfDay + " : " + startMiute);
                                        hourOfDay += 1;
                                        if (hourOfDay == 12) {
                                            String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                            Endtime.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                                        } else {
                                            String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                            Endtime.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                                        }


                                    }
                                }
                            });
                        }
                    });
                    //타임피커 종료시간 설정
                    Endtime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StartNTimeName.setTextColor(Color.parseColor("#80000000"));
                            Starttime.setTextColor(Color.parseColor("#80000000"));
                            EndtimeName.setTextColor(Color.parseColor("#FFFFFF"));
                            Endtime.setTextColor(Color.parseColor("#FFFFFF"));

                            Starttime.setBackgroundResource(R.drawable.round_background_test);
                            Endtime.setBackgroundResource(0);
                            timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                                @Override
                                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                    String endHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                    String endMiute = minute < 10 ? "0" + minute : "" + minute;
                                    if (hourOfDay >= 12) {
                                        Endtime.setText("오후 " + endHourOfDay + " : " + endMiute);
                                    } else {
                                        Endtime.setText("오전 " + endHourOfDay + " : " + endMiute);
                                    }
                                }
                            });
                        }
                    });
                    for (int i = 0; i < 7; i++) {
                        if (userSchedule.days[i].equals("1")) {
                            buttons[i].setBackgroundResource(R.drawable.shadow_round);
                        } else {
                            buttons[i].setBackgroundResource(0);
                        }


                        int finalI = i;
                        buttons[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (userSchedule.days[finalI].equals("1")) {
                                    userSchedule.days[finalI] = "0";
                                    buttons[finalI].setBackgroundResource(0);
                                } else {
                                    userSchedule.days[finalI] = "1";
                                    buttons[finalI].setBackgroundResource(R.drawable.shadow_round);
                                }
                            }
                        });
                    }
                    bottomSheetView.findViewById(R.id.changebutton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            userSchedule.state = true;
                            userSchedule.time = timeParse2(Starttime.getText().toString(), Endtime.getText().toString());
                            HashMap<String, Object> schedule = new HashMap<>();
                            schedule.put(Constants.KEY_SCHEDULE_REPEAT, userSchedule.repeat);
                            schedule.put(Constants.KEY_SCHEDULE_DAYS, Arrays.asList(userSchedule.days));
                            schedule.put(Constants.KEY_STATE, userSchedule.state);
                            schedule.put(Constants.KEY_TIMESTAMP, userSchedule.time);
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                    .collection(Constants.KEY_SCHEDULE)
                                    .document(userSchedule.document)
                                    .update(schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            for (int i = 0; i < userSchedules.size(); i++) {
                                                if (userSchedule.document.equals(userSchedules.get(i).document)) {
                                                    userSchedules.get(i).days = userSchedule.days;
                                                    userSchedules.get(i).repeat = userSchedule.repeat;
                                                    userSchedules.get(i).state = userSchedule.state;
                                                    userSchedules.get(i).time = userSchedule.time;
                                                    chatbotScheduleAdapter.notifyItemChanged(i);
                                                }


                                            }

                                        }
                                    });
                            bottomSheetDialog.dismiss();

                        }
                    });

                    bottomSheetView.findViewById(R.id.canclebutton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });

                    Starttime.performClick();
                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();
                }
            });
        }
    }

    private String timeParse2(String start, String end) {
        String[] stemp = start.split(" ");
        String[] etemp = end.split(" ");
        if (stemp[0].equals("오후")) {
            if (Integer.parseInt(stemp[1]) == 12)
                stemp[1] = "12";
            else if (Integer.parseInt(stemp[1]) > 23) {
                stemp[1] = String.valueOf(Integer.parseInt(stemp[1]) - 12);
            } else if (Integer.parseInt(stemp[1]) < 12)
                stemp[1] = String.valueOf(Integer.parseInt(stemp[1]) + 12);
        }
        if (etemp[0].equals("오후")) {
            if (Integer.parseInt(etemp[1]) == 12)
                etemp[1] = "12";
            else if (Integer.parseInt(etemp[1]) > 23) {
                etemp[1] = String.valueOf(Integer.parseInt(etemp[1]) - 12);
            } else if (Integer.parseInt(etemp[1]) < 12)
                etemp[1] = String.valueOf(Integer.parseInt(etemp[1]) + 12);
        }
        return stemp[1] + ":" + stemp[3] + ":" + etemp[1] + ":" + etemp[3];
    }

    private String[] timeParse(String time) {

        String[] newtime = {"00", "00", "00", "00", "오전", "오전"};

        if (time == null)
            return newtime;


        String[] timeparse = time.split(":"); // [0] 시작시간 [1] 시작분 [2] 오후 [3] 종료시간 [4] 오전  [5] 종료분
        newtime = new String[6];

        for (int i = 0; i < timeparse.length; i++)
            newtime[i] = timeparse[i];

        newtime[4] = Integer.parseInt(newtime[0]) >= 12 ? "오후" : "오전";
        newtime[5] = Integer.parseInt(newtime[2]) >= 12 ? "오후" : "오전";
        return newtime;
    }

}
