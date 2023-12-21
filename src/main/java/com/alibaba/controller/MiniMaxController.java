package com.alibaba.controller;

import com.alibaba.entity.MiniMaxRequest;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author quanhangbo
 * @date 2023/12/20 14:37
 */
@Slf4j
@RestController
public class MiniMaxController {

    // https://api.minimax.chat/user-center/basic-information/interface-key
    private static final String MODEL = "abab5.5-chat";
    private static final String API_KEY = "YOUR_API_KEY";
    private static final String GROUP_ID = "YOUR_GROUP_ID";

    @GetMapping(value = "/api/model/minimax", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam String query) {
        log.info("query={}", query);
        SseEmitter emitter = new SseEmitter();

        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.execute(() -> {
            try {

                MiniMaxRequest minimaxRequest = new MiniMaxRequest();
                minimaxRequest.setRequestId(UUID.randomUUID().toString().replace("_", ""));
                minimaxRequest.setModel(MiniMaxController.MODEL);
                minimaxRequest.setStream(true);
                minimaxRequest.setTopP(0.7f);
                minimaxRequest.setTemperature(0.9f);
                minimaxRequest.setTokensToGenerate(4096);
                minimaxRequest.setMessages(
                        Arrays.asList(new MiniMaxRequest.Message().
                                setSender_type("USER").setSender_name("YOU").setText(query))
                );

                minimaxRequest.setBot_setting(
                        Arrays.asList(new MiniMaxRequest.BotSetting().
                                setBot_name("MM智能助理").setContent("MM智能助理是一款由MiniMax自研的，没有调用其他产品的接口的大型语言模型。MiniMax是一家中国科技公司，一直致力于进行大模型相关的研究。"))
                );

                minimaxRequest.setReply_constraints(
                        new MiniMaxRequest.ReplyConstraints().
                                setSender_type("BOT").setSender_name("MM智能助理")
                );


                String jsonPayload = new Gson().toJson(minimaxRequest);
                System.out.println(jsonPayload);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.minimax.chat/v1/text/chatcompletion_pro?GroupId=" + GROUP_ID)
                        .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonPayload))
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Cache-Control","no-cache")
                        .header("Accept", "text/event-stream")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    String responseData = body.string();
                    log.info("Sending SSE event: {}", responseData);
                    emitter.send(SseEmitter.event().data(responseData).name("message"));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    e.printStackTrace();
                } finally {
                    emitter.complete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok(emitter);
    }
}
