//챗봇 명소 출력 리스트
package com.test.chatapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.test.chatapp.activities.GoogleMapActivity;
import com.test.chatapp.databinding.ItemContainerChatbotPlaceBinding;
import com.test.chatapp.databinding.ItemContainerChatbotRestaurantBinding;
import com.test.chatapp.models.Place;
import com.test.chatapp.models.Restaurant;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private final List<Place> places;
    int GOOGLEMAP_PLACE = 300;


    public PlaceAdapter(List<Place> places) {
        this.places = places;
    }

    //onCreate
    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerChatbotPlaceBinding itemContainerChatbotPlaceBinding = ItemContainerChatbotPlaceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new PlaceViewHolder(itemContainerChatbotPlaceBinding);
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.setPlacetData(places.get(position));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    //받은 정보(장소) 표현
    class PlaceViewHolder extends RecyclerView.ViewHolder {

        ItemContainerChatbotPlaceBinding binding;

        PlaceViewHolder(ItemContainerChatbotPlaceBinding itemContainerChatbotPlaceBinding) {
            super(itemContainerChatbotPlaceBinding.getRoot());
            binding = itemContainerChatbotPlaceBinding;
        }

        void setPlacetData(Place place) {
            Glide.with(binding.imageView.getContext()).load(place.image).into(binding.imageView);
            binding.textName.setText(place.name);
            binding.textCategory.setText(place.category);
            binding.textAddress.setText(place.address);

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
