package com.alibaba.controller;

import com.alibaba.utils.TokenAuthUtil;
import com.google.gson.Gson;
import com.zhipu.oapi.service.v3.ModelApiRequest;
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
 * @date 2023/12/19 18:40
 */
@Slf4j
@RestController
public class ChatGLMController {

    // https://open.bigmodel.cn/usercenter/apikeys
    @GetMapping(value = "/api/model/chatglm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam String query) {
        log.info("query={}", query);
        SseEmitter emitter = new SseEmitter();

        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.execute(() -> {
            try {
                ModelApiRequest modelApiRequest = new ModelApiRequest();
                modelApiRequest.setRequestId(UUID.randomUUID().toString().replace("_", ""));
                modelApiRequest.setTopP(0.7f);
                modelApiRequest.setIncremental(true);
                modelApiRequest.setTemperature(0.9f);
                modelApiRequest.setPrompt(Arrays.asList(new ModelApiRequest.Prompt("user", query)));

                String jsonPayload = new Gson().toJson(modelApiRequest);
                System.out.println(jsonPayload);

                // 输入你自己的api_key
                String apiKey = "YOUR_API_KEY";
                String[] ans = apiKey.split("\\.");
                String token = TokenAuthUtil.getToken(ans[0], ans[1]);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://open.bigmodel.cn/api/paas/v3/model-api/chatglm_lite/sse-invoke")
                        .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonPayload))
                        .header("Authorization", "Bearer " + token)
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
