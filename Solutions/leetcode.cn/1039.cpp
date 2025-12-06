#include <bits/stdc++.h>
using namespace std;

class Solution {
public:
    int f(vector<int> &v, vector<vector<int>> &dp, int l, int r) {
        if (dp[l][r] != -1) return dp[l][r];
        if (r - l < 2) return dp[l][r] = 0;
        int ans = INT_MAX;
        for (int m = l + 1; m < r; ++m) 
            ans = min(ans, f(v, dp, l, m) + f(v, dp, m, r) + v[l] * v[m] * v[r]);
        return dp[l][r] = ans;
    }
    int dp(vector<int> &v) {
        int n = v.size();
        vector<vector<int>> dp(n, vector<int>(n, 0));
        for (int i = n - 1; i >= 0; --i) {
            for (int j = i; j < n; ++j) {
                if (j - i < 2) continue;
                dp[i][j] = INT_MAX;
                for (int m = i + 1; m < j; ++m) 
                    dp[i][j] = min(dp[i][j], dp[i][m] + dp[m][j] + v[i] * v[m] * v[j]);
            }
        }
        return dp[0][n - 1];
    }
    int minScoreTriangulation(vector<int>& values) {
        // vector<vector<int>> dp(values.size(), vector<int>(values.size(), -1));
        // return f(values, dp, 0, values.size() - 1);
        return dp(values);
    }
};