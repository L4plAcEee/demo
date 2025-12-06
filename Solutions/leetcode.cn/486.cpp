#include <bits/stdc++.h>
using namespace std;

class Solution {
public:

    int dp(vector<int> &nums) {
        vector<vector<int>> dp(nums.size(), vector<int>(nums.size(), 0));
        for (int i = 0; i < nums.size(); ++i) dp[i][i] = nums[i];

        for (int i = nums.size() - 1; i >= 0; --i) {
            for (int j = i; j < nums.size(); ++j) {
                if (i == j) continue;
                dp[i][j] = max(-dp[i + 1][j] + nums[i], -dp[i][j - 1] + nums[j]);
            }
        }
        return dp[0][nums.size() - 1];
    }
    bool predictTheWinner(vector<int>& nums) {
        int player1 = dp(nums);
        return reduce(nums.begin(), nums.end()) - player1 < player1;
    }
};


int main() {
    vector<int> a = {1, 5, 2};
    Solution sol;
    cout << sol.predictTheWinner(a) << '\n';
}