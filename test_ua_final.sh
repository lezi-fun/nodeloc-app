#!/bin/bash
set -e

BASE_URL="https://www.nodeloc.com"

echo "=== 步骤 1: 访问首页获取初始状态 ==="
curl -s -c /tmp/cookies.txt -o /dev/null "$BASE_URL/"

echo "=== 步骤 2: 获取登录页面的 CSRF token ==="
LOGIN_PAGE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/login")
CSRF_TOKEN=$(echo "$LOGIN_PAGE" | grep -o 'csrf-token" content="[^"]*' | cut -d'"' -f3)
echo "CSRF Token: ${CSRF_TOKEN:0:30}..."

echo -e "\n=== 步骤 3: 登录 ==="
curl -s -b /tmp/cookies.txt -c /tmp/cookies.txt -X POST "$BASE_URL/session" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  --data-urlencode "login=lezi-fun" \
  --data-urlencode "password=Yangzimu@880" \
  > /tmp/login_response.json

if grep -q '"username":"lezi-fun"' /tmp/login_response.json; then
    echo "✅ 登录成功"
else
    echo "❌ 登录失败"
    cat /tmp/login_response.json
    exit 1
fi

echo -e "\n=== 步骤 4: 获取新的 CSRF token ==="
MAIN_PAGE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/")
CSRF_TOKEN=$(echo "$MAIN_PAGE" | grep -o 'csrf-token" content="[^"]*' | cut -d'"' -f3)
echo "新 CSRF Token: ${CSRF_TOKEN:0:30}..."

echo -e "\n=== 步骤 5: 检查 post_source level ==="
curl -s -b /tmp/cookies.txt "$BASE_URL/mobile/preferences/post_source" > /tmp/level.json
CURRENT_LEVEL=$(cat /tmp/level.json | grep -o '"level":[0-9]' | cut -d':' -f2)
echo "当前 level: $CURRENT_LEVEL"

if [ "$CURRENT_LEVEL" != "4" ]; then
    echo -e "\n=== 步骤 6: 设置 level 为 4 ==="
    curl -s -b /tmp/cookies.txt -X PUT "$BASE_URL/mobile/preferences/post_source" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "X-CSRF-Token: $CSRF_TOKEN" \
      -H "X-Requested-With: XMLHttpRequest" \
      -d "level=4"
    echo "✅ 设置完成"
fi

echo -e "\n=== 步骤 7: 使用移动 User-Agent 发帖 ==="
MOBILE_UA="Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 NodeLocApp/0.1"
TEST_MSG="测试移动设备信息 - $(date +%s)"

echo "User-Agent: $MOBILE_UA"
echo "消息内容: $TEST_MSG"

curl -s -b /tmp/cookies.txt -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  -H "User-Agent: $MOBILE_UA" \
  --data-urlencode "topic_id=105674" \
  --data-urlencode "raw=$TEST_MSG" \
  > /tmp/post_response.json

POST_ID=$(cat /tmp/post_response.json | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$POST_ID" ]; then
    echo -e "\n✅ 发帖成功！Post ID: $POST_ID"
    
    echo -e "\n=== 步骤 8: 等待 2 秒后检查帖子 ==="
    sleep 2
    
    curl -s -b /tmp/cookies.txt "$BASE_URL/posts/$POST_ID.json" > /tmp/post_detail.json
    
    echo -e "\n=== 结果 ==="
    USER_AGENT_URL=$(cat /tmp/post_detail.json | grep -o '"user_agent_url":"[^"]*' | cut -d'"' -f4)
    
    echo "Post ID: $POST_ID"
    echo "User Agent URL: $USER_AGENT_URL"
    
    if [ -n "$USER_AGENT_URL" ] && [ "$USER_AGENT_URL" != "null" ]; then
        echo -e "\n✅✅✅ 成功！设备信息已正确记录！"
        echo "设备信息链接: $USER_AGENT_URL"
    else
        echo -e "\n❌ user_agent_url 仍然为 null"
        echo -e "\n可能的原因："
        echo "1. 服务端未启用此功能"
        echo "2. 需要特殊的请求参数"
        echo "3. User-Agent 格式不被识别"
    fi
    
    echo -e "\n查看帖子: https://www.nodeloc.com/t/topic/105674"
else
    echo -e "\n❌ 发帖失败"
    cat /tmp/post_response.json
fi

rm -f /tmp/cookies.txt /tmp/*.json
