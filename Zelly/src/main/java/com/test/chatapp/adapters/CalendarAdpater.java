//캘린더액티비티 내 일정 출력
package com.test.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.databinding.ItemContainerCalendarBinding;
import com.test.chatapp.listeners.CalendarListener;
import com.test.chatapp.models.Calendar;

import java.util.List;

public class CalendarAdpater extends RecyclerView.Adapter<CalendarAdpater.CalendarViewHolder> {

    private final List<Calendar> calendars;
    private CalendarListener calendarListener;

    //캘린더 어댑터 생성자
    public CalendarAdpater(List<Calendar> calendars, CalendarListener calendarListener) {
        this.calendars = calendars;
        this.calendarListener = calendarListener;
    }

    //Activity 의 onCreate 느낌?
    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerCalendarBinding itemContainerCalendarBinding = ItemContainerCalendarBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalendarViewHolder(itemContainerCalendarBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.setCalendarData(calendars.get(position));
    }

    //가지고 있는 아이템 갯수(사이즈) 저장
    @Override
    public int getItemCount() {
        return calendars.size();
    }

    //받은 정보를 표현 하는 클래스
    class CalendarViewHolder extends RecyclerView.ViewHolder {
        ItemContainerCalendarBinding binding;

        CalendarViewHolder(ItemContainerCalendarBinding itemContainerCalendarBinding) {
            super(itemContainerCalendarBinding.getRoot());
            binding = itemContainerCalendarBinding;
        }

        void setCalendarData(Calendar calendar) {
            binding.calendarContent.setText(calendar.message);
            binding.calendarFriend.setText(calendar.friend_name);
            binding.calendarExplain.setText(calendar.explain);
            binding.hourTime.setText(calendar.starthour + ':' + calendar.startminute);
            String place = ChatAdapter.getAddress(binding.getRoot().getContext(), calendar.lat, calendar.lon);
            binding.calendarPlace.setText(place);
            binding.getRoot().setOnClickListener(v -> calendarListener.onCalendarClicked(calendar));
        }

    }
}
