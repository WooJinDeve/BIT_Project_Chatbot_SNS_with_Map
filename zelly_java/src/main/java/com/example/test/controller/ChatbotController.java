package com.example.test.controller;

import funtion.PythonConnect;
import funtion.Unicode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//주소 형식: http://localhost:8080/chatbot/일정알려줘/?me=12&friend=null&date=2022-06-21
//주소 형식2: http://localhost:8080/chatbot/날씨알려줘/?friend=null&date=2022-06-21
@Controller
public class ChatbotController {
    @ResponseBody
    @GetMapping("chatbot/{text}/")
    public String Hello(@PathVariable(value = "text") String text,
                        @RequestParam(value = "me", required = false) String me,
                        @RequestParam(value = "friend", required = false) String friend,
                        @RequestParam(value = "date", required = false) String date,
                        @RequestParam(value = "lat", required = false) String lat,
                        @RequestParam(value = "lon", required = false) String lon,
                        @RequestParam(value = "content", required = false) String content) {

        String uniText = Unicode.getInstance().korToUni(text);
        uniText = uniText.replace(" ", "%20");

        String uniContent = null;

        if (content != null) {
            uniContent = Unicode.getInstance().korToUni(content);
            uniContent = uniContent.replace(" ", "%20");
        }

        String msg = PythonConnect.getInstance().pytest("chatbot/" + uniText + "/?me=" + me + "&friend=" + friend + "&date=" + date + "&lat=" + lat + "&lon=" + lon + "&content=" + uniContent);
        System.out.println(msg);
        return msg;
    }
}