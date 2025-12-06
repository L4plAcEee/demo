#include <bits/stdc++.h>
using namespace std;

class Solution {
public:

    int mergeStones(vector<int>& stones, int k) {
        int n = stones.size();
        if ((n - 1) % (k - 1) != 0) return -1;
        vector<int> prefix_sum(n + 1, 0);
        for (int i = 0; i < n; ++i) {
            prefix_sum[i + 1] = prefix_sum[i] + stones[i];
        }

        vector<vector<int>> dp(n, vector<int>(n, 0));
        for (int i = n - 2, ans; i >= 0; --i) {
            for (int j = i + 1; j < n; ++j) {
                ans = INT_MAX;

                for (int m = i; m < j; m += k - 1) {
                    ans = min(ans, dp[i][m] + dp[m + 1][j]);
                }

                if ((j - i) % (k - 1) == 0) ans += prefix_sum[j + 1] - prefix_sum[i];
                dp[i][j] = ans;
            }
        }
        return dp[0][n - 1];
    }
};