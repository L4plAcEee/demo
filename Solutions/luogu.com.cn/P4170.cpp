#include <bits/stdc++.h>
using namespace std;

string t;
// vector<vector<int>> dp(51, vector<int>(51, -1));
vector<vector<int>> dp(51, vector<int>(51, 0));

int f(int l, int r) {
    if (l > r) return 0;
    if (l == r) return 1;
    if (dp[l][r] != -1) return dp[l][r];
    dp[l][r] = t[l] == t[r] ? f(l , r - 1) : INT_MAX;
    for (int m = l; m < r; ++m) {
        dp[l][r] = min(dp[l][r], f(l, m) + f(m + 1, r));
    }
    return dp[l][r];
 }

void solve() {
    int len = t.length();
    for (int i = 0; i < len; ++i) 
        dp[i][i] = 1;
    
    for (int i = len - 1; i >= 0; --i) {
        for (int j = i + 1; j < len; ++j) {
            dp[i][j] = t[i] == t[j] ? dp[i][j - 1] : INT_MAX;
            for (int k = i; k < j; ++k) 
                dp[i][j] = min(dp[i][j], f(i, k) + f(k + 1, j));
        }
    }
    cout << dp[0][len - 1];
}


int main() {
    cin >> t;
    // cout << f(0, t.length() - 1) << '\n';
    solve();
}