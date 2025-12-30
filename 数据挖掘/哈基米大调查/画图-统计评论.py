import os
import csv
import matplotlib.pyplot as plt

# 支持中文显示
plt.rcParams['font.family'] = 'SimHei'
plt.rcParams['axes.unicode_minus'] = False

def extract_bv_and_title(filename):
    """
    从文件名提取 BV 号和标题
    当前文件格式：BV号_标题.csv
    """
    if filename.endswith('.csv'):
        base = filename[:-4]  # 去掉.csv后缀
        parts = base.split('_', 1)
        if len(parts) == 2:
            bv = parts[0]
            title = parts[1]
            return bv, title
    return None, None

def count_csv_records(filepath):
    """计算 CSV 文件中的数据记录条数（不包含表头）"""
    with open(filepath, mode='r', encoding='utf-8-sig') as file:
        reader = csv.reader(file)
        rows = list(reader)
        return len(rows) - 1  # 减去表头

def main():
    # 获取当前脚本路径
    current_dir = os.getcwd()
    data_dir = os.path.join(current_dir, 'data')
    output_dir = os.path.join(current_dir, 'output')

    # 创建输出文件夹
    os.makedirs(output_dir, exist_ok=True)

    # 数据收集
    bv_list = []
    title_list = []
    count_list = []
    total: int = 0

    for filename in os.listdir(data_dir):
        if filename.endswith('.csv'):
            bv, title = extract_bv_and_title(filename)
            if bv and title:
                filepath = os.path.join(data_dir, filename)
                record_count = count_csv_records(filepath)
                bv_list.append(bv)
                title_list.append(title)
                count_list.append(record_count)
                total += record_count
                print(f"{filename}: {record_count} 条评论")

    if not count_list:
        print("⚠️ 未找到符合格式的 CSV 文件，或数据为空。请检查文件路径和命名格式。")
        return

    # 按评论数量排序
    sorted_data = sorted(zip(count_list, bv_list, title_list), reverse=True)
    count_list, bv_list, title_list = zip(*sorted_data)

    # 绘图
    plt.figure(figsize=(14, 7))
    plt.barh(title_list, count_list, color='skyblue')
    plt.xlabel('评论数量')
    plt.ylabel('视频标题')
    plt.title('视频评论数量统计')
    plt.tight_layout()

    # 保存图片
    image_path = os.path.join(output_dir, '评论数量统计图.png')
    plt.savefig(image_path, dpi=300)
    print(f"✅ 统计图已保存至：{image_path}")
    print(f"总评论数：{total}")
    # 显示图片
    plt.show()

if __name__ == '__main__':
    main()
