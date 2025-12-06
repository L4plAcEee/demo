#include <bits/stdc++.h>
using namespace std;

struct TrieNode {
    int son[26] = {0};  // 子节点
    int fail = 0;       // fail指针
    int id = -1;        // 该节点是否是一个模式串的结尾，若是，记录其模式串编号
};

TrieNode tr[10000];
int tot = 0;
vector<int> matchCnt;   // 匹配次数

// 插入模式串
void insert(const string& s, int idx) {
    int u = 0;
    for (char ch : s) {
        int c = ch - 'a';
        if (!tr[u].son[c]) tr[u].son[c] = ++tot;
        u = tr[u].son[c];
    }
    tr[u].id = idx; // 标记该节点为模式串结尾
}

// 构建 fail 指针
void build() {
    queue<int> q;
    for (int i = 0; i < 26; i++)
        if (tr[0].son[i]) q.push(tr[0].son[i]);

    while (!q.empty()) {
        int u = q.front(); q.pop();
        for (int i = 0; i < 26; i++) {
            if (tr[u].son[i]) {
                tr[tr[u].son[i]].fail = tr[tr[u].fail].son[i];
                q.push(tr[u].son[i]);
            } else {
                tr[u].son[i] = tr[tr[u].fail].son[i];
            }
        }
    }
}

// 查询文本并统计每个模式串匹配次数
void query(const string& t) {
    int u = 0;
    for (char ch : t) {
        int c = ch - 'a';
        u = tr[u].son[c];

        for (int j = u; j; j = tr[j].fail) {
            if (tr[j].id != -1) {
                matchCnt[tr[j].id]++;
            }
        }
    }
}

int main() {
    vector<string> patterns = {"he", "she", "his", "hers"};
    string text = "ushers";

    matchCnt.resize(patterns.size(), 0);
    for (int i = 0; i < patterns.size(); ++i)
        insert(patterns[i], i);

    build();
    query(text);

    for (int i = 0; i < patterns.size(); ++i)
        cout << "模式串 \"" << patterns[i] << "\" 出现了 " << matchCnt[i] << " 次" << endl;

    return 0;
}
