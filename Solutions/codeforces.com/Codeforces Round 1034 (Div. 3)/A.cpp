#include <bits/stdc++.h>
using namespace std;


int main() {
    int n, t;
    cin >> t;
    while (t--) {
        cin >> n;
        cout << ((n % 4) == 0 ? "Bob\n" : "Alice\n");
    }
}
