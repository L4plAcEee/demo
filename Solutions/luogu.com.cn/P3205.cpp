#include <bits/stdc++.h>
using namespace std;

const int MOD = 19650827;

int main() {
    int n;
    cin >> n;
    vector<int> arr(n);
    for (int i = 0; i < n; ++i) cin >> arr[i];
    vector<vector<vector<int>>> dp(n, vector<vector<int>>(n, vector<int>(2, 0)));
    for (int i = 0; i < n; ++i) if (arr[i] < arr[i+1]) dp[i][i+1][0] = dp[i][i+1][1] = 1;



    for (int i = n - 1; i >= 0; --i) {
        for (int j = i + 2; j < n; ++j) {
            if (arr[i] < arr[i + 1]) dp[i][j][0] = (dp[i][j][0] + dp[i + 1][j][0]) % MOD; 
            if (arr[i] < arr[j]) dp[i][j][0] = (dp[i][j][0] + dp[i + 1][j][1]) % MOD;
            if (arr[j] > arr[j - 1]) dp[i][j][1] = (dp[i][j][1] + dp[i][j - 1][1]) % MOD;
            if (arr[j] > arr[i]) dp[i][j][1] = (dp[i][j][1] + dp[i][j - 1][0]) % MOD;
        }
    }

    cout << (dp[0][n - 1][0] + dp[0][n - 1][1]) % MOD<< '\n';
}