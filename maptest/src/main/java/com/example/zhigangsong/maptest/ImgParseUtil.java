package com.example.zhigangsong.maptest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhigang.song on 2016/3/29.
 */
public class ImgParseUtil {
    public static List<ImgBean> parse(JSONObject jsonObject) {
        List<ImgBean> imgBeans = new ArrayList<>();
        if (jsonObject.optString("status").equals("ok")) {
            JSONArray array = jsonObject.optJSONArray("radar_img");
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONArray array1 = array.getJSONArray(i);
                    JSONArray array2 = array1.getJSONArray(2);
                    imgBeans.add(new ImgBean(array1.optString(0),array1.optDouble(1),array2.getDouble(0),array2.getDouble(1),array2.getDouble(2),array2.getDouble(3)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return imgBeans;
    }
}
