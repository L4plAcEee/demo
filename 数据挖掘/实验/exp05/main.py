import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from mlxtend.frequent_patterns import apriori, association_rules, fpgrowth
from mlxtend.preprocessing import TransactionEncoder
import os
import warnings

warnings.filterwarnings('ignore')

# 设置中文显示
plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

# 数据路径
data_path = './data'

# 读取数据
goods_order = pd.read_csv(os.path.join(data_path, 'GoodsOrder.csv'))
goods_types = pd.read_csv(os.path.join(data_path, 'GoodsTypes.csv'))

print("数据读取完成，开始分析...")
print(f"订单数据形状: {goods_order.shape}")
print(f"商品类型数据形状: {goods_types.shape}")

# 显示数据前几行
print("\n订单数据前5行:")
print(goods_order.head())
print("\n商品类型数据前5行:")
print(goods_types.head())

# 根据示例，确认数据结构
# GoodsOrder.csv格式: id,Goods (例如: 1,柑橘类水果)
# 确保goods_order包含正确的列名
if 'TransactionID' not in goods_order.columns and 'id' in goods_order.columns:
    goods_order.rename(columns={'id': 'TransactionID'}, inplace=True)
if 'OrderCount' not in goods_order.columns:
    # 假设每行代表一次购买，数量为1
    goods_order['OrderCount'] = 1

# GoodsTypes.csv格式: Goods,Types (例如: 白饭,熟食)
# 确保goods_types包含正确的列名
if 'Type' not in goods_types.columns and 'Types' in goods_types.columns:
    goods_types.rename(columns={'Types': 'Type'}, inplace=True)

print("\n调整后的订单数据结构:")
print(goods_order.columns.tolist())
print("\n调整后的商品类型数据结构:")
print(goods_types.columns.tolist())


# 1. 计算销量排名前8的商品销量及其占比，并绘制条形图
def top_goods_analysis():
    print("\n任务1: 计算销量排名前8的商品销量及其占比")

    # 计算每种商品的销量（频次）
    goods_sales = goods_order['Goods'].value_counts().reset_index()
    goods_sales.columns = ['Goods', 'OrderCount']

    # 计算总销量
    total_sales = goods_sales['OrderCount'].sum()

    # 获取前8名商品
    top8_goods = goods_sales.head(8)

    # 计算占比
    top8_goods['Percentage'] = top8_goods['OrderCount'] / total_sales * 100

    print("\n销量排名前8的商品:")
    print(top8_goods)

    # 绘制条形图
    plt.figure(figsize=(12, 6))
    bars = plt.bar(top8_goods['Goods'], top8_goods['OrderCount'], color='skyblue')

    # 在条形上方显示具体销量和占比
    for i, bar in enumerate(bars):
        height = bar.get_height()
        plt.text(bar.get_x() + bar.get_width() / 2., height + 0.1,
                 f'{int(height)}\n({top8_goods["Percentage"].iloc[i]:.2f}%)',
                 ha='center', va='bottom')

    plt.title('销量排名前8的商品销量及占比')
    plt.xlabel('商品名称')
    plt.ylabel('销量')
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig('top8_goods_sales.png')
    plt.show()

    return top8_goods


# 2. 对商品进行归类，计算各类商品的销量及占比，绘制饼状图
def goods_type_analysis():
    print("\n任务2: 对商品进行归类，计算各类商品销量及占比")

    # 合并商品订单和商品类型数据
    merged_data = pd.merge(goods_order, goods_types, on='Goods', how='left')

    # 检查是否有未分类的商品
    if merged_data['Type'].isna().any():
        print(f"警告: 有 {merged_data['Type'].isna().sum()} 条记录的商品没有分类信息")
        # 为未分类商品创建一个默认分类
        merged_data['Type'] = merged_data['Type'].fillna('未分类')

    # 按商品类别计算销量
    type_sales = merged_data['Type'].value_counts().reset_index()
    type_sales.columns = ['Type', 'OrderCount']

    # 计算总销量
    total_sales = type_sales['OrderCount'].sum()

    # 计算占比
    type_sales['Percentage'] = type_sales['OrderCount'] / total_sales * 100

    print("\n各类商品销量及占比:")
    print(type_sales)

    # 绘制饼图
    plt.figure(figsize=(10, 8))
    plt.pie(type_sales['OrderCount'], labels=type_sales['Type'],
            autopct=lambda p: f'{p:.2f}%\n({int(p * total_sales / 100)})',
            startangle=90, shadow=True)
    plt.axis('equal')  # 确保饼图是圆的
    plt.title('各类商品销量及占比')
    plt.tight_layout()
    plt.savefig('goods_type_sales.png')
    plt.show()

    return type_sales, merged_data


# 3. 数据预处理，转换为Apriori算法所需的格式
def data_preprocessing():
    print("\n任务3: 数据预处理，转换为关联规则算法所需的格式")

    # 将订单数据按Transaction分组，每个Transaction包含多个商品
    # 对于单条交易记录，我们将其转换为列表格式
    if 'TransactionID' in goods_order.columns:
        # 如果存在TransactionID列，按其分组
        transactions = goods_order.groupby('TransactionID')['Goods'].apply(list).tolist()
    else:
        # 如果没有TransactionID列，假设每个id是一个交易ID
        transactions = goods_order.groupby('id')['Goods'].apply(lambda x: x.tolist()).tolist()

    # 检查transactions列表是否为空
    if not transactions:
        # 如果无法正确分组，可能每行就是一个独立的交易
        print("警告：无法按交易ID分组，将每行视为一个独立交易")
        transactions = [[item] for item in goods_order['Goods']]

    print(f"\n共有 {len(transactions)} 个交易记录")
    print("前5个交易记录示例:")
    for i in range(min(5, len(transactions))):
        print(f"交易 {i + 1}: {transactions[i]}")

    # 使用TransactionEncoder转换为二进制矩阵
    te = TransactionEncoder()
    te_ary = te.fit(transactions).transform(transactions)
    df_encoded = pd.DataFrame(te_ary, columns=te.columns_)

    print("\n转换后的二进制矩阵(前5行5列):")
    print(df_encoded.iloc[:5, :min(5, df_encoded.shape[1])])

    return transactions, df_encoded


# 4. 使用Apriori算法，最小支持度0.2，最小可信度0.3
def apriori_analysis(df_encoded, min_support=0.2, min_confidence=0.3):
    print(f"\n任务4: Apriori算法 (min_support={min_support}, min_confidence={min_confidence})")

    # 使用Apriori算法找出频繁项集
    frequent_itemsets = apriori(df_encoded, min_support=min_support, use_colnames=True)

    print("\n频繁项集:")
    if len(frequent_itemsets) == 0:
        print("没有找到满足最小支持度的频繁项集，请尝试降低最小支持度")
        return None, None

    print(frequent_itemsets.head())

    # 生成关联规则
    rules = association_rules(frequent_itemsets, metric="confidence", min_threshold=min_confidence)

    if len(rules) == 0:
        print("没有找到满足最小可信度的关联规则，请尝试降低最小可信度")
        return frequent_itemsets, None

    # 按可信度排序
    rules = rules.sort_values(by='confidence', ascending=False)

    # 规则转换为更易读的格式
    rules['antecedents'] = rules['antecedents'].apply(lambda x: ', '.join(list(x)))
    rules['consequents'] = rules['consequents'].apply(lambda x: ', '.join(list(x)))

    print("\n关联规则(前5条):")
    print(rules[['antecedents', 'consequents', 'support', 'confidence', 'lift']].head())

    return frequent_itemsets, rules


# 5. 修改参数，最小支持度0.02，最小可信度0.35
def apriori_analysis_adjusted(df_encoded):
    print("\n任务5: 调整参数后的Apriori算法 (min_support=0.02, min_confidence=0.35)")

    return apriori_analysis(df_encoded, min_support=0.02, min_confidence=0.35)


# 6. 分析关联规则并给出销售建议
def business_analysis(rules):
    print("\n任务6: 关联规则分析及销售建议")

    if rules is None or len(rules) == 0:
        print("没有找到关联规则，无法进行分析")
        return

    # 按提升度排序，找出提升度最高的规则
    top_lift_rules = rules.sort_values(by='lift', ascending=False).head(10)

    print("\n提升度最高的10条规则:")
    print(top_lift_rules[['antecedents', 'consequents', 'support', 'confidence', 'lift']])

    # 分析并给出销售建议
    print("\n销售建议:")
    print("1. 捆绑销售策略:")
    for i, row in top_lift_rules.head(3).iterrows():
        print(f"   - 将 {row['antecedents']} 与 {row['consequents']} 放在一起销售，可能会提高销量")

    print("\n2. 产品布局:")
    for i, row in top_lift_rules.iloc[3:6].iterrows():
        print(f"   - 在商店中将 {row['antecedents']} 与 {row['consequents']} 放在相邻位置")

    print("\n3. 促销活动:")
    for i, row in top_lift_rules.iloc[6:9].iterrows():
        print(f"   - 购买 {row['antecedents']} 时，可以给予 {row['consequents']} 折扣")

    return top_lift_rules


# 7. 解释提升度(Lift)指标
def explain_lift():
    print("\n任务7: 提升度(Lift)指标解释")

    explanation = """
提升度(Lift)是衡量关联规则有效性的重要指标，它表示同时购买A和B的概率与独立购买A和B的概率的比值。

提升度的计算公式为: Lift(A→B) = P(B|A) / P(B) = Confidence(A→B) / Support(B)

提升度与关联规则的关系:
1. 提升度 > 1: 表示A的出现对B的出现有正向影响，即A和B是正相关的。提升度越高，关联性越强。
2. 提升度 = 1: 表示A和B相互独立，即A的出现对B的出现没有影响。
3. 提升度 < 1: 表示A的出现对B的出现有负向影响，即A和B是负相关的。

在商业分析中，通常关注提升度大于1的规则，因为这些规则表明两种商品之间存在真正的关联性，
可以用于指导产品布局、捆绑销售、促销活动等营销策略的制定。
    """

    print(explanation)


# 8. 使用FP-Tree算法生成关联规则，并分析两种算法的异同
def fp_growth_analysis(df_encoded):
    print("\n任务8: FP-Tree算法与Apriori算法比较")

    # 使用FP-Growth算法
    print("\nFP-Growth算法 (min_support=0.02):")
    start_time_fp = pd.Timestamp.now()
    frequent_itemsets_fp = fpgrowth(df_encoded, min_support=0.02, use_colnames=True)
    rules_fp = association_rules(frequent_itemsets_fp, metric="confidence", min_threshold=0.35)
    end_time_fp = pd.Timestamp.now()

    # 规则转换为更易读的格式
    if len(rules_fp) > 0:
        rules_fp['antecedents'] = rules_fp['antecedents'].apply(lambda x: ', '.join(list(x)))
        rules_fp['consequents'] = rules_fp['consequents'].apply(lambda x: ', '.join(list(x)))

    print(f"\nFP-Growth找到的频繁项集数量: {len(frequent_itemsets_fp)}")
    print(f"FP-Growth找到的关联规则数量: {len(rules_fp)}")
    print(f"FP-Growth执行时间: {(end_time_fp - start_time_fp).total_seconds():.4f} 秒")

    # 对比Apriori算法
    print("\n使用相同参数的Apriori算法:")
    start_time_ap = pd.Timestamp.now()
    frequent_itemsets_ap = apriori(df_encoded, min_support=0.02, use_colnames=True)
    rules_ap = association_rules(frequent_itemsets_ap, metric="confidence", min_threshold=0.35)
    end_time_ap = pd.Timestamp.now()

    print(f"Apriori找到的频繁项集数量: {len(frequent_itemsets_ap)}")
    print(f"Apriori找到的关联规则数量: {len(rules_ap)}")
    print(f"Apriori执行时间: {(end_time_ap - start_time_ap).total_seconds():.4f} 秒")

    # 算法比较分析
    comparison = """
Apriori算法与FP-Tree算法的异同:

相同点:
1. 目的相同: 两种算法都用于发现数据中的频繁项集和关联规则。
2. 最终结果相同: 在相同的参数设置下，两种算法发现的频繁项集和关联规则应该是一致的。
3. 都遵循支持度和置信度阈值: 两种算法都使用支持度和置信度作为筛选规则的标准。

不同点:
1. 算法原理:
   - Apriori: 使用"先验性质"，即如果一个项集是频繁的，则它的所有子集也是频繁的。采用广度优先搜索策略。
   - FP-Tree: 使用紧凑的树结构存储频繁项信息，避免了多次扫描数据库，采用深度优先搜索策略。

2. 性能效率:
   - Apriori: 在处理大数据集时，可能需要生成大量的候选项集，导致算法效率较低。
   - FP-Tree: 通常比Apriori更高效，尤其是在处理大规模数据集时，因为它避免了生成候选项集的过程。

3. 内存使用:
   - Apriori: 需要存储所有候选项集，可能占用较大内存。
   - FP-Tree: 使用紧凑的树结构，内存使用通常更高效。

4. 应用场景:
   - Apriori: 适合项目数量少、事务数量适中的情况。
   - FP-Tree: 更适合处理大规模数据集和高维数据。

总结: FP-Tree算法通常比Apriori算法更高效，特别是在处理大规模数据集时。
但Apriori算法概念简单，易于实现和理解，在小型数据集上仍有其应用价值。
    """

    print(comparison)

    return rules_fp


# 主函数
def main():
    print("开始商品销售数据分析与关联规则挖掘...\n")

    try:
        # 1. 销量排名前8的商品分析
        top8_goods = top_goods_analysis()

        # 2. 商品类别分析
        type_sales, merged_data = goods_type_analysis()

        # 3. 数据预处理
        transactions, df_encoded = data_preprocessing()

        # 如果数据量较小，可能需要调整支持度参数
        min_support_default = 0.2
        min_confidence_default = 0.3

        # 检查数据规模，根据实际情况调整参数
        transaction_count = len(transactions)
        if transaction_count < 100:
            min_support_default = 0.05
            print(f"\n注意：由于交易记录较少 ({transaction_count} 条)，已自动降低默认最小支持度为 {min_support_default}")

        # 4. Apriori算法分析
        print(f"\n使用初始参数: 最小支持度={min_support_default}, 最小可信度={min_confidence_default}")
        frequent_itemsets, rules = apriori_analysis(df_encoded, min_support=min_support_default,
                                                    min_confidence=min_confidence_default)

        # 5. 调整参数的Apriori算法分析
        min_support_adjusted = 0.02
        min_confidence_adjusted = 0.35
        print(f"\n使用调整后参数: 最小支持度={min_support_adjusted}, 最小可信度={min_confidence_adjusted}")
        frequent_itemsets_adj, rules_adj = apriori_analysis(df_encoded, min_support=min_support_adjusted,
                                                            min_confidence=min_confidence_adjusted)

        # 6. 业务分析和销售建议
        if rules_adj is not None and len(rules_adj) > 0:
            top_rules = business_analysis(rules_adj)
        else:
            print("\n无法进行业务分析，因为没有找到满足条件的关联规则。")

        # 7. 解释提升度指标
        explain_lift()

        # 8. FP-Tree算法分析与比较
        fp_rules = fp_growth_analysis(df_encoded)

        print("\n分析完成！所有图表已保存。")

    except Exception as e:
        print(f"\n分析过程中发生错误: {str(e)}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()