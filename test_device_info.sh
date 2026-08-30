#!/bin/bash

BASE_URL="https://www.nodeloc.com"

# 1. 先访问首页获取初始 cookie
echo "=== 获取初始 cookies ==="
curl -s -c /tmp/cookies.txt "$BASE_URL/" > /dev/null

# 2. 获取 CSRF token
echo "=== 获取 CSRF Token ==="
CSRF_RESPONSE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/session/csrf")
CSRF_TOKEN=$(echo "$CSRF_RESPONSE" | jq -r '.csrf')
echo "CSRF Token: ${CSRF_TOKEN:0:20}..."

# 3. 登录
echo -e "\n=== 登录中 ==="
LOGIN_RESPONSE=$(curl -s -b /tmp/cookies.txt -c /tmp/cookies.txt -X POST "$BASE_URL/session" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  -d "login=lezi-fun&password=Yangzimu%40880")

USERNAME=$(echo "$LOGIN_RESPONSE" | jq -r '.user.username' 2>/dev/null)
if [ "$USERNAME" = "lezi-fun" ]; then
    echo "✅ 登录成功: $USERNAME"
else
    echo "❌ 登录失败"
    echo "$LOGIN_RESPONSE"
    exit 1
fi

# 4. 重新获取 CSRF token（登录后的）
echo -e "\n=== 获取新的 CSRF Token ==="
CSRF_RESPONSE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/session/csrf")
CSRF_TOKEN=$(echo "$CSRF_RESPONSE" | jq -r '.csrf')
echo "新 CSRF Token: ${CSRF_TOKEN:0:20}..."

# 5. 检查当前 post_source level
echo -e "\n=== 检查发帖来源设置 ==="
LEVEL_RESPONSE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/mobile/preferences/post_source")
CURRENT_LEVEL=$(echo "$LEVEL_RESPONSE" | jq -r '.level')
echo "当前 level: $CURRENT_LEVEL"

# 6. 设置 post_source level 为 4
if [ "$CURRENT_LEVEL" != "4" ]; then
    echo -e "\n=== 设置 post_source level 为 4 ==="
    curl -s -b /tmp/cookies.txt -X PUT "$BASE_URL/mobile/preferences/post_source" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "X-CSRF-Token: $CSRF_TOKEN" \
      -H "X-Requested-With: XMLHttpRequest" \
      -d "level=4" | jq .
fi

# 7. 使用移动设备 User-Agent 发帖
echo -e "\n=== 发帖测试 (使用移动 User-Agent) ==="
USER_AGENT="Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 NodeLocApp/0.1"
TEST_MESSAGE="测试设备信息显示 - $(date +%s)"

echo "User-Agent: $USER_AGENT"
echo "消息: $TEST_MESSAGE"

POST_RESPONSE=$(curl -s -b /tmp/cookies.txt -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  -H "User-Agent: $USER_AGENT" \
  -d "topic_id=105674&raw=$(echo -n "$TEST_MESSAGE" | jq -sRr @uri)")

POST_ID=$(echo "$POST_RESPONSE" | jq -r '.id')
echo -e "\n发帖响应:"
echo "$POST_RESPONSE" | jq .

if [ "$POST_ID" != "null" ] && [ -n "$POST_ID" ]; then
    echo -e "\n✅ 发帖成功！Post ID: $POST_ID"
    
    # 8. 等待并检查帖子的 user_agent_url
    echo -e "\n=== 等待 2 秒后检查帖子 ==="
    sleep 2
    
    POST_DETAIL=$(curl -s -b /tmp/cookies.txt "$BASE_URL/posts/$POST_ID.json")
    USER_AGENT_URL=$(echo "$POST_DETAIL" | jq -r '.user_agent_url')
    
    echo -e "\n=== 结果 ==="
    echo "Post ID: $POST_ID"
    echo "User Agent URL: $USER_AGENT_URL"
    
    if [ "$USER_AGENT_URL" != "null" ] && [ -n "$USER_AGENT_URL" ]; then
        echo -e "\n✅ 成功！设备信息已记录！"
    else
        echo -e "\n❌ user_agent_url 仍然为 null"
        echo -e "\n完整帖子信息:"
        echo "$POST_DETAIL" | jq '{id, user_agent_url, created_at, username}'
    fi
    
    echo -e "\n查看帖子: https://www.nodeloc.com/t/topic/105674/$POST_ID"
else
    echo -e "\n❌ 发帖失败"
fi

rm -f /tmp/cookies.txt
