#!/bin/bash

# 使用真实的浏览器 cookies 测试
# 请提供你的 _t 和 _forum_session cookies

echo "==================== 测试说明 ===================="
echo ""
echo "由于服务器有防护，无法通过 curl 模拟登录。"
echo "请按以下步骤提供真实的 cookie："
echo ""
echo "1. 在浏览器中打开 https://www.nodeloc.com"
echo "2. 确保已登录 lezi-fun 账号"
echo "3. 打开开发者工具 (F12)"
echo "4. 进入 Application/Storage -> Cookies"
echo "5. 复制以下 cookies 的值："
echo "   - _t"
echo "   - _forum_session"
echo ""
echo "然后运行："
echo ""
echo "  T_COOKIE='你的_t值' SESSION_COOKIE='你的_forum_session值' ./test_post_device_info.sh"
echo ""
echo "=================================================="

if [ -z "$T_COOKIE" ] || [ -z "$SESSION_COOKIE" ]; then
    echo ""
    echo "提示：环境变量未设置，请先提供 cookies"
    exit 0
fi

BASE_URL="https://www.nodeloc.com"

# 创建 cookie 文件
cat > /tmp/cookies.txt << COOKIES
# Netscape HTTP Cookie File
www.nodeloc.com	FALSE	/	TRUE	0	_t	$T_COOKIE
www.nodeloc.com	FALSE	/	TRUE	0	_forum_session	$SESSION_COOKIE
COOKIES

echo "=== 步骤 1: 获取 CSRF Token ==="
CSRF_TOKEN=$(curl -s -b /tmp/cookies.txt "$BASE_URL/session/csrf.json" | grep -o '"csrf":"[^"]*' | cut -d'"' -f4)
echo "CSRF Token: ${CSRF_TOKEN:0:40}..."

echo -e "\n=== 步骤 2: 检查 post_source level ==="
LEVEL_RESPONSE=$(curl -s -b /tmp/cookies.txt "$BASE_URL/mobile/preferences/post_source")
CURRENT_LEVEL=$(echo "$LEVEL_RESPONSE" | grep -o '"level":[0-9]' | cut -d':' -f2)
echo "当前 level: $CURRENT_LEVEL"

if [ "$CURRENT_LEVEL" != "4" ]; then
    echo -e "\n=== 步骤 3: 设置 level 为 4 ==="
    curl -s -b /tmp/cookies.txt -X PUT "$BASE_URL/mobile/preferences/post_source" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -H "X-CSRF-Token: $CSRF_TOKEN" \
      -H "X-Requested-With: XMLHttpRequest" \
      -d "level=4" > /dev/null
    echo "✅ 设置完成"
fi

echo -e "\n=== 步骤 4: 使用移动 User-Agent 和设备信息字段发帖 ==="
MOBILE_UA="Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36"
TEST_MSG="测试设备信息显示 - $(date +%s)"

echo "User-Agent: $MOBILE_UA"
echo "消息: $TEST_MSG"
echo "添加设备信息字段:"
echo "  - mobile_source_platform: android"
echo "  - mobile_source_brand: Google"
echo "  - mobile_source_model: Pixel 8 Pro"

POST_RESPONSE=$(curl -s -b /tmp/cookies.txt -X POST "$BASE_URL/posts" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "X-CSRF-Token: $CSRF_TOKEN" \
  -H "X-Requested-With: XMLHttpRequest" \
  -H "User-Agent: $MOBILE_UA" \
  --data-urlencode "topic_id=105674" \
  --data-urlencode "raw=$TEST_MSG" \
  --data-urlencode "mobile_source_platform=android" \
  --data-urlencode "mobile_source_brand=Google" \
  --data-urlencode "mobile_source_model=Pixel 8 Pro")

POST_ID=$(echo "$POST_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$POST_ID" ]; then
    echo -e "\n✅ 发帖成功！Post ID: $POST_ID"
    
    echo -e "\n=== 步骤 5: 等待 3 秒后检查 ==="
    sleep 3
    
    POST_DETAIL=$(curl -s -b /tmp/cookies.txt "$BASE_URL/posts/$POST_ID.json")
    USER_AGENT_URL=$(echo "$POST_DETAIL" | grep -o '"user_agent_url":"[^"]*' | cut -d'"' -f4)
    
    echo -e "\n========== 测试结果 =========="
    echo "Post ID: $POST_ID"
    echo "User Agent URL: $USER_AGENT_URL"
    
    if [ -n "$USER_AGENT_URL" ]; then
        echo -e "\n✅✅✅ 成功！设备信息已记录！"
        echo "设备信息: $USER_AGENT_URL"
    else
        echo -e "\n❌ user_agent_url 为空"
    fi
    
    echo -e "\n查看: https://www.nodeloc.com/t/topic/105674"
else
    echo -e "\n❌ 发帖失败: $POST_RESPONSE"
fi

rm -f /tmp/cookies.txt
