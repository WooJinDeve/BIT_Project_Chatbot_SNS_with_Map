//구글맵 내 마커 필터(친구)
package com.test.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.test.chatapp.R;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.activities.RecommandPlaceActivity;
import com.test.chatapp.databinding.ItemContainerFilterOptionHashtagBinding;
import com.test.chatapp.databinding.ItemContainerRecommendPlaceBinding;
import com.test.chatapp.models.RecommendPlace;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.Base64;
import java.util.List;

public class RecommendPlaceAdapter extends RecyclerView.Adapter<RecommendPlaceAdapter.FilterOptionViewHolder> {
    private final List<RecommendPlace> recommendPlaces;

    public RecommendPlaceAdapter(List<RecommendPlace> recommendPlaces) {
        this.recommendPlaces = recommendPlaces;
    }

    //onCreate 느낌
    @NonNull
    @Override
    public FilterOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerRecommendPlaceBinding itemContainerRecommendPlaceBinding = ItemContainerRecommendPlaceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FilterOptionViewHolder(itemContainerRecommendPlaceBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull FilterOptionViewHolder holder, int position) {
        holder.setTitle(recommendPlaces.get(position));
    }

    @Override
    public int getItemCount() {
        return recommendPlaces.size();
    }

    //받은 정보(친구) 표현
    public class FilterOptionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecommendPlaceBinding binding;

        public FilterOptionViewHolder(ItemContainerRecommendPlaceBinding itemContainerRecommendPlaceBinding) {
            super(itemContainerRecommendPlaceBinding.getRoot());
            binding = itemContainerRecommendPlaceBinding;
        }

        void setTitle(RecommendPlace option) {
            firebaseImageLoader(binding.imageView.getContext(), option.image, binding.imageView);
        }
    }

    //파이어베이스 스토리지에서 이미지 원본 가져오기 + 표현
    private static void firebaseImageLoader(Context context, String Path, ImageView image) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Glide.with(context).load(uri).into(image);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }
}
