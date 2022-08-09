package com.test.chatapp.utilities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class getAddress {
    //위도경도 주소로 변환
    static public Address getAddress(Context mContext, double lat, double lng) {
        Address nowAddr = null;
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    nowAddr = address.get(0);
                    //.getAddressLine(0)
                    //System.out.println(address.get(0).getAdminArea() + address.get(0).getSubLocality() + address.get(0).getThoroughfare()); //대전광역시 동구 자양동
                }
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddr;
    }


    static public String getDetailAddress(Context mContext, double lat, double lng) {
        Address nowAddr = null;
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;

        String adminArea = null;
        String locality = null;
        String subLocality = null;
        String thoroughfare = null;
        String fullAddress = null;

        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lat, lng, 1);
                if (address != null && address.size() > 0) {
                    nowAddr = address.get(0);
                    //.getAddressLine(0)
                    try {
                        locality = address.get(0).getLocality();
                    } catch (Exception exception) {
                        locality = null;
                    }
                    finally {
                        adminArea = address.get(0).getAdminArea();
                        subLocality = address.get(0).getSubLocality();
                        thoroughfare = address.get(0).getThoroughfare();

                        fullAddress = adminArea + "/" + locality + "/" + subLocality + "/" + thoroughfare;   //대전광역시 동구 자양동
                    }
                }
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return fullAddress;
    }
}
