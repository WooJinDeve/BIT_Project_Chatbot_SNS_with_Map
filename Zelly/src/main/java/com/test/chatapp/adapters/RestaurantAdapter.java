//챗봇 맛집 리스트 표현
package com.test.chatapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.databinding.ItemContainerChatbotRestaurantBinding;
import com.test.chatapp.databinding.ItemContainerUserBinding;
import com.test.chatapp.models.Restaurant;
import com.test.chatapp.models.User;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private final List<Restaurant> restaurants;
    int GOOGLEMAP_PLACE = 300;

    public RestaurantAdapter(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    //onCreate
    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerChatbotRestaurantBinding itemContainerChatbotRestaurantBinding = ItemContainerChatbotRestaurantBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RestaurantViewHolder(itemContainerChatbotRestaurantBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.setRestaurantData(restaurants.get(position));
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    //받은 정보(맛집) 표현
    class RestaurantViewHolder extends RecyclerView.ViewHolder {

        ItemContainerChatbotRestaurantBinding binding;

        RestaurantViewHolder(ItemContainerChatbotRestaurantBinding itemContainerChatbotRestaurantBinding) {
            super(itemContainerChatbotRestaurantBinding.getRoot());
            binding = itemContainerChatbotRestaurantBinding;
        }

        void setRestaurantData(Restaurant restaurant) {
            Glide.with(binding.imageView.getContext()).load(restaurant.imgSource).into(binding.imageView);
            binding.textName.setText(restaurant.name);
            binding.textrating.setText("★  " + restaurant.rating);

            binding.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(binding.getRoot().getContext(), GoogleMapActivity.class);
                    intent.putExtra("data", binding.textName.getText());
                    intent.putExtra("selectLocation", GOOGLEMAP_PLACE);
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
        }
    }
}
