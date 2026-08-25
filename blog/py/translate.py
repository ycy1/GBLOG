import translators as ts
import argparse
import sys
import re

# 待翻译的文本
text = "开发部"
# 需要过滤的介词列表
STOP_WORDS = {'of', 'and', 'the', 'for', 'with', 'on', 'at', 'from', 'by', 'in', 'to', 'a', 'an'}
word_length=5  # 单词长度阈值

# 使用默认翻译器（通常是Bing）翻译成英文
parser = argparse.ArgumentParser(description='文本翻译工具')
parser.add_argument('text', nargs='?', default="信息科技部", help='输入文本，默认为 开发部')
args = parser.parse_args()

result = ts.translate_text(
    args.text,  
    translator='bing',  # 指定使用Bing翻译
    from_language='zh',  # 从中文翻译
    to_language='en'  # 到英文
)
# print(result) 

# 移除特殊字符，只保留字母和空格
clean = re.sub(r'[^a-zA-Z\s]', '', result)

# 方法1: 取每个单词首字母大写 (适合专有名词)
words = clean.split()
#过滤修饰词，只保留主要词汇
filtered_words = [word for word in words if word.lower() not in STOP_WORDS]
if not filtered_words:
        filtered_words = words[:1]

# 生成简写（每个单词取前N个字母，小写）
short_parts = []
for word in filtered_words:
    if len(word) <= word_length:
        short_parts.append(word.lower())
    else:
        short_parts.append(word[:word_length].lower())

# 用下划线连接
short_code = '_'.join(short_parts)


print(short_code)  # 输出简写
