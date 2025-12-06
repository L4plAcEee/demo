import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import Lasso
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVR
from sklearn.neural_network import MLPRegressor
from sklearn.metrics import mean_squared_error, r2_score
import warnings

warnings.filterwarnings('ignore')

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False


class GreyModel:
    """灰色预测模型GM(1,1)实现"""

    def __init__(self):
        self.a = None
        self.b = None
        self.x0 = None
        self.x1 = None

    def fit(self, data):
        """拟合GM(1,1)模型"""
        self.x0 = np.array(data, dtype=float)
        n = len(self.x0)

        # 检查数据有效性
        if n < 4:
            raise ValueError("GM(1,1)模型至少需要4个数据点")

        # 一次累加生成
        self.x1 = np.cumsum(self.x0)

        # 构建数据矩阵
        B = np.zeros((n - 1, 2))
        Y = np.zeros(n - 1)

        for i in range(n - 1):
            B[i, 0] = -(self.x1[i] + self.x1[i + 1]) / 2
            B[i, 1] = 1
            Y[i] = self.x0[i + 1]

        # 使用更稳定的最小二乘求解方法
        try:
            # 使用伪逆矩阵求解，更稳定
            params = np.linalg.lstsq(B, Y, rcond=None)[0]
            self.a = params[0]
            self.b = params[1]
        except np.linalg.LinAlgError:
            # 如果仍然失败，使用简单的参数估计
            self.a = 0.1  # 默认发展系数
            self.b = np.mean(Y)  # 使用Y的均值

        # 确保参数合理性
        if abs(self.a) < 1e-10:
            self.a = 0.01 if self.a >= 0 else -0.01

        return self

    def predict(self, steps):
        """预测未来steps步的值"""
        if self.a is None or self.b is None:
            raise ValueError("模型未拟合，请先调用fit方法")

        n = len(self.x0)
        predictions = []

        # 获取最后一个累加值
        last_x1 = self.x1[-1]

        for k in range(1, steps + 1):
            # GM(1,1)预测公式 - 修正版
            try:
                if abs(self.a) > 1e-10:
                    x1_pred = (self.x0[0] - self.b / self.a) * np.exp(-self.a * (n + k - 1)) + self.b / self.a
                else:
                    # 当a接近0时，使用线性增长
                    x1_pred = last_x1 + self.b * k

                # 计算原始序列预测值
                if k == 1:
                    x0_pred = x1_pred - last_x1
                else:
                    x0_pred = x1_pred - x1_prev

                # 确保预测值合理
                if x0_pred < 0:
                    x0_pred = max(0, self.x0[-1] * (1 + 0.05))  # 使用5%的增长率作为备选

                predictions.append(x0_pred)
                x1_prev = x1_pred

            except (OverflowError, ZeroDivisionError):
                # 数值溢出时使用简单增长模型
                growth_rate = np.mean(np.diff(self.x0)) / np.mean(self.x0[:-1])
                x0_pred = self.x0[-1] * (1 + growth_rate) ** k
                predictions.append(x0_pred)

        return np.array(predictions)


def load_and_preprocess_data():
    """加载和预处理数据"""
    # 创建示例数据集（实际使用时请替换为真实的data.csv文件）
    np.random.seed(42)
    years = list(range(2000, 2014))  # 2000-2013年的历史数据
    n_years = len(years)

    # 模拟各种经济指标数据 - 更加稳定的数据生成
    base_values = {
        'gdp': 5000,
        'population': 800,
        'investment': 2000,
        'consumption': 1500,
        'export': 800,
        'import': 700,
        'industry_output': 1800
    }

    data = {'year': years}

    # 生成更平滑的时间序列数据
    for key, base_val in base_values.items():
        # 使用指数增长 + 小幅随机波动
        growth_rates = np.random.normal(0.05, 0.02, n_years)  # 年均5%增长，波动2%
        values = [base_val]
        for i in range(1, n_years):
            new_val = values[-1] * (1 + growth_rates[i])
            values.append(new_val)
        data[key] = values

    # 财政收入作为其他指标的线性组合 + 噪声
    fiscal_revenue = []
    for i in range(n_years):
        revenue = (0.2 * data['gdp'][i] +
                   0.15 * data['investment'][i] +
                   0.1 * data['consumption'][i] +
                   0.05 * data['industry_output'][i] +
                   np.random.normal(0, 50))
        fiscal_revenue.append(revenue)

    data['fiscal_revenue'] = fiscal_revenue

    df = pd.DataFrame(data)

    print("数据预览：")
    print(df.head())
    print(f"\n数据形状: {df.shape}")
    print(f"数据年份范围: {df['year'].min()} - {df['year'].max()}")

    return df


def lasso_feature_selection(df):
    """1. 使用Lasso回归进行特征选择"""
    print("\n" + "=" * 50)
    print("1. Lasso回归特征选择")
    print("=" * 50)

    # 准备特征和目标变量
    feature_cols = ['gdp', 'population', 'investment', 'consumption',
                    'export', 'import', 'industry_output']
    X = df[feature_cols].values
    y = df['fiscal_revenue'].values

    # 标准化特征
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # Lasso回归
    # alpha值较小以避免过度稀疏化
    lasso = Lasso(alpha=0.1, random_state=42)
    lasso.fit(X_scaled, y)

    # 获取特征重要性
    coefficients = lasso.coef_
    feature_importance = pd.DataFrame({
        'feature': feature_cols,
        'coefficient': coefficients,
        'abs_coefficient': np.abs(coefficients)
    }).sort_values('abs_coefficient', ascending=False)

    print("Lasso回归系数：")
    print(feature_importance)

    # 选择非零系数的特征作为关键属性
    selected_features = feature_importance[feature_importance['abs_coefficient'] > 0.01]['feature'].tolist()

    print(f"\n选中的关键属性: {selected_features}")
    print(f"关键属性数量: {len(selected_features)}")

    # 可视化特征重要性
    plt.figure(figsize=(10, 6))
    plt.barh(feature_importance['feature'], feature_importance['abs_coefficient'])
    plt.xlabel('系数绝对值')
    plt.title('Lasso回归特征重要性')
    plt.tight_layout()
    plt.show()

    return selected_features, scaler


def grey_prediction_features(df, selected_features):
    """2. 使用GM(1,1)预测关键属性值"""
    print("\n" + "=" * 50)
    print("2. GM(1,1)预测关键属性")
    print("=" * 50)

    predicted_features = {}

    for feature in selected_features:
        print(f"\n预测特征: {feature}")

        # 获取历史数据
        historical_data = df[feature].values

        try:
            # 拟合GM(1,1)模型
            gm = GreyModel()
            gm.fit(historical_data)

            # 预测2014、2015年的值
            predictions = gm.predict(2)
            predicted_features[feature] = predictions

            print(f"2014年预测值: {predictions[0]:.2f}")
            print(f"2015年预测值: {predictions[1]:.2f}")

            # 简化精度计算 - 避免递归拟合
            # 使用后验差比值检验模型精度
            fitted_predictions = []
            for i in range(len(historical_data) - 1):
                if gm.a != 0:
                    x1_fit = (historical_data[0] - gm.b / gm.a) * np.exp(-gm.a * i) + gm.b / gm.a
                    if i == 0:
                        x0_fit = x1_fit
                    else:
                        x0_fit = x1_fit - prev_x1
                    fitted_predictions.append(x0_fit)
                    prev_x1 = x1_fit
                else:
                    fitted_predictions.append(historical_data[i])

            if len(fitted_predictions) > 0:
                residuals = np.array(historical_data[1:len(fitted_predictions) + 1]) - np.array(fitted_predictions)
                mse = np.mean(residuals ** 2)
                print(f"模型MSE: {mse:.2f}")

                # 计算平均相对误差
                mape = np.mean(np.abs(residuals / historical_data[1:len(fitted_predictions) + 1])) * 100
                print(f"平均相对误差: {mape:.2f}%")

        except Exception as e:
            print(f"GM(1,1)拟合失败，使用线性外推: {e}")
            # 使用简单的线性外推作为备选方案
            if len(historical_data) >= 2:
                growth_rate = (historical_data[-1] - historical_data[-2]) / historical_data[-2]
                pred_2014 = historical_data[-1] * (1 + growth_rate)
                pred_2015 = pred_2014 * (1 + growth_rate)
                predicted_features[feature] = np.array([pred_2014, pred_2015])

                print(f"2014年预测值 (线性外推): {pred_2014:.2f}")
                print(f"2015年预测值 (线性外推): {pred_2015:.2f}")
            else:
                # 最后备选：使用最后一个值
                predicted_features[feature] = np.array([historical_data[-1], historical_data[-1]])
                print(f"使用最后已知值作为预测")

    return predicted_features


def svr_prediction(df, selected_features, predicted_features, scaler):
    """3. 使用支持向量回归预测财政收入"""
    print("\n" + "=" * 50)
    print("3. 支持向量回归预测财政收入")
    print("=" * 50)

    # 准备训练数据
    X_train = df[selected_features].values
    y_train = df['fiscal_revenue'].values

    # 标准化
    X_train_scaled = scaler.transform(X_train)

    # 训练SVR模型
    svr = SVR(kernel='rbf', C=100, gamma='scale', epsilon=0.1)
    svr.fit(X_train_scaled, y_train)

    # 准备预测数据
    X_pred = []
    for year in [2014, 2015]:
        year_features = []
        for feature in selected_features:
            if year == 2014:
                year_features.append(predicted_features[feature][0])
            else:
                year_features.append(predicted_features[feature][1])
        X_pred.append(year_features)

    X_pred = np.array(X_pred)
    X_pred_scaled = scaler.transform(X_pred)

    # 预测财政收入
    svr_predictions = svr.predict(X_pred_scaled)

    print(f"SVR预测2014年财政收入: {svr_predictions[0]:.2f}")
    print(f"SVR预测2015年财政收入: {svr_predictions[1]:.2f}")

    # 绘制预测结果
    years_all = list(df['year']) + [2014, 2015]
    revenue_all = list(df['fiscal_revenue']) + list(svr_predictions)

    plt.figure(figsize=(12, 6))
    plt.plot(df['year'], df['fiscal_revenue'], 'bo-', label='历史数据', linewidth=2)
    plt.plot([2014, 2015], svr_predictions, 'ro-', label='SVR预测', linewidth=2)
    plt.plot([2013, 2014], [df['fiscal_revenue'].iloc[-1], svr_predictions[0]], 'r--', alpha=0.5)
    plt.xlabel('年份')
    plt.ylabel('财政收入')
    plt.title('基于SVR的财政收入预测')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.show()

    return svr_predictions


def direct_grey_prediction(df):
    """4. 直接使用GM(1,1)预测财政收入"""
    print("\n" + "=" * 50)
    print("4. GM(1,1)直接预测财政收入")
    print("=" * 50)

    # 获取财政收入历史数据
    fiscal_revenue = df['fiscal_revenue'].values

    # 拟合GM(1,1)模型
    gm = GreyModel()
    gm.fit(fiscal_revenue)

    # 预测2014、2015年的值
    gm_predictions = gm.predict(2)

    print(f"GM(1,1)预测2014年财政收入: {gm_predictions[0]:.2f}")
    print(f"GM(1,1)预测2015年财政收入: {gm_predictions[1]:.2f}")

    # 绘制预测结果
    plt.figure(figsize=(12, 6))
    plt.plot(df['year'], df['fiscal_revenue'], 'bo-', label='历史数据', linewidth=2)
    plt.plot([2014, 2015], gm_predictions, 'go-', label='GM(1,1)预测', linewidth=2)
    plt.plot([2013, 2014], [df['fiscal_revenue'].iloc[-1], gm_predictions[0]], 'g--', alpha=0.5)
    plt.xlabel('年份')
    plt.ylabel('财政收入')
    plt.title('基于GM(1,1)的财政收入直接预测')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.show()

    return gm_predictions


def mlp_prediction(df, selected_features, predicted_features, scaler):
    """5. 使用多层感知机预测财政收入"""
    print("\n" + "=" * 50)
    print("5. 多层感知机(MLP)预测财政收入")
    print("=" * 50)

    # 准备训练数据
    X_train = df[selected_features].values
    y_train = df['fiscal_revenue'].values

    # 标准化
    X_train_scaled = scaler.transform(X_train)

    # MLP参数设置
    mlp_params = {
        'hidden_layer_sizes': (100, 50, 25),  # 3个隐藏层，分别有100、50、25个神经元
        'activation': 'relu',  # ReLU激活函数
        'solver': 'adam',  # Adam优化器
        'alpha': 0.01,  # L2正则化参数
        'learning_rate': 'adaptive',  # 自适应学习率
        'max_iter': 1000,  # 最大迭代次数
        'random_state': 42  # 随机种子
    }

    print("MLP网络结构参数:")
    print(f"- 输入层神经元数: {len(selected_features)}")
    print(f"- 隐藏层结构: {mlp_params['hidden_layer_sizes']}")
    print(f"- 输出层神经元数: 1")
    print(f"- 激活函数: {mlp_params['activation']}")
    print(f"- 优化器: {mlp_params['solver']}")
    print(f"- 正则化参数: {mlp_params['alpha']}")
    print(f"- 最大迭代次数: {mlp_params['max_iter']}")

    # 训练MLP模型
    mlp = MLPRegressor(**mlp_params)
    mlp.fit(X_train_scaled, y_train)

    print(f"实际迭代次数: {mlp.n_iter_}")
    print(f"训练损失: {mlp.loss_:.4f}")

    # 准备预测数据
    X_pred = []
    for year in [2014, 2015]:
        year_features = []
        for feature in selected_features:
            if year == 2014:
                year_features.append(predicted_features[feature][0])
            else:
                year_features.append(predicted_features[feature][1])
        X_pred.append(year_features)

    X_pred = np.array(X_pred)
    X_pred_scaled = scaler.transform(X_pred)

    # 预测财政收入
    mlp_predictions = mlp.predict(X_pred_scaled)

    print(f"\nMLP预测2014年财政收入: {mlp_predictions[0]:.2f}")
    print(f"MLP预测2015年财政收入: {mlp_predictions[1]:.2f}")

    # 绘制预测结果
    plt.figure(figsize=(12, 6))
    plt.plot(df['year'], df['fiscal_revenue'], 'bo-', label='历史数据', linewidth=2)
    plt.plot([2014, 2015], mlp_predictions, 'mo-', label='MLP预测', linewidth=2)
    plt.plot([2013, 2014], [df['fiscal_revenue'].iloc[-1], mlp_predictions[0]], 'm--', alpha=0.5)
    plt.xlabel('年份')
    plt.ylabel('财政收入')
    plt.title('基于MLP的财政收入预测')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.show()

    return mlp_predictions


def compare_results(svr_pred, gm_pred, mlp_pred):
    """比较不同模型的预测结果"""
    print("\n" + "=" * 50)
    print("预测结果比较")
    print("=" * 50)

    results_df = pd.DataFrame({
        '年份': [2014, 2015],
        'SVR预测': svr_pred,
        'GM(1,1)直接预测': gm_pred,
        'MLP预测': mlp_pred
    })

    print(results_df)

    # 综合比较图
    plt.figure(figsize=(12, 8))
    x = np.array([2014, 2015])

    plt.plot(x, svr_pred, 'ro-', label='SVR预测', linewidth=2, markersize=8)
    plt.plot(x, gm_pred, 'go-', label='GM(1,1)预测', linewidth=2, markersize=8)
    plt.plot(x, mlp_pred, 'mo-', label='MLP预测', linewidth=2, markersize=8)

    plt.xlabel('年份')
    plt.ylabel('财政收入')
    plt.title('三种模型预测结果比较')
    plt.legend()
    plt.grid(True, alpha=0.3)

    # 添加数值标签
    for i, year in enumerate([2014, 2015]):
        plt.text(year, svr_pred[i], f'{svr_pred[i]:.1f}', ha='center', va='bottom')
        plt.text(year, gm_pred[i], f'{gm_pred[i]:.1f}', ha='center', va='bottom')
        plt.text(year, mlp_pred[i], f'{mlp_pred[i]:.1f}', ha='center', va='bottom')

    plt.tight_layout()
    plt.show()


def main():
    """主函数"""
    print("地方财政收入预测实验")
    print("=" * 50)

    # 加载数据
    df = load_and_preprocess_data()

    # 1. Lasso特征选择
    selected_features, scaler = lasso_feature_selection(df)

    # 2. GM(1,1)预测关键属性
    predicted_features = grey_prediction_features(df, selected_features)

    # 3. SVR预测财政收入
    svr_predictions = svr_prediction(df, selected_features, predicted_features, scaler)

    # 4. GM(1,1)直接预测财政收入
    gm_predictions = direct_grey_prediction(df)

    # 5. MLP预测财政收入
    mlp_predictions = mlp_prediction(df, selected_features, predicted_features, scaler)

    # 比较结果
    compare_results(svr_predictions, gm_predictions, mlp_predictions)


if __name__ == "__main__":
    main()