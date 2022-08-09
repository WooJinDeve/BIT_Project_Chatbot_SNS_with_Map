//구글맵 내 마커 필터(친구)
package com.test.chatapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.R;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.activities.PostingActivity;
import com.test.chatapp.databinding.ItemContainerHashtagBinding;

import java.util.ArrayList;

public class HashTagAdapter extends RecyclerView.Adapter<HashTagAdapter.HashTagViewHolder> {
    private final ArrayList<String> hashTag;

    public HashTagAdapter(ArrayList<String> hashTag) {
        this.hashTag = hashTag;
    }

    //onCreate 느낌
    @NonNull
    @Override
    public HashTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerHashtagBinding itemContainerHashtagBinding = ItemContainerHashtagBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HashTagViewHolder(itemContainerHashtagBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull HashTagViewHolder holder, int position) {
        holder.setHashTag(hashTag.get(position));
    }

    @Override
    public int getItemCount() {
        return hashTag.size();
    }

    //받은 정보(친구) 표현
    public class HashTagViewHolder extends RecyclerView.ViewHolder {
        ItemContainerHashtagBinding binding;

        public HashTagViewHolder(ItemContainerHashtagBinding itemContainerHashtagBinding) {
            super(itemContainerHashtagBinding.getRoot());
            binding = itemContainerHashtagBinding;
        }

        void setHashTag(String hashTag) {
            if(binding.getRoot().getContext().toString().contains("GoogleMapActivity")){
                binding.parentLayout.setBackgroundResource(R.drawable.round_background_border_white);
                binding.hashTag.setTextColor(Color.parseColor("#000000"));
            }
            binding.hashTag.setText(hashTag);

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (binding.getRoot().getContext().toString().contains("PostingActivity")) {
                        if (PostingActivity.hashTags.contains(hashTag)) {
                            binding.parentLayout.setBackgroundResource(R.drawable.round_background_border_lightgray);
                            binding.hashTag.setTextColor(Color.parseColor("#000000"));
                            PostingActivity.hashTags.remove(hashTag);
                        } else {
                            binding.parentLayout.setBackgroundResource(R.drawable.round_background_test);
                            binding.hashTag.setTextColor(Color.parseColor("#ffffff"));
                            PostingActivity.hashTags.add(hashTag);
                        }
                    }
                    else if(binding.getRoot().getContext().toString().contains("GoogleMapActivity")){
                        if(GoogleMapActivity.selectedHashTags.contains(hashTag)){
                            binding.parentLayout.setBackgroundResource(R.drawable.round_background_border_white);
                            binding.hashTag.setTextColor(Color.parseColor("#000000"));
                            GoogleMapActivity.selectedHashTags.remove(hashTag);
                        }
                        else{
                            binding.parentLayout.setBackgroundResource(R.drawable.round_background_test);
                            binding.hashTag.setTextColor(Color.parseColor("#ffffff"));
                            GoogleMapActivity.selectedHashTags.add(hashTag);
                        }
                    }
                }
            });
        }
    }
}
