package com.alibaba.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author quanhangbo
 * @date 2023/12/21 20:54
 */
@Data
@Accessors
public class WenXinYiYanRequest {

    private List<Message> messages;
    private boolean stream;

    @Data
    @Accessors(chain = true)
    static public class Message {
        private String role;
        private String content;
    }

    @Data
    @Accessors(chain = true)
    static public class WenXinYiYanAuthToken {
        private String refresh_token;
        private int expires_in;
        private String session_key;
        private String access_token;
        private String scope;
        private String session_secret;
    }

}
