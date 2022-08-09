package com.example.test.controller;

import funtion.PythonConnect;
import funtion.Unicode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RestaurantController {
    @GetMapping("restorang/{text}")
    public String Hello(Model model, @PathVariable(value="text") String text) {
        String uni = Unicode.getInstance().korToUni(text);
        String msg = PythonConnect.getInstance().pytest("restorang/"+uni);
        model.addAttribute("data", msg);
        return "hello";
    }
}
