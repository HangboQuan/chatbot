调用chatglm官方api接口实现简单的流式对话效果【Spring Boot + HTML 约400行代码实现】

流式效果图
![chatglm_async_stream](https://github.com/HangboQuan/chatbot/blob/master/images/chatglm_async_stream.gif)

同步效果图
![chatglm_sync](https://github.com/HangboQuan/chatbot/blob/master/images/chatglm_sync.gif)

运行
1. 在chatglm开放平台中找到对应的apiKey填写到代码中相应的位置上，启动Spring Boot项目
2. 在resouces目录下找到两个分别为chatglm_async_stream.html 和 chatglm_sync.html，分别在浏览器中打开；然后请求想要的query即可

扩展和思考
1. 在并发条件上会不会出现乱序、重复？
2. 这种消息如何设计它的存储？


