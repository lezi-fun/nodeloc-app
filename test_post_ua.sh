#!/bin/bash

# 使用用户提供的真实登录 session 测试
# 注意：这需要用户提供有效的 _t 和 _forum_session cookies

echo "请提供以下信息进行测试："
echo "1. _t cookie"
echo "2. _forum_session cookie"
echo ""
echo "由于安全原因，我无法直接使用账号密码登录"
echo "你可以从浏览器开发者工具中复制这些 cookie"
echo ""
echo "测试将会："
echo "1. 检查当前 post_source level"
echo "2. 设置为 level 4（如果不是）"
echo "3. 使用移动 User-Agent 发一条测试帖子"
echo "4. 检查 user_agent_url 是否有值"

