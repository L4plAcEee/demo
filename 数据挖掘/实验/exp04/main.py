import os
# 解决 Windows 上 MKL 与 KMeans 的内存泄漏问题
os.environ['OMP_NUM_THREADS'] = '2'

import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from sklearn.manifold import TSNE
import matplotlib.pyplot as plt
import seaborn as sns

# 配置 matplotlib 支持中文
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

def load_data(path):
    """读取 CSV 数据文件"""
    df = pd.read_csv(path)
    return df


def preprocess(df):
    """对数据进行标准化处理"""
    scaler = StandardScaler()
    data_scaled = scaler.fit_transform(df)
    return data_scaled


def elbow_method(data, k_range, save_path=None):
    """手肘法：绘制不同 K 值下的 SSE 曲线"""
    sse = []
    for k in k_range:
        km = KMeans(n_clusters=k, init='k-means++', max_iter=300, random_state=42)
        km.fit(data)
        sse.append(km.inertia_)
    plt.figure()
    plt.plot(k_range, sse, 'o-')
    plt.xlabel('簇数 K')
    plt.ylabel('簇内 SSE')
    plt.title('Elbow Method')
    if save_path:
        plt.savefig(save_path)
    plt.show()
    return sse


def silhouette_method(data, k_range, save_path=None):
    """轮廓系数法：绘制不同 K 值下的平均轮廓系数"""
    # 跳过 k = 1，因为 silhouette_score 仅在 2 <= k <= n_samples-1 有效
    sil_scores = []
    sil_k = []
    for k in k_range:
        if k < 2:
            continue
        km = KMeans(n_clusters=k, init='k-means++', max_iter=300, random_state=42)
        labels = km.fit_predict(data)
        score = silhouette_score(data, labels)
        sil_scores.append(score)
        sil_k.append(k)
    plt.figure()
    plt.plot(sil_k, sil_scores, 'o-')
    plt.xlabel('簇数 K')
    plt.ylabel('平均轮廓系数')
    plt.title('Silhouette Method')
    if save_path:
        plt.savefig(save_path)
    plt.show()
    return sil_k, sil_scores


def run_kmeans(data, n_clusters, max_iter=100, random_state=42):
    """执行 K-Means 聚类并返回模型与标签"""
    km = KMeans(n_clusters=n_clusters, init='k-means++', max_iter=max_iter, random_state=random_state)
    labels = km.fit_predict(data)
    return km, labels


def plot_tsne(data, labels, title):
    """使用 t-SNE 降维并可视化聚类结果"""
    tsne = TSNE(n_components=2, random_state=42)
    emb = tsne.fit_transform(data)
    plt.figure()
    sns.scatterplot(x=emb[:,0], y=emb[:,1], hue=labels, palette='tab10', legend='full')
    plt.title(title)
    plt.show()


def plot_feature_pdfs(data_scaled, labels, features, title):
    """绘制各簇在指定特征上的核密度估计图"""
    df_scaled = pd.DataFrame(data_scaled, columns=features_all)
    df_scaled['cluster'] = labels
    for feat in features:
        plt.figure()
        sns.kdeplot(data=df_scaled, x=feat, hue='cluster', common_norm=False)
        plt.title(f'{title} - 特征 {feat} 核密度估计')
        plt.show()


def main():
    # 数据路径设置
    data_file = os.path.join(os.path.dirname(__file__), 'data', 'Wholesale customers data.csv')
    df = load_data(data_file)

    # 选择数值特征并标准化
    df_features = df.select_dtypes(include=[np.number])
    global features_all
    features_all = list(df_features.columns)
    data_scaled = preprocess(df_features)

    # 1. 手肘法确定 K
    k_range = range(1, 11)
    sse = elbow_method(data_scaled, k_range, save_path='elbow.png')
    k_opt = 3

    # 2-4. 不同最大迭代次数的聚类对比
    results = {}
    for max_iter in [100, 500, 1000]:
        km, labels = run_kmeans(data_scaled, n_clusters=k_opt, max_iter=max_iter)
        results[max_iter] = labels
        print(f'Max iter={max_iter}, SSE={km.inertia_}')

    # 5. t-SNE 可视化三次聚类结果
    for max_iter, labels in results.items():
        plot_tsne(data_scaled, labels, title=f'KMeans (K={k_opt}, max_iter={max_iter})')

    # 6. 使用核密度估计对比特征分布
    key_features = ['Fresh', 'Milk', 'Grocery']  # 可根据需求调整
    for max_iter, labels in results.items():
        plot_feature_pdfs(data_scaled, labels, key_features, title=f'迭代{max_iter}')

    # 7. 不同随机种子下聚类稳定性检验（max_iter=500）
    seeds = [0, 42, 123]
    for seed in seeds:
        km, labels = run_kmeans(data_scaled, n_clusters=k_opt, max_iter=500, random_state=seed)
        plot_tsne(data_scaled, labels, title=f'随机种子={seed}')

    # 8. 轮廓系数法确定 K 并可视化
    sil_k, sil_scores = silhouette_method(data_scaled, k_range, save_path='silhouette.png')
    # 选取轮廓系数最大的 K
    k_opt2 = sil_k[np.argmax(sil_scores)]
    print(f'轮廓系数法最优 K={k_opt2}')
    km2, labels2 = run_kmeans(data_scaled, n_clusters=k_opt2, max_iter=300)
    plot_tsne(data_scaled, labels2, title=f'Silhouette 确定 K={k_opt2}')


if __name__ == '__main__':
    main()
