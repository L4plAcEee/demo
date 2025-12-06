#include <bits/stdc++.h>
using namespace std;

class Solution {
public:
    int f(vector<int> &cuts, vector<vector<int>> &dp, int l, int r) {
        if (dp[l][r] != -1) return dp[l][r];
        if (l + 1 >= r) return dp[l][r] = 0;
        dp[l][r] = INT_MAX;
        for (int m = l + 1; m < r; ++m) {
            dp[l][r] = min(dp[l][r], f(cuts, dp, l, m) + f(cuts, dp, m, r) + cuts[r] - cuts[l]);
        }
        return dp[l][r];
    }
    int minCost(int n, vector<int>& cuts) {
        cuts.push_back(0);
        cuts.push_back(n);
        sort(cuts.begin(), cuts.end());
        vector<vector<int>> dp(cuts.size(), vector<int>(cuts.size(), -1));
        return f(cuts, dp, 0, cuts.size() - 1);
    }
};