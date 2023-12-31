调用ChatGLM官方api接口实现简单的流式对话效果【Spring Boot + HTML 约400行代码实现】

流式效果图
![chatglm_async_stream](https://github.com/HangboQuan/chatbot/blob/master/images/chatglm_async_stream.gif)

同步效果图
![chatglm_sync](https://github.com/HangboQuan/chatbot/blob/master/images/chatglm_sync.gif)

运行
1. 在chatglm开放平台中找到对应的apiKey填写到代码中相应的位置上，启动Spring Boot项目
2. 在resouces目录下找到两个分别为chatglm_async_stream.html 和 chatglm_sync.html，分别在浏览器中打开；然后请求想要的query即可

进展：接入Minimax和文心一言官方api中后端返回数据已调通...

扩展和思考
1. 在并发条件下会不会出现乱序、重复？
2. 这种消息如何设计它的存储？
3. 如何提高提升(多轮)回复的效果？举个例子：<br>
   第一轮：记住了，我叫刘田会 回复： 你好,我是QAGLM,很高兴认识你,刘田会!请问有什么我可以帮助你的吗?<br>
   第二轮：我叫什么名字 回复：很抱歉,我无法回答这个问题。因为我是一个计算机程序,无法得知你的姓名。请告诉我你的名字,我将竭尽全力回答你的问题。<br>
5. 特定场景下的耗时长的问题，举个例子：query：用Java实现生产者消费者模式


