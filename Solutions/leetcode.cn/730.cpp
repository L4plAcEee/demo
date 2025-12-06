#include <bits/stdc++.h>
using namespace std;
const int MOD = 1e9 + 7;
class Solution {
public:
    int countPalindromicSubsequences(string s) {
        #define int long long
        int len = s.length();
        vector<vector<int>> dp(len, vector<int>(len, 0));
        for (int i = 0; i < len; ++i) dp[i][i] = 1;
        for (int i = len - 2; i >= 0; --i) {
            for (int j = i + 1; j < len; ++j) {
                if (s[i] != s[j]) {
                    dp[i][j] = dp[i][j - 1] % MOD + dp[i + 1][j] % MOD - dp[i + 1][j - 1] % MOD;
                } else {
                    int l = i + 1;
                    int r = j - 1;
                    while (s[i] != s[l] && l < j) l++;
                    while (s[i] != s[r] && r > i) r--;
                    if (l > r) dp[i][j] = dp[i + 1][j - 1] * 2 % MOD + 2 % MOD;
                    else if (l == r) dp[i][j] = dp[i + 1][j - 1] * 2 % MOD + 1 % MOD;
                    else dp[i][j] = dp[i + 1][j - 1] * 2 % MOD - dp[l + 1][r - 1] % MOD;
                }
                dp[i][j] = (dp[i][j] + MOD) % MOD;
            }
        }
        #undef int long long
        return dp[0][len - 1];
    }
};