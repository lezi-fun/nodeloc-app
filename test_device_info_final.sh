#!/bin/bash
set -e

BASE_URL="https://www.nodeloc.com"

echo "=== 步骤 1: 获取初始 CSRF token ==="
curl -s -c /tmp/cookies.txt "$BASE_URL/" > /dev/null
CSRF_TOKEN=$(curl -s -b /tmp/cookies.txt "$BASE_URL/session/csrf.json" | grep -o '"csrf":"[^"]*' | cut -d'"' -f4)
echo "CSRF Token: ${CSRF_TOKEN:0:30}..."

echo -e "\n=== 步骤 2: 登录 ==="
LOGIN_RESPONSE=$(curl -s -b /tmp/cookies.txt -c /tmp/cookies.txt -X POST "$BASE_URL/session" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  --data-urlencode "login=lezi-fun" \
  --data-urlencode "password=Yangzimu@880")

if echo "$LOGIN_RESPONSE" | grep -q '"username":"lezi-fun"'; then
    echo "✅ 登录成功"
else
    echo "❌ 登录失败: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "\n=== 步骤 3: 获取新的 CSRF token ==="
CSRF_TOKEN=$(curl -s -b /tmp/cookies.txt "$BASE_URL/session/csrf.json" | grep -o '"csrf":"[^"]*' | cut -d'"' -f4)
echo "新 CSRF Token: ${CSRF_TOKEN:0:30}..."

echo -e "\n=== 步骤 4: 检查 post_source level ==="
LEVEL_RESPONSE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/mobile/preferences/post_source")
CURRENT_LEVEL=$(echo "$LEVEL_RESPONSE" | grep -o '"level":[0-9]' | cut -d':' -f2)
echo "当前 level: $CURRENT_LEVEL"

if [ "$CURRENT_LEVEL" != "4" ]; then
    echo -e "\n=== 步骤 5: 设置 level 为 4 ==="
    curl -s -b /tmp/cookies.txt -X PUT "$BASE_URL/mobile/preferences/post_source" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "X-CSRF-Token: $CSRF_TOKEN" \
      -H "X-Requested-With: XMLHttpRequest" \
      -d "level=4" > /dev/null
    echo "✅ 设置完成"
fi

echo -e "\n=== 步骤 6: 使用移动 User-Agent 发帖 ==="
MOBILE_UA="Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 NodeLocApp/0.1"
TEST_MSG="测试设备信息显示 - $(date +%s)"

echo "User-Agent: $MOBILE_UA"
echo "消息内容: $TEST_MSG"

POST_RESPONSE=$(curl -s -b /tmp/cookies.txt -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  -H "User-Agent: $MOBILE_UA" \
  --data-urlencode "topic_id=105674" \
  --data-urlencode "raw=$TEST_MSG")

POST_ID=$(echo "$POST_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$POST_ID" ]; then
    echo -e "\n✅ 发帖成功！Post ID: $POST_ID"
    
    echo -e "\n=== 步骤 7: 等待 3 秒后检查帖子 ==="
    sleep 3
    
    POST_DETAIL=$(curl -s -b /tmp/cookies.txt "$BASE_URL/posts/$POST_ID.json")
    USER_AGENT_URL=$(echo "$POST_DETAIL" | grep -o '"user_agent_url":"[^"]*' | cut -d'"' -f4)
    
    echo -e "\n========== 最终结果 =========="
    echo "Post ID: $POST_ID"
    echo "User Agent URL: $USER_AGENT_URL"
    
    if [ -n "$USER_AGENT_URL" ] && [ "$USER_AGENT_URL" != "null" ]; then
        echo -e "\n✅✅✅ 成功！设备信息已正确记录！"
        echo "设备信息链接: $USER_AGENT_URL"
    else
        echo -e "\n❌ user_agent_url 仍然为 null"
        echo -e "\n完整响应:"
        echo "$POST_DETAIL" | grep -E '"(id|user_agent_url|username|created_at)"'
    fi
    
    echo -e "\n查看帖子: https://www.nodeloc.com/t/topic/105674"
else
    echo -e "\n❌ 发帖失败"
    echo "$POST_RESPONSE"
fi

rm -f /tmp/cookies.txt
