import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
# 设置中文字体和负号显示，以避免中文缺失警告
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False
from statsmodels.tsa.stattools import adfuller
from statsmodels.graphics.tsaplots import plot_acf, plot_pacf
from statsmodels.tsa.arima.model import ARIMA
from sklearn.preprocessing import MinMaxScaler

# 使用 PyTorch 实现 LSTM，无需 TensorFlow
import torch
import torch.nn as nn
from torch.utils.data import DataLoader, TensorDataset

class LSTMModel(nn.Module):
    def __init__(self, input_size=1, hidden_size=32, num_layers=1):
        super(LSTMModel, self).__init__()
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, 1)

    def forward(self, x):
        out, _ = self.lstm(x)
        # 只取最后时刻输出
        out = out[:, -1, :]
        out = self.fc(out)
        return out


def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)


def read_data(path):
    df = pd.read_csv(path, sep=';')
    df = df.apply(pd.to_numeric, errors='coerce')
    return df


def test_stationarity(series, title, out_dir):
    result = adfuller(series.dropna())
    print(f"{title} ADF Statistic: {result[0]:.4f}, p-value: {result[1]:.4f}")
    diff1 = series.diff().dropna()
    fig, axes = plt.subplots(2,1, figsize=(8,6))
    plot_acf(diff1, ax=axes[0], lags=20)
    axes[0].set_title(f'{title} 差分后 ACF')
    plot_pacf(diff1, ax=axes[1], lags=20)
    axes[1].set_title(f'{title} 差分后 PACF')
    fig.tight_layout()
    fig.savefig(os.path.join(out_dir, f"{title}_acf_pacf.png"))
    plt.close()


def fit_arima(series, order, title, out_dir):
    model = ARIMA(series, order=order)
    res = model.fit()
    pred = res.predict(start=series.index[0], end=series.index[-1], typ='levels')
    plt.figure(figsize=(8,4))
    plt.plot(series, label='实际值')
    plt.plot(pred, label='ARIMA预测')
    plt.legend()
    plt.title(f'{title} ARIMA {order} 预测对比')
    plt.savefig(os.path.join(out_dir, f"{title}_arima_pred.png"))
    plt.close()
    return res, pred


def fit_lstm_pytorch(series, look_back, title, out_dir, epochs=50, batch_size=16, lr=0.001):
    # 归一化
    scaler = MinMaxScaler()
    data = scaler.fit_transform(series.values.reshape(-1,1))
    # 构造数据集
    X, y = [], []
    for i in range(len(data) - look_back):
        X.append(data[i:i+look_back])
        y.append(data[i+look_back])
    X = np.array(X); y = np.array(y)
    dataset = TensorDataset(torch.from_numpy(X).float(), torch.from_numpy(y).float())
    loader = DataLoader(dataset, batch_size=batch_size, shuffle=False)

    # 模型、损失与优化器
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    model = LSTMModel().to(device)
    criterion = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=lr)

    history = []
    # 训练
    for epoch in range(epochs):
        epoch_loss = 0
        for xb, yb in loader:
            xb, yb = xb.to(device), yb.to(device)
            optimizer.zero_grad()
            pred = model(xb)
            loss = criterion(pred, yb)
            loss.backward()
            optimizer.step()
            epoch_loss += loss.item()
        history.append(epoch_loss/len(loader))
    # 绘制损失
    plt.figure(); plt.plot(history); plt.title(f'{title} LSTM(Pytorch) 训练损失'); plt.savefig(os.path.join(out_dir, f"{title}_lstm_loss.png")); plt.close()

    # 预测
    model.eval()
    X_tensor = torch.from_numpy(X).float().to(device)
    with torch.no_grad():
        y_pred = model(X_tensor).cpu().numpy()
    y_pred = scaler.inverse_transform(y_pred)
    actual = scaler.inverse_transform(y.reshape(-1,1))
    plt.figure(); plt.plot(actual, label='实际'); plt.plot(y_pred, label='LSTM预测'); plt.title(f'{title} LSTM(Pytorch) 预测对比'); plt.legend(); plt.savefig(os.path.join(out_dir, f"{title}_lstm_pred.png")); plt.close()
    return y_pred


def main():
    data_path = './data/Daily_Demand_Forecasting_Orders.csv'
    out_dir = './output'
    ensure_dir(out_dir)

    df = read_data(data_path)
    series_fiscal = df.iloc[:, 7]
    series_total  = df.iloc[:, 12]

    # 财政部门订单 ARIMA 分析与预测
    test_stationarity(series_fiscal, 'Fiscal_Orders', out_dir)
    fit_arima(series_fiscal, order=(1,1,1), title='Fiscal_Orders', out_dir=out_dir)

    # 总订单 ARIMA 分析与预测
    test_stationarity(series_total, 'Total_Orders', out_dir)
    fit_arima(series_total, order=(1,1,1), title='Total_Orders', out_dir=out_dir)

    # PyTorch LSTM 对比
    fit_lstm_pytorch(series_fiscal, look_back=5, title='Fiscal_Orders', out_dir=out_dir)
    fit_lstm_pytorch(series_total, look_back=5, title='Total_Orders', out_dir=out_dir)

if __name__ == '__main__':
    main()
