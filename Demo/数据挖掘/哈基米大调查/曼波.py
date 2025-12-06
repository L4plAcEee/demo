import pandas as pd
import numpy as np
import jieba
import jieba.analyse
from snownlp import SnowNLP
import re
import os
import glob
from collections import Counter
import matplotlib.pyplot as plt
import seaborn as sns
from wordcloud import WordCloud
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans, DBSCAN
from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import SVC
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, silhouette_score
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
import warnings

warnings.filterwarnings('ignore')

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False


class BilibiliUserProfiler:
    def __init__(self):
        self.df = None
        self.user_features = None
        self.tfidf_vectorizer = None
        self.cluster_labels = None
        self.stop_words = self._load_stop_words()

    def _load_stop_words(self):
        """加载停用词表"""
        # 常用中文停用词
        stop_words = [
            '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
            '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
            '自己', '这', '那', '就是', '但是', '如果', '可以', '因为', '所以', '虽然',
            '哈哈', '呵呵', '嘿嘿', '啊', '哦', '呀', '吧', '呢', '吗', '嗯', '哇'
        ]
        return set(stop_words)

    def load_data(self, data_dir='./data'):
        """批量加载data目录下的所有CSV文件"""
        print(f"正在从 {data_dir} 目录加载CSV文件...")

        # 获取所有CSV文件路径
        csv_files = glob.glob(os.path.join(data_dir, '*.csv'))

        if not csv_files:
            raise FileNotFoundError(f"在 {data_dir} 目录下未找到CSV文件")

        print(f"找到 {len(csv_files)} 个CSV文件:")
        for file in csv_files:
            print(f"  - {os.path.basename(file)}")

        # 批量读取并合并所有CSV文件
        dataframes = []
        total_rows = 0

        for csv_file in csv_files:
            try:
                df_temp = pd.read_csv(csv_file, encoding='utf-8')
                # 检查列名是否匹配预期格式
                expected_columns = ['序号', '上级评论ID', '评论ID', '用户ID', '用户名', '用户等级',
                                    '性别', '评论内容', '评论时间', '回复数', '点赞数', '个性签名',
                                    'IP属地', '是否是大会员', '头像']

                # 如果列名不完全匹配，尝试部分匹配或使用现有列
                if not all(col in df_temp.columns for col in expected_columns[:8]):  # 至少需要前8列
                    print(f"警告: {os.path.basename(csv_file)} 的列名可能不匹配，将尝试处理...")
                    print(f"实际列名: {list(df_temp.columns)}")

                dataframes.append(df_temp)
                rows = len(df_temp)
                total_rows += rows
                print(f"  ✓ {os.path.basename(csv_file)}: {rows} 条评论")

            except Exception as e:
                print(f"  ✗ 加载 {os.path.basename(csv_file)} 失败: {str(e)}")
                continue

        if not dataframes:
            raise ValueError("没有成功加载任何CSV文件")

        # 合并所有数据框
        self.df = pd.concat(dataframes, ignore_index=True)

        # 去重处理（基于评论ID）
        if '评论ID' in self.df.columns:
            original_len = len(self.df)
            self.df = self.df.drop_duplicates(subset=['评论ID'], keep='first')
            removed_duplicates = original_len - len(self.df)
            if removed_duplicates > 0:
                print(f"去除重复评论: {removed_duplicates} 条")

        print(f"\n数据加载完成!")
        print(f"总计: {len(self.df)} 条评论，来自 {len(csv_files)} 个文件")
        print(f"涉及用户数: {self.df['用户ID'].nunique() if '用户ID' in self.df.columns else '未知'}")

        # 显示数据基本信息
        print(f"\n数据概览:")
        print(self.df.info())

        return self.df

    def load_single_file(self, csv_file):
        """加载单个CSV文件（保留此方法以便向后兼容）"""
        self.df = pd.read_csv(csv_file)
        print(f"数据加载完成，共{len(self.df)}条评论")
        return self.df

    def preprocess_text(self, text):
        """文本预处理：中文分词、去停用词、去噪声"""
        if pd.isna(text):
            return ""

        # 去除特殊字符和数字
        text = re.sub(r'[^\u4e00-\u9fa5a-zA-Z]', ' ', str(text))

        # 中文分词
        words = jieba.cut(text)

        # 去停用词和长度过短的词
        words = [word.strip() for word in words
                 if word.strip() not in self.stop_words and len(word.strip()) > 1]

        return ' '.join(words)

    def extract_sentiment(self, text):
        """提取情感极性得分"""
        if pd.isna(text) or text == "":
            return 0.5

        try:
            s = SnowNLP(str(text))
            return s.sentiments
        except:
            return 0.5

    def data_preprocessing(self):
        """数据预处理"""
        print("开始数据预处理...")

        # 处理缺失值
        self.df['评论内容'] = self.df['评论内容'].fillna('')
        self.df['个性签名'] = self.df['个性签名'].fillna('')
        self.df['点赞数'] = pd.to_numeric(self.df['点赞数'], errors='coerce').fillna(0)
        self.df['回复数'] = pd.to_numeric(self.df['回复数'], errors='coerce').fillna(0)

        # 文本预处理
        self.df['处理后评论'] = self.df['评论内容'].apply(self.preprocess_text)
        self.df['处理后签名'] = self.df['个性签名'].apply(self.preprocess_text)

        # 情感分析
        self.df['情感得分'] = self.df['评论内容'].apply(self.extract_sentiment)

        # 处理时间
        self.df['评论时间'] = pd.to_datetime(self.df['评论时间'], errors='coerce')

        print("数据预处理完成")
        return self.df

    def feature_engineering(self):
        """特征工程"""
        print("开始特征工程...")

        # 用户维度聚合
        user_stats = self.df.groupby('用户ID').agg({
            '评论内容': 'count',  # 评论数量
            '点赞数': 'sum',  # 总点赞数
            '回复数': 'sum',  # 总回复数
            '情感得分': 'mean',  # 平均情感得分
            '处理后评论': lambda x: ' '.join(x),  # 所有评论合并
            '用户名': 'first',
            '用户等级': 'first',
            '性别': 'first',
            '个性签名': 'first',
            'IP属地': 'first',
            '是否是大会员': 'first'
        }).reset_index()

        user_stats.columns = ['用户ID', '评论数量', '总点赞数', '总回复数', '平均情感得分',
                              '所有评论', '用户名', '用户等级', '性别', '个性签名', 'IP属地', '是否是大会员']

        # 计算用户活跃度
        user_stats['活跃度得分'] = (user_stats['评论数量'] * 0.4 +
                                    user_stats['总点赞数'] * 0.3 +
                                    user_stats['总回复数'] * 0.3)

        # 情感倾向分类
        user_stats['情感倾向'] = user_stats['平均情感得分'].apply(
            lambda x: '积极' if x > 0.6 else ('消极' if x < 0.4 else '中性')
        )

        # 活跃等级分类
        activity_quantiles = user_stats['活跃度得分'].quantile([0.33, 0.67])
        user_stats['活跃等级'] = user_stats['活跃度得分'].apply(
            lambda x: '高活跃' if x > activity_quantiles[0.67] else (
                '中活跃' if x > activity_quantiles[0.33] else '低活跃'
            )
        )

        # TF-IDF特征提取
        self.tfidf_vectorizer = TfidfVectorizer(
            max_features=100,
            ngram_range=(1, 2),
            min_df=2
        )

        # 过滤空评论
        valid_comments = user_stats[user_stats['所有评论'].str.len() > 0]
        if len(valid_comments) > 0:
            tfidf_features = self.tfidf_vectorizer.fit_transform(valid_comments['所有评论'])
            tfidf_df = pd.DataFrame(
                tfidf_features.toarray(),
                columns=[f'tfidf_{i}' for i in range(tfidf_features.shape[1])]
            )
            tfidf_df['用户ID'] = valid_comments['用户ID'].values
            user_stats = user_stats.merge(tfidf_df, on='用户ID', how='left')
            user_stats = user_stats.fillna(0)

        self.user_features = user_stats
        print(f"特征工程完成，共生成{len(user_stats)}个用户画像")
        return user_stats

    def user_clustering(self, n_clusters=5):
        """用户聚类分析"""
        print("开始用户聚类...")

        # 选择数值特征进行聚类
        feature_cols = ['评论数量', '总点赞数', '总回复数', '平均情感得分', '活跃度得分']

        # 添加TF-IDF特征
        tfidf_cols = [col for col in self.user_features.columns if col.startswith('tfidf_')]
        if tfidf_cols:
            feature_cols.extend(tfidf_cols[:20])  # 取前20个TF-IDF特征

        X = self.user_features[feature_cols].fillna(0)

        # 标准化
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        # K-means聚类
        kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
        self.cluster_labels = kmeans.fit_predict(X_scaled)

        # 计算聚类效果
        silhouette_avg = silhouette_score(X_scaled, self.cluster_labels)
        print(f"K-means聚类完成，轮廓系数: {silhouette_avg:.3f}")

        # DBSCAN聚类对比
        dbscan = DBSCAN(eps=0.5, min_samples=5)
        dbscan_labels = dbscan.fit_predict(X_scaled)

        n_clusters_dbscan = len(set(dbscan_labels)) - (1 if -1 in dbscan_labels else 0)
        print(f"DBSCAN聚类完成，发现{n_clusters_dbscan}个簇")

        # 保存聚类结果
        self.user_features['聚类标签'] = self.cluster_labels
        self.user_features['DBSCAN标签'] = dbscan_labels

        return self.cluster_labels

    def analyze_clusters(self):
        """分析聚类结果"""
        print("\n聚类结果分析:")

        for cluster_id in range(max(self.cluster_labels) + 1):
            cluster_users = self.user_features[self.user_features['聚类标签'] == cluster_id]
            print(f"\n簇 {cluster_id} (用户数: {len(cluster_users)}):")
            print(f"  平均评论数: {cluster_users['评论数量'].mean():.1f}")
            print(f"  平均点赞数: {cluster_users['总点赞数'].mean():.1f}")
            print(f"  平均情感得分: {cluster_users['平均情感得分'].mean():.3f}")
            print(f"  活跃等级分布: {cluster_users['活跃等级'].value_counts().to_dict()}")
            print(f"  情感倾向分布: {cluster_users['情感倾向'].value_counts().to_dict()}")

    def visualize_results_v2(self):
        """结果可视化"""
        print("生成可视化图表...")

        # 1. 用户聚类分布
        plt.figure(figsize=(6, 6))
        cluster_counts = pd.Series(self.cluster_labels).value_counts().sort_index()
        plt.pie(cluster_counts.values, labels=[f'簇{i}' for i in cluster_counts.index], autopct='%1.1f%%')
        plt.title('用户聚类分布')
        plt.show()

        # 2. 情感倾向分布
        plt.figure(figsize=(6, 6))
        sentiment_counts = self.user_features['情感倾向'].value_counts()
        plt.bar(sentiment_counts.index, sentiment_counts.values)
        plt.title('情感倾向分布')
        plt.xlabel('情感倾向')
        plt.ylabel('用户数量')
        plt.show()

        # 3. 活跃等级分布
        plt.figure(figsize=(6, 6))
        activity_counts = self.user_features['活跃等级'].value_counts()
        plt.bar(activity_counts.index, activity_counts.values, color=['red', 'orange', 'green'])
        plt.title('活跃等级分布')
        plt.xlabel('活跃等级')
        plt.ylabel('用户数量')
        plt.show()

        # 4. 聚类散点图 (使用PCA降维)
        feature_cols = ['评论数量', '总点赞数', '总回复数', '平均情感得分', '活跃度得分']
        X = self.user_features[feature_cols].fillna(0)

        if len(X) > 1:
            pca = PCA(n_components=2)
            X_pca = pca.fit_transform(StandardScaler().fit_transform(X))

            plt.figure(figsize=(8, 6))
            scatter = plt.scatter(X_pca[:, 0], X_pca[:, 1], c=self.cluster_labels, cmap='viridis')
            plt.title('用户聚类可视化 (PCA)')
            plt.xlabel('主成分1')
            plt.ylabel('主成分2')
            plt.colorbar(scatter)
            plt.show()

        # 5. 活跃度 vs 情感得分散点图
        plt.figure(figsize=(8, 6))
        scatter2 = plt.scatter(self.user_features['活跃度得分'], self.user_features['平均情感得分'],
                               c=self.cluster_labels, cmap='viridis', alpha=0.6)
        plt.title('活跃度 vs 情感得分')
        plt.xlabel('活跃度得分')
        plt.ylabel('平均情感得分')
        plt.show()

        # 6. 评论数量分布直方图
        plt.figure(figsize=(8, 6))
        plt.hist(self.user_features['评论数量'], bins=30, alpha=0.7)
        plt.title('评论数量分布')
        plt.xlabel('评论数量')
        plt.ylabel('用户数量')
        plt.show()

        # 生成词云图
        self.generate_wordcloud()

    def visualize_results(self):
        """结果可视化"""
        print("生成可视化图表...")

        # 创建图表
        fig, axes = plt.subplots(2, 3, figsize=(18, 12))

        # 1. 用户聚类分布
        cluster_counts = pd.Series(self.cluster_labels).value_counts().sort_index()
        axes[0, 0].pie(cluster_counts.values, labels=[f'簇{i}' for i in cluster_counts.index],
                       autopct='%1.1f%%')
        axes[0, 0].set_title('用户聚类分布')

        # 2. 情感倾向分布
        sentiment_counts = self.user_features['情感倾向'].value_counts()
        axes[0, 1].bar(sentiment_counts.index, sentiment_counts.values)
        axes[0, 1].set_title('情感倾向分布')
        axes[0, 1].set_xlabel('情感倾向')
        axes[0, 1].set_ylabel('用户数量')

        # 3. 活跃等级分布
        activity_counts = self.user_features['活跃等级'].value_counts()
        axes[0, 2].bar(activity_counts.index, activity_counts.values,
                       color=['red', 'orange', 'green'])
        axes[0, 2].set_title('活跃等级分布')
        axes[0, 2].set_xlabel('活跃等级')
        axes[0, 2].set_ylabel('用户数量')

        # 4. 聚类散点图 (使用PCA降维)
        feature_cols = ['评论数量', '总点赞数', '总回复数', '平均情感得分', '活跃度得分']
        X = self.user_features[feature_cols].fillna(0)

        if len(X) > 1:
            pca = PCA(n_components=2)
            X_pca = pca.fit_transform(StandardScaler().fit_transform(X))

            scatter = axes[1, 0].scatter(X_pca[:, 0], X_pca[:, 1],
                                         c=self.cluster_labels, cmap='viridis')
            axes[1, 0].set_title('用户聚类可视化 (PCA)')
            axes[1, 0].set_xlabel('主成分1')
            axes[1, 0].set_ylabel('主成分2')
            plt.colorbar(scatter, ax=axes[1, 0])

        # 5. 活跃度vs情感得分散点图
        scatter2 = axes[1, 1].scatter(self.user_features['活跃度得分'],
                                      self.user_features['平均情感得分'],
                                      c=self.cluster_labels, cmap='viridis', alpha=0.6)
        axes[1, 1].set_title('活跃度 vs 情感得分')
        axes[1, 1].set_xlabel('活跃度得分')
        axes[1, 1].set_ylabel('平均情感得分')

        # 6. 评论数量分布直方图
        axes[1, 2].hist(self.user_features['评论数量'], bins=30, alpha=0.7)
        axes[1, 2].set_title('评论数量分布')
        axes[1, 2].set_xlabel('评论数量')
        axes[1, 2].set_ylabel('用户数量')

        plt.tight_layout()
        plt.show()

        # 生成词云图
        self.generate_wordcloud()

    def generate_wordcloud(self):
        """生成词云图"""
        print("生成词云图...")

        # 合并所有评论文本
        all_text = ' '.join(self.user_features['所有评论'].dropna())

        if all_text:
            # 生成词云
            wordcloud = WordCloud(
                font_path='simhei.ttf',  # 需要中文字体文件
                width=800, height=400,
                background_color='white',
                max_words=100,
                collocations=False
            ).generate(all_text)

            plt.figure(figsize=(12, 6))
            plt.imshow(wordcloud, interpolation='bilinear')
            plt.axis('off')
            plt.title('评论词云图', fontsize=16)
            plt.show()

    def classification_validation(self):
        """使用分类算法验证聚类效果"""
        print("进行分类验证...")

        # 准备分类特征
        feature_cols = ['评论数量', '总点赞数', '总回复数', '平均情感得分', '活跃度得分']
        X = self.user_features[feature_cols].fillna(0)
        y = self.cluster_labels

        # 分割数据集
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.3, random_state=42, stratify=y
        )

        # 标准化
        scaler = StandardScaler()
        X_train_scaled = scaler.fit_transform(X_train)
        X_test_scaled = scaler.transform(X_test)

        # 随机森林分类
        rf_model = RandomForestClassifier(n_estimators=100, random_state=42)
        rf_model.fit(X_train_scaled, y_train)
        rf_pred = rf_model.predict(X_test_scaled)

        print("\n随机森林分类结果:")
        print(classification_report(y_test, rf_pred))

        # SVM分类
        svm_model = SVC(kernel='rbf', random_state=42)
        svm_model.fit(X_train_scaled, y_train)
        svm_pred = svm_model.predict(X_test_scaled)

        print("\nSVM分类结果:")
        print(classification_report(y_test, svm_pred))

    def generate_user_tags(self):
        """生成用户标签"""
        print("生成用户标签...")

        # 计算点赞数的分位数阈值
        likes_threshold = self.user_features['总点赞数'].quantile(0.8)
        replies_threshold = self.user_features['总回复数'].quantile(0.7)

        # 为每个用户生成综合标签
        def create_user_tag(row):
            tags = []

            # 活跃度标签
            tags.append(row['活跃等级'])

            # 情感标签
            tags.append(f"{row['情感倾向']}用户")

            # 互动标签
            if row['总点赞数'] > likes_threshold:
                tags.append("高人气")

            if row['总回复数'] > replies_threshold:
                tags.append("互动达人")

            # 评论频率标签
            if row['评论数量'] >= 10:
                tags.append("活跃发言")
            elif row['评论数量'] >= 5:
                tags.append("适度发言")
            else:
                tags.append("偶尔发言")

            # 聚类标签
            tags.append(f"类型{row['聚类标签']}")

            return ', '.join(tags)

        self.user_features['用户标签'] = self.user_features.apply(create_user_tag, axis=1)

        # 显示标签示例
        print("\n用户标签示例:")
        sample_users = self.user_features[['用户名', '用户标签']].head(10)
        for _, user in sample_users.iterrows():
            print(f"{user['用户名']}: {user['用户标签']}")

        # 显示标签统计
        print(f"\n标签统计:")
        print(f"- 高人气用户: {(self.user_features['总点赞数'] > likes_threshold).sum()} 人")
        print(f"- 互动达人: {(self.user_features['总回复数'] > replies_threshold).sum()} 人")
        print(f"- 活跃发言用户: {(self.user_features['评论数量'] >= 10).sum()} 人")

    def export_results(self, output_dir='./results'):
        """导出分析结果到指定目录"""
        # 创建结果目录
        os.makedirs(output_dir, exist_ok=True)

        # 导出用户画像数据
        user_profile_file = os.path.join(output_dir, 'user_profiling_results.csv')
        self.user_features.to_csv(user_profile_file, index=False, encoding='utf-8-sig')
        print(f"用户画像结果已保存到: {user_profile_file}")

        # 导出聚类统计报告
        cluster_report_file = os.path.join(output_dir, 'cluster_analysis_report.txt')
        with open(cluster_report_file, 'w', encoding='utf-8') as f:
            f.write("B站用户聚类分析报告\n")
            f.write("=" * 50 + "\n\n")

            f.write(f"数据概况:\n")
            f.write(f"- 总用户数: {len(self.user_features)}\n")
            f.write(f"- 聚类数量: {len(set(self.cluster_labels))}\n\n")

            for cluster_id in range(max(self.cluster_labels) + 1):
                cluster_users = self.user_features[self.user_features['聚类标签'] == cluster_id]
                f.write(f"簇 {cluster_id} 分析 (用户数: {len(cluster_users)}):\n")
                f.write(f"  - 平均评论数: {cluster_users['评论数量'].mean():.1f}\n")
                f.write(f"  - 平均点赞数: {cluster_users['总点赞数'].mean():.1f}\n")
                f.write(f"  - 平均情感得分: {cluster_users['平均情感得分'].mean():.3f}\n")
                f.write(f"  - 活跃等级分布: {dict(cluster_users['活跃等级'].value_counts())}\n")
                f.write(f"  - 情感倾向分布: {dict(cluster_users['情感倾向'].value_counts())}\n\n")

        print(f"聚类分析报告已保存到: {cluster_report_file}")

        # 导出用户标签统计
        tag_stats_file = os.path.join(output_dir, 'user_tags_statistics.csv')
        if '用户标签' in self.user_features.columns:
            tag_stats = self.user_features['用户标签'].value_counts().reset_index()
            tag_stats.columns = ['用户标签', '用户数量']
            tag_stats.to_csv(tag_stats_file, index=False, encoding='utf-8-sig')
            print(f"用户标签统计已保存到: {tag_stats_file}")

        return output_dir


# 使用示例和批量处理函数
def batch_process_pipeline(data_dir='./data', output_dir='./results', n_clusters=5):
    """完整的批量处理流水线"""
    print("=" * 60)
    print("B站评论用户画像分析系统 - 批量处理模式")
    print("=" * 60)

    try:
        # 创建分析器实例
        profiler = BilibiliUserProfiler()

        # 1. 批量加载数据
        print("\n步骤 1: 批量加载数据")
        profiler.load_data(data_dir)

        # 2. 数据预处理
        print("\n步骤 2: 数据预处理")
        profiler.data_preprocessing()

        # 3. 特征工程
        print("\n步骤 3: 特征工程")
        profiler.feature_engineering()

        # 4. 用户聚类
        print("\n步骤 4: 用户聚类")
        profiler.user_clustering(n_clusters=n_clusters)

        # 5. 聚类结果分析
        print("\n步骤 5: 聚类结果分析")
        profiler.analyze_clusters()

        # 6. 生成用户标签
        print("\n步骤 6: 生成用户标签")
        profiler.generate_user_tags()

        # 7. 可视化分析
        print("\n步骤 7: 可视化分析")
        profiler.visualize_results_v2()

        # 8. 分类验证
        print("\n步骤 8: 分类算法验证")
        profiler.classification_validation()

        # 9. 导出结果
        print("\n步骤 9: 导出分析结果")
        profiler.export_results(output_dir)

        print("\n" + "=" * 60)
        print("批量处理完成! 🎉")
        print(f"结果文件保存在: {output_dir}")
        print("=" * 60)

        return profiler

    except Exception as e:
        print(f"\n❌ 处理过程中出现错误: {str(e)}")
        print("请检查数据文件格式和路径是否正确")
        return None

def main():
    if os.path.exists('./data'):
        print(f"\n检测到 ./data 目录存在，包含以下文件:")
        csv_files = glob.glob('./data/*.csv')
        for file in csv_files:
            print(f"  - {os.path.basename(file)}")

        print(f"\n可以直接运行: batch_process_pipeline() 开始分析")
        batch_process_pipeline()
    else:
        print(f"\n请确保将CSV文件放置在 ./data 目录下")


if __name__ == "__main__":
    main()