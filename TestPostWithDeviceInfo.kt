package app.nodeloc

import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SessionStore
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    try {
        // 登录
        println("正在登录...")
        SessionStore.tCookie = null
        SessionStore.sessionCookie = null
        DiscourseApi.login("lezi-fun", "Yangzimu@880")
        println("登录成功！")
        
        // 获取当前 post_source level
        println("\n检查发帖来源设置...")
        val level = DiscourseApi.getPostSourceLevel()
        println("当前 post_source level: $level")
        
        // 如果不是 4，设置为 4
        if (level != 4) {
            println("设置 post_source level 为 4...")
            DiscourseApi.setPostSourceLevel(4)
            println("设置完成")
        }
        
        // 发帖测试
        val testTopicId = 105674L
        val testMessage = "测试设备信息显示 - ${System.currentTimeMillis()}"
        println("\n发送测试回复到话题 $testTopicId...")
        println("内容: $testMessage")
        
        DiscourseApi.createPost(testTopicId, testMessage)
        println("\n✅ 发帖成功！")
        
        // 等待一下让服务器处理
        Thread.sleep(2000)
        
        // 获取话题最新帖子，检查 user_agent_url
        println("\n检查最新帖子的设备信息...")
        val topic = DiscourseApi.topicDetail(testTopicId)
        val lastPost = topic.postStream.posts.lastOrNull()
        
        if (lastPost != null) {
            println("\n最新帖子信息:")
            println("- Post ID: ${lastPost.id}")
            println("- Post Number: ${lastPost.postNumber}")
            println("- User Agent URL: ${lastPost.userAgentUrl}")
            
            if (lastPost.userAgentUrl != null) {
                println("\n✅ 成功！设备信息已记录！")
            } else {
                println("\n❌ user_agent_url 仍然为 null")
                println("\n当前 User-Agent 会被识别为:")
                println(android.os.Build.MODEL)
            }
        }
        
        println("\n查看帖子: https://www.nodeloc.com/t/topic/$testTopicId")
        
    } catch (e: Exception) {
        println("❌ 错误: ${e.message}")
        e.printStackTrace()
    }
}
