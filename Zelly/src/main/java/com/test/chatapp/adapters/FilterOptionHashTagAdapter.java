//구글맵 내 마커 필터(친구)
package com.test.chatapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.R;
import com.test.chatapp.activities.RecommandPlaceActivity;
import com.test.chatapp.databinding.ItemContainerFilterOptionHashtagBinding;
import com.test.chatapp.databinding.ItemContainerHashtagBinding;

import java.util.List;

public class FilterOptionHashTagAdapter extends RecyclerView.Adapter<FilterOptionHashTagAdapter.FilterOptionViewHolder> {
    private final List<String> filterOption;

    public FilterOptionHashTagAdapter(List<String> filterOption) {
        this.filterOption = filterOption;
    }

    //onCreate 느낌
    @NonNull
    @Override
    public FilterOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFilterOptionHashtagBinding itemContainerFilterOptionHashtagBinding = ItemContainerFilterOptionHashtagBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FilterOptionViewHolder(itemContainerFilterOptionHashtagBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull FilterOptionViewHolder holder, int position) {
        holder.setTitle(filterOption.get(position));
    }

    @Override
    public int getItemCount() {
        return filterOption.size();
    }

    //받은 정보(친구) 표현
    public class FilterOptionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFilterOptionHashtagBinding binding;

        public FilterOptionViewHolder(ItemContainerFilterOptionHashtagBinding itemContainerFilterOptionHashtagBinding) {
            super(itemContainerFilterOptionHashtagBinding.getRoot());
            binding = itemContainerFilterOptionHashtagBinding;
        }

        void setTitle(String option) {
            binding.text.setText(option);

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (RecommandPlaceActivity.selectedHashTags.contains(option)){
                        binding.parentLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                        binding.text.setTextColor(Color.parseColor("#000000"));
                        RecommandPlaceActivity.selectedHashTags.remove(option);
                    } else {
                        binding.parentLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                        binding.text.setTextColor(Color.parseColor("#FFFFFF"));
                        RecommandPlaceActivity.selectedHashTags.add(option);
                    }
                }
            });
        }
    }
}
