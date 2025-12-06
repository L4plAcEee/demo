#include <bits/stdc++.h>
using namespace std;

class Solution {
public:
    int f(vector<int> &nums, vector<vector<int>> &dp, int l, int r) {
        if (l + 1 >= r) return 0; // 区间内无可戳气球
        if (dp[l][r] != -1) return dp[l][r];
        dp[l][r] = 0;
        for (int m = l + 1; m < r; ++m) {
            int coins = nums[l] * nums[m] * nums[r]; // m 是最后戳破的
            coins += f(nums, dp, l, m) + f(nums, dp, m, r);
            dp[l][r] = max(dp[l][r], coins);
        }
        return dp[l][r];
    }

    int maxCoins(vector<int>& nums) {
        vector<int> new_nums(nums.size() + 2, 1);
        for (int i = 0; i < nums.size(); ++i) {
            new_nums[i + 1] = nums[i];
        }
        int n = new_nums.size();
        vector<vector<int>> dp(n, vector<int>(n, -1));
        return f(new_nums, dp, 0, n - 1);
    }
};