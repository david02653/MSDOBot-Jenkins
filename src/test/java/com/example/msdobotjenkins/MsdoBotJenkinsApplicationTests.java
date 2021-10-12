package com.example.msdobotjenkins;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import soselab.david.msdobot.Entity.Jenkins.JenkinsJob;
import soselab.david.msdobot.Entity.Jenkins.JenkinsLog;
import soselab.david.msdobot.Entity.Jenkins.JenkinsLogEntry;
import soselab.david.msdobot.Entity.Jenkins.JenkinsView;
import soselab.david.msdobot.Exception.RequestFailException;
import soselab.david.msdobot.Service.IntentHandleService;
import soselab.david.msdobot.Service.JenkinsXMLParser;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootConfiguration
class MsdoBotJenkinsApplicationTests {



    @Test
    void RegexTest(){
//        Pattern pattern = Pattern.compile("^[a-zA-Z ]+on$");
        String pattern = "^[a-zA-Z ]+on$";
//        Pattern pattern1 = Pattern.compile("^is[a-zA-Z ]+on$");
        String pattern1 = "^is[a-zA-Z ]+on$";
        String input = "last build report of Notification";
        if(Pattern.matches(pattern, input))
            System.out.println("0 hits");
        if(Pattern.matches(pattern1, input))
            System.out.println("1 hits");
    }



}
