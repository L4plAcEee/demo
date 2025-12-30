#include <functional>
#include <vector>
#include <algorithm>
#include <iostream>

using namespace std;

// 定义一个纯加法函数
function<int(int, int)> add = [](int a, int b) {
    return a + b;
};

// 定义一个高阶函数：将两个整数加起来再加上一个偏移值（偏移值由外部闭包捕获）
function<function<int(int, int)>(int)> makeAdderWithOffset = [](int offset) {
    return [=](int a, int b) {
        return a + b + offset;
    };
};

// 定义 map 函数：将函数作用于 vector 上的所有元素
function<vector<int>(vector<int>, function<int(int)>)> map = [](vector<int> v, function<int(int)> f) {
    vector<int> result;
    result.reserve(v.size());
    for (int x : v) {
        result.push_back(f(x));
    }
    return result;
};

// int main() {
//     // 构造加偏移函数
//     auto addWithOffset5 = makeAdderWithOffset(5);

//     // 调用纯函数
//     int result = addWithOffset5(2, 3);  // 2 + 3 + 5 = 10
    
//     cout << result << '\n';
    
//     // 创建输入数据
//     vector<int> data = {1, 2, 3, 4, 5};

//     // 使用 map 应用一个 lambda（平方函数）
//     auto squared = map(data, [](int x) { return x * x; });


//     return 0;
// }

#include <bits/stdc++.h>
using namespace std;

// C++ 的 Lambda 表达式默认不能直接递归调用自身，因为它没有名称可供调用。
// function<void(size_t)> loop = [&](size_t times){
//     if (times == 1) return;
//     else loop(times - 1);
// };

// 不能在定义时用 [&] 捕获自身变量 loop，因为此时 loop 还未完全定义
// function<void(size_t, const function<void(size_t)>&)> loop = [&](size_t times, const function<void(size_t)>& func) {
//     if (times == 0) return;
//     func(times - 1); // 可以改成 func(total - times) 实现正序
//     loop(times - 1, func);
// };

int32_t main() {
    function<void(size_t, const function<void(size_t)>&)> loop;
    loop = [&loop](size_t times, const function<void(size_t)>& func) {
        if (times == 0) return;
        func(times - 1);
        loop(times - 1, func);
    };
    loop(5, [](size_t i) {
        cout << "第 " << i << " 次调用" << endl;
    });

    function<void(size_t, size_t, const function<void(size_t)>&)> loop2;
    loop2 = [&](size_t i, size_t times, const function<void(size_t)>& func) {
        if (i >= times) return;
        func(i);
        loop2(i + 1, times, func);
    };

    loop2(0, 5, [](size_t i) {
        cout << "第 " << i << " 次调用" << endl;
    });
}