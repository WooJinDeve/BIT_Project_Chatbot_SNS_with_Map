//유저 정보 표현
package com.test.chatapp.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.activities.UserActivity;
import com.test.chatapp.databinding.ItemContainerUserBinding;
import com.test.chatapp.databinding.ItemContainerUserResponseBinding;
import com.test.chatapp.listeners.UserListener;
import com.test.chatapp.models.Calendar;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.Base64;
import java.util.Date;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<User> users;
    private final UserListener userListener;


    public static final int VIEW_TYPE_FRIEND = 1;
    public static final int VIEW_TYPE_REQUEST_FRIEND = 2;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    //onCreate
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_FRIEND){
            return new UsersAdapter.UserViewHolder(
                    ItemContainerUserBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else return new RequestViewHolder(
                ItemContainerUserResponseBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_FRIEND) {
            ((UserViewHolder) holder).setUserData(users.get(position));
        } else {
            ((RequestViewHolder) holder).setUserData(users.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public int getItemViewType(int position){
        if (users.get(position).state.equals("true")) {
            return VIEW_TYPE_FRIEND;
        } else {
            return VIEW_TYPE_REQUEST_FRIEND;
        }
    }

    //받은 정보 표현
    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
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

    class RequestViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserResponseBinding binding;
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        RequestViewHolder(ItemContainerUserResponseBinding itemContainerUserResponseBinding) {
            super(itemContainerUserResponseBinding.getRoot());
            binding = itemContainerUserResponseBinding;
        }

        void setUserData(User user) {
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);


            binding.friendTure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(user.receiveId)
                            .collection(Constants.KEY_FRIEND)
                            .document(user.id)
                            .update(Constants.KEY_STATE,"true")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(user.id)
                                            .collection(Constants.KEY_FRIEND)
                                            .document(user.receiveId)
                                            .update(Constants.KEY_STATE,"true")
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Intent intent = new Intent(binding.getRoot().getContext(), UserActivity.class);
                                                    binding.getRoot().getContext().startActivity(intent);
                                                }
                                            });
                                }
                            });
                }
            });

            binding.friendFalse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .document(user.receiveId)
                            .collection(Constants.KEY_FRIEND)
                            .document(user.id)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.collection(Constants.KEY_COLLECTION_USERS)
                                            .document(user.id)
                                            .collection(Constants.KEY_FRIEND)
                                            .document(user.receiveId)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Intent intent = new Intent(binding.getRoot().getContext(), UserActivity.class);
                                                    binding.getRoot().getContext().startActivity(intent);
                                                }
                                            });
                                }
                            });
                }
            });
        }
    }




    //이미지 디코딩
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
