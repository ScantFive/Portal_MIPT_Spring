package com.mipt.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {
        "/",
        "/{a:[^\\.]*}",
        "/{a:[^\\.]*}/{b:[^\\.]*}",
        "/{a:[^\\.]*}/{b:[^\\.]*}/{c:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}