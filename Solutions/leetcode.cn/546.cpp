#include <bits/stdc++.h>
using namespace std;

class Solution {
public:
    int removeBoxes(vector<int>& boxes) {
        int n = boxes.size();
        // 三维记忆化数组：dp[l][r][k] 表示在 boxes[l..r] 范围上，并且前面跟着 k 个和 boxes[l] 同颜色的盒子时的最大得分
        vector<vector<vector<int>>> dp(n, vector<vector<int>>(n, vector<int>(n, 0)));
        return f(boxes, 0, n - 1, 0, dp);
    }

private:
    int f(const vector<int>& boxes, int l, int r, int k, vector<vector<vector<int>>>& dp) {
        if (l > r) return 0;
        if (dp[l][r][k] > 0) return dp[l][r][k];

        int s = l;
        // 将起始位置往右推进，把与 boxes[l] 相同的连续部分合并成一块
        while (s + 1 <= r && boxes[s + 1] == boxes[l]) ++s;

        // cnt 是当前颜色段的总数：前缀 k 个 + 当前段 [l..s] 的 (s - l + 1) 个
        int cnt = k + (s - l + 1);

        // 可能性1：当前这 cnt 个盒子直接一起消除，然后继续处理后面的
        int ans = cnt * cnt + f(boxes, s + 1, r, 0, dp);

        // 可能性2：将这段延后消除 —— 找到后面和 boxes[l] 相同颜色的盒子，把中间的先处理完
        for (int m = s + 2; m <= r; ++m) {
            if (boxes[m] == boxes[l] && boxes[m - 1] != boxes[m]) {
                // 剪枝条件：只有 boxes[m] 与前一个不相等时才进入递归，避免重复分段
                ans = max(ans,
                          f(boxes, s + 1, m - 1, 0, dp) +  // 中间部分先移除
                          f(boxes, m, r, cnt, dp));        // 再合并两段消除
            }
        }

        dp[l][r][k] = ans;
        return ans;
    }
};
