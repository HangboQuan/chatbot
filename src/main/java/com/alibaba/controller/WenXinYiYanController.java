package com.alibaba.controller;

import com.alibaba.entity.WenXinYiYanRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author quanhangbo
 * @date 2023/12/21 20:36
 */
@Slf4j
@RestController
public class WenXinYiYanController {

    // https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application
    private static final String API_KEY = "YOUR_API_KEY";
    private static final String API_SECRET = "YOUR_API_SECRET";

    private static final long expireMillis = 30 * 24 * 60 * 1000L;
    private static final String ACCESS_TOKEN = "access_token";

    // 缓存服务
    public static Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS)
            .build();

    @GetMapping(value = "/api/model/wenxinyiyan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<ResponseBodyEmitter> chat(@RequestParam String query) {
        log.info("query={}", query);
        SseEmitter emitter = new SseEmitter();

        ExecutorService threadPool = Executors.newFixedThreadPool(1);
        threadPool.execute(() -> {
            String accessToken = cache.getIfPresent(ACCESS_TOKEN);
            if (accessToken == null) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + API_KEY + "&client_secret=" + API_SECRET)
                        .get().header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Cache-Control","no-cache")
                        .header("Accept", "text/event-stream")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    String data = body.string();
                    WenXinYiYanRequest.WenXinYiYanAuthToken wenXinYiYanAuthToken = new Gson().fromJson(data, WenXinYiYanRequest.WenXinYiYanAuthToken.class);
                    System.out.println(wenXinYiYanAuthToken.toString());
                    accessToken = wenXinYiYanAuthToken.getAccess_token();
                    cache.put(ACCESS_TOKEN, accessToken);
                    log.info("accessToken:{}", accessToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                WenXinYiYanRequest wenXinYiYanRequest = new WenXinYiYanRequest();
                WenXinYiYanRequest.Message message = new WenXinYiYanRequest.Message();
                message.setRole("user");
                message.setContent(query);
                wenXinYiYanRequest.setMessages(new ArrayList<WenXinYiYanRequest.Message>() {
                    {
                        add(message);
                    }
                });
                wenXinYiYanRequest.setStream(true);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions_pro?access_token=" + accessToken)
                        .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), new Gson().toJson(wenXinYiYanRequest)))
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
