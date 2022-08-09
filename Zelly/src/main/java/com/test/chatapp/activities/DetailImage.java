//DetailImage =  채팅방 이미지를 클릭 했을 때 큰 화면으로 띄워줌

package com.test.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.test.chatapp.R;

public class DetailImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);

        ImageView imageview = (ImageView) findViewById(R.id.detailImage);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageview.setImageBitmap(bitmap);
    }
}