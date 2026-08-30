#!/bin/bash

echo "分析发帖请求可能需要的字段："
echo ""
echo "已知字段："
echo "- topic_id"
echo "- raw (帖子内容)"
echo "- reply_to_post_number (可选)"
echo ""
echo "可能缺少的字段（猜测）："
echo "1. user_agent - 直接传递 User-Agent 字符串"
echo "2. device_info - 设备信息对象"
echo "3. mobile - 标记是否移动设备"
echo "4. app_version - APP 版本"
echo ""
echo "让我检查 Discourse 官方文档或代码..."

# 搜索可能的字段名
for field in "user_agent" "device_info" "mobile" "app_version" "client"; do
    echo -e "\n=== 搜索字段: $field ==="
    grep -r "\"$field\"" /Users/home/Projects/nodeloc-app/mirror/www.nodeloc.com/assets/js/ --include="*.js" | grep -v ".map" | head -3
done
