//포스팅에 달린 댓글 출력
package com.test.chatapp.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.databinding.ItemContainerChatbotRestaurantBinding;
import com.test.chatapp.databinding.ItemContainerFeedMessageBinding;
import com.test.chatapp.models.Restaurant;

import java.util.Base64;
import java.util.List;

public class PostUserMessageAdapter extends RecyclerView.Adapter<PostUserMessageAdapter.PostUserMessageViewHolder> {

    private final List<String> postUserMessages;

    public PostUserMessageAdapter(List<String> postUserMessages) {
        this.postUserMessages = postUserMessages;
    }


    //onCreate
    @NonNull
    @Override
    public PostUserMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFeedMessageBinding itemContainerFeedMessageBinding = ItemContainerFeedMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new PostUserMessageViewHolder(itemContainerFeedMessageBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull PostUserMessageViewHolder holder, int position) {
        holder.setPostUserMessageData(postUserMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return postUserMessages.size();
    }

    //받은 정보(포스팅 댓글) 표현
    class PostUserMessageViewHolder extends RecyclerView.ViewHolder {

        ItemContainerFeedMessageBinding binding;

        PostUserMessageViewHolder(ItemContainerFeedMessageBinding itemContainerFeedMessageBinding) {
            super(itemContainerFeedMessageBinding.getRoot());
            binding = itemContainerFeedMessageBinding;
        }

        void setPostUserMessageData(String userMessageData) {
            String[] data = userMessageData.split("#");
            binding.userProfile.setImageBitmap(getUserImage(data[1]));
            binding.userName.setText(data[0]);
            binding.userMessage.setText(data[2]);
            binding.messageDatetime.setText(data[3]);
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}


