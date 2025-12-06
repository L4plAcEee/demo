#include <bits/stdc++.h>
using namespace std;

int a[200005];

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int t; cin >> t;
    while (t--) {
        int n, j, k;
        cin >> n >> j >> k;

        int max_val = 0;
        for (int i = 0; i < n; ++i) {
            cin >> a[i];
            max_val = max(max_val, a[i]);
        }
        if (k > 1) {
            cout << "Yes" << '\n';
            continue;
        }
        int aj = a[j - 1];
        cout << (aj == max_val ? "Yes" : "No") << '\n';
    }
}
