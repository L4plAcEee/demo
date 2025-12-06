#include <bits/stdc++.h>
using namespace std;

bool match(char a, char b) {
    return (a == '(' && b == ')') || (a == '[' && b == ']');
}

int f(string &s,vector<vector<int>> &dp, int l, int r) {
    if (l == r) return 1;
    if (l == r - 1) return match(s[l], s[r]) ? 0 : 2;
    if (dp[l][r]  != -1) return dp[l][r];
    int p = match(s[l], s[r]) ? f(s, dp, l + 1, r - 1) : INT_MAX;
    for (int m = l; m < r; ++m) {
        p = min(p, f(s, dp, l ,m) + f(s, dp, m + 1, r));
    }
    return dp[l][r] = p;
}

int dp(string &s) {
    int len = s.length();
    vector<vector<int>> dp(len, vector<int>(len, 0));
    for (int i = 0; i < len; ++i) {
        for (int j = 0; j < len; ++j) {
            if (i == j) dp[i][j] = 1;
            else if (i == j - 1) dp[i][j] = match(s[i], s[j]) ? 0 : 2;
        }
    }
    for (int i = len - 1; i >= 0; --i) {
        for (int j = i + 1; j < len; ++j) {
            dp[i][j] = match(s[i], s[j]) ? dp[i + 1][j - 1] : INT_MAX;
            for (int k = i; k < j; ++k) {
                dp[i][j] = min(dp[i][j], dp[i][k] + dp[k + 1][j]);
            }
        }
    }
    return dp[0][len - 1];
}

int main() {
    string s;
    cin >> s;
    // vector<vector<int>> dp(s.length(), vector<int>(s.length(), -1));
    // cout << f(s, dp, 0, s.length() - 1);
    cout << dp(s) << '\n';
}
// 64 位输出请用 printf("%lld")