#include <iostream>
#include <vector>
#include <cstdio>
#include <string>
using namespace std;

#define LOG(X, Y) cout << " [ "<< #X << " ] " << Y << '\n'
#define MAX_SIZE (int)1024

char buf[MAX_SIZE] = {0};
size_t ptr = MAX_SIZE / 2;

void run(const string& code) {
    vector<size_t> loop_stack;
    for (size_t pc = 0; pc < code.size(); ++pc) {
        char cmd = code[pc];
        switch (cmd) {
            case '>': 
                if (ptr + 1 < MAX_SIZE) ptr++; break;
            case '<': 
                if (ptr > 0) ptr--; break;
            case '+': 
                buf[ptr]++; break;
            case '-': 
                buf[ptr]--; break;
            case '.': 
                putchar(buf[ptr]); break;
            case ',': 
                buf[ptr] = getchar(); break;
            case '[':
                if (buf[ptr] == 0) {
                    int level = 1;
                    while (level && ++pc < code.size()) {
                        if (code[pc] == '[') level++;
                        else if (code[pc] == ']') level--;
                    }
                } else {
                    loop_stack.push_back(pc);
                }
                break;
            case ']':
                if (buf[ptr] != 0) {
                    pc = loop_stack.back();  // 跳回对应 [
                } else {
                    loop_stack.pop_back();   // 退出循环
                }
                break;
        }
    }
}

int main() {
    string code;
    getline(cin, code);
    run(code);
    return 0;
}
