package com.alibaba.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author quanhangbo
 * @date 2023/12/20 14:40
 */
@Data
@Accessors
public class MiniMaxRequest {

    private String requestId;
    private String sessionId;
    private String model;
    private List<Message> messages;
    private List<BotSetting> bot_setting;
    private ReplyConstraints reply_constraints;

    // 下面是可选参数
    private boolean stream;

    private int tokensToGenerate;

    private float topP;

    private float temperature;

    private boolean maskSensitiveInfo;


    @Data
    @Accessors(chain = true)
    static public class Message {
        String sender_type;
        String sender_name;
        String text;
    }

    @Data
    @Accessors(chain = true)
    static public class BotSetting {
        String bot_name;
        String content;
    }

    @Data
    @Accessors(chain = true)
    static public class ReplyConstraints {
        String sender_type;
        String sender_name;
    }



}
