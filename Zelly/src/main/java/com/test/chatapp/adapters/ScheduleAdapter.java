package com.test.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.databinding.ItemContainerCalendarBinding;
import com.test.chatapp.databinding.ItemContainerChatbotCalendarLoadBinding;
import com.test.chatapp.models.Schedule;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {


    private final List<Schedule> schedules;

    public ScheduleAdapter(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerChatbotCalendarLoadBinding itemContainerChatbotCalendarLoadBinding = ItemContainerChatbotCalendarLoadBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ScheduleViewHolder(itemContainerChatbotCalendarLoadBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.setScheduleDate(schedules.get(position));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {

        ItemContainerChatbotCalendarLoadBinding binding;

        ScheduleViewHolder(ItemContainerChatbotCalendarLoadBinding itemContainerChatbotCalendarLoadBinding) {
            super(itemContainerChatbotCalendarLoadBinding.getRoot());
            binding = itemContainerChatbotCalendarLoadBinding;
        }

        void setScheduleDate(Schedule schedule) {
            binding.hourTime.setText(schedule.calendarDate);
            binding.calendarContent.setText(schedule.calendarContent);
            binding.calendarExplain.setText(schedule.calendarExplain);
            binding.calendarFriend.setText(schedule.myName + ", " + schedule.friendName);
            binding.calendarPlace.setText(schedule.calendarLocation);
        }
    }

}
