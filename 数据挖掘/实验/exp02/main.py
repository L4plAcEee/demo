# main.py

import warnings

import matplotlib.pyplot as plt
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report
from sklearn.model_selection import train_test_split, GridSearchCV

# 全局忽略 SGDClassifier 的未收敛警告
warnings.filterwarnings("ignore", category=ConvergenceWarning)

# 字体配置
plt.rcParams['font.sans-serif'] = ['SimHei']
plt.rcParams['axes.unicode_minus'] = False

# 1. 读取 bankloan.xls 并划分数据集（8:2）
bankloan = pd.read_excel('./data/bankloan.xls', engine='xlrd')
# 列名示例：['年龄','教育','工龄','地址','收入','负债率','信用卡负债','其他负债','违约']
X_bank = bankloan.drop(columns=['违约'])
y_bank = bankloan['违约']
Xb_train, Xb_test, yb_train, yb_test = train_test_split(
    X_bank, y_bank, test_size=0.2, random_state=42, shuffle=True
)

# 2. 使用 Logistic 回归模型基线建模，并计算准确率
log_reg = LogisticRegression(max_iter=1000, random_state=42)
log_reg.fit(Xb_train, yb_train)
y_pred = log_reg.predict(Xb_test)
acc_baseline = accuracy_score(yb_test, y_pred)
print(f"Logistic 回归基线准确率：{acc_baseline:.4f}")

# 3. 调参：使用 GridSearchCV 优化正则化强度 C
param_grid = {'C': [0.01, 0.1, 1, 10, 100]}
grid_log = GridSearchCV(
    LogisticRegression(max_iter=1000, random_state=42),
    param_grid,
    cv=5,
    scoring='accuracy',
    n_jobs=-1
)
grid_log.fit(Xb_train, yb_train)
best_log = grid_log.best_estimator_
y_pred_best = best_log.predict(Xb_test)
acc_tuned = accuracy_score(yb_test, y_pred_best)
print(f"Logistic 最优 C：{grid_log.best_params_['C']}")
print(f"调参后准确率：{acc_tuned:.4f}")

# 4. 读取 bupa.data，并按要求划分（8:2）
# bupa.names 中字段：mcv, alkphos, sgpt, sgot, gammagt, drinks, selector
col_names = ['mcv', 'alkphos', 'sgpt', 'sgot', 'gammagt', 'drinks', 'selector']
bupa = pd.read_csv('./data/bupa.data', names=col_names)
# selector 原为 1=肝病，2=正常，转换为 0/1（二分类）
bupa['selector'] = bupa['selector'].map({1: 1, 2: 0})
X_bupa = bupa.drop(columns=['selector'])
y_bupa = bupa['selector']
Xbupa_train, Xbupa_test, ybupa_train, ybupa_test = train_test_split(
    X_bupa, y_bupa, test_size=0.2, random_state=42, shuffle=True
)

# 5. 选择 RandomForestClassifier 建模（适合捕捉非线性与特征交互）
rf = RandomForestClassifier(random_state=42)
rf.fit(Xbupa_train, ybupa_train)
y_pred_rf = rf.predict(Xbupa_test)
acc_rf = accuracy_score(ybupa_test, y_pred_rf)
print(f"RandomForest 基线准确率（Bupa 数据）：{acc_rf:.4f}")

# 6. RandomForest 参数调优
param_grid_rf = {
    'n_estimators': [50, 100, 200],
    'max_depth': [None, 5, 10]
}
grid_rf = GridSearchCV(
    RandomForestClassifier(random_state=42),
    param_grid_rf,
    cv=5,
    scoring='accuracy',
    n_jobs=-1
)
grid_rf.fit(Xbupa_train, ybupa_train)
best_rf = grid_rf.best_estimator_
y_pred_best_rf = best_rf.predict(Xbupa_test)
report_rf = classification_report(ybupa_test, y_pred_best_rf)
print(f"RandomForest 最优参数：{grid_rf.best_params_}")
print("RandomForest 调参后分类报告：")
print(report_rf)

# 7. 不同参数下的损失下降曲线（以 SGDClassifier 为例）
from sklearn.linear_model import SGDClassifier
from sklearn.metrics import log_loss

sgd = SGDClassifier(
    loss='log_loss',
    learning_rate='constant',
    eta0=0.01,
    max_iter=1,
    warm_start=True,
    random_state=42
)
loss_values = []
epochs = 50
for epoch in range(epochs):
    sgd.fit(Xb_train, yb_train)
    proba = sgd.predict_proba(Xb_test)
    loss = log_loss(yb_test, proba)
    loss_values.append(loss)

import matplotlib.pyplot as plt
plt.figure(figsize=(8, 5))
plt.plot(range(1, epochs+1), loss_values, marker='o')
plt.xlabel('迭代次数 (Epoch)')
plt.ylabel('对数损失 (Log Loss)')
plt.title('SGDClassifier 在 BankLoan 测试集上的损失下降曲线')
plt.grid(True)
plt.tight_layout()
plt.show()
