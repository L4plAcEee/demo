import os

# 获取当前脚本所在目录
folder_path = os.getcwd()

# 要删除的后缀
suffix_to_remove = '_哔哩哔哩_bilibili_comments'

# 遍历当前目录所有文件
for filename in os.listdir(folder_path):
    if filename.endswith('.csv') and suffix_to_remove in filename:
        # 新文件名
        new_name = filename.replace(suffix_to_remove, '')
        # 完整路径
        old_file = os.path.join(folder_path, filename)
        new_file = os.path.join(folder_path, new_name)
        # 执行重命名
        os.rename(old_file, new_file)
        print(f"已重命名: {filename} -> {new_name}")
