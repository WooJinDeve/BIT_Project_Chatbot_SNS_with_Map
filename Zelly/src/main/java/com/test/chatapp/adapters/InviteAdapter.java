package com.test.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.databinding.ItemContainerUserBinding;
import com.test.chatapp.listeners.UserListener;
import com.test.chatapp.models.User;

import java.util.Base64;
import java.util.List;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public InviteAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    //onCreate
    @Override
    public InviteAdapter.InviteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new InviteAdapter.InviteViewHolder(itemContainerUserBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(InviteAdapter.InviteViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    //받은 정보 표현
    class InviteViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        InviteViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    //이미지 디코딩
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
