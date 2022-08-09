package com.example.test.controller;

import funtion.PythonConnect;
import funtion.Unicode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WeatherContorller {
    @GetMapping("weather/{text}")
    @ResponseBody
    public String Hello(Model model, @PathVariable(value="text") String text) {
        String uni = Unicode.getInstance().korToUni(text);
        String msg = PythonConnect.getInstance().pytest("weather/"+uni);
        return msg;
    }
}