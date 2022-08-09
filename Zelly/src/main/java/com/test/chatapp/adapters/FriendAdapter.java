//구글맵 내 마커 필터(친구)
package com.test.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.chatapp.R;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.databinding.ItemContainerFriendBigBinding;
import com.test.chatapp.databinding.ItemContainerFriendBinding;
import com.test.chatapp.models.User;

import java.util.Base64;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<User> friends;
    public static final int GOOGLEMAP_FRIEND_LIST = 1;
    public static final int BOTTOMSHEET_FRIEND_LIST = 2;

    public FriendAdapter(List<User> friends) {
        this.friends = friends;
    }

    //onCreate 느낌
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GOOGLEMAP_FRIEND_LIST) {
            return new FriendAdapter.GoogleMapFriendViewHolder(
                    ItemContainerFriendBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new FriendAdapter.BottomSheetFriendViewHolder(
                    ItemContainerFriendBigBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    //타입설정
    @Override
    public int getItemViewType(int position) {
        if (friends.get(position).position == GOOGLEMAP_FRIEND_LIST) {
            return GOOGLEMAP_FRIEND_LIST;
        } else {
            return BOTTOMSHEET_FRIEND_LIST;
        }
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == GOOGLEMAP_FRIEND_LIST) {
            ((FriendAdapter.GoogleMapFriendViewHolder) holder).setData(friends.get(position));
        } else {
            ((FriendAdapter.BottomSheetFriendViewHolder) holder).setData(friends.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    //받은 정보(친구) 표현
    public class GoogleMapFriendViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendBinding binding;

        public GoogleMapFriendViewHolder(ItemContainerFriendBinding itemContainerFriendBinding) {
            super(itemContainerFriendBinding.getRoot());
            binding = itemContainerFriendBinding;
        }

        void setData(User friend) {
            binding.textName.setText(friend.name);
            binding.friendImage.setImageBitmap(getUserImage(friend.image));

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GoogleMapActivity.selectedFriends.contains(friend)) {
                        binding.parentLayout.setBackgroundResource(R.drawable.round_background_border_purple_nostroke);
                        binding.textName.setTextColor(Color.parseColor("#000000"));
                        GoogleMapActivity.selectedFriends.remove(friend);
                    } else {
                        binding.parentLayout.setBackgroundResource(R.drawable.round_background_test);
                        binding.textName.setTextColor(Color.parseColor("#ffffff"));
                        GoogleMapActivity.selectedFriends.add(friend);
                    }
                }
            });
        }

        private Bitmap getUserImage(String encodedImage) {
            try {
                byte[] bytes = Base64.getDecoder().decode(encodedImage);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }
    }

    //받은 정보(친구) 표현
    public class BottomSheetFriendViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendBigBinding binding;

        public BottomSheetFriendViewHolder(@NonNull ItemContainerFriendBigBinding itemContainerFriendBigBinding) {
            super(itemContainerFriendBigBinding.getRoot());
            binding = itemContainerFriendBigBinding;
        }

        void setData(User friend) {
            binding.textName.setText(friend.name);
            binding.friendImage.setImageBitmap(getUserImage(friend.image));

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GoogleMapActivity.selectedFriends.contains(friend)) {
                        binding.parentLayout.setBackgroundResource(R.drawable.round_background_white);
                        binding.textName.setTextColor(Color.parseColor("#000000"));
                        GoogleMapActivity.selectedFriends.remove(friend);
                    } else {
                        binding.parentLayout.setBackgroundResource(R.drawable.round_background_blue);
                        binding.textName.setTextColor(Color.parseColor("#ffffff"));
                        GoogleMapActivity.selectedFriends.add(friend);
                    }
                }
            });
        }

        private Bitmap getUserImage(String encodedImage) {
            try {
                byte[] bytes = Base64.getDecoder().decode(encodedImage);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }
    }
}
