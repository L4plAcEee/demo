#pragma once
#include <cstdint>
#include <vector>
#include <iostream>
#include <random>
#include <unordered_map>
#include <set>
#include <unordered_set>
#include <algorithm>
#include <limits>
#include <vector>
#include <chrono>

#define LOG(Level, S) std::cout << "[调试信息] : [" << #Level << "] : " << S << "  " << std::endl

#define S_5 (int)1145141919

#define S_4 (int)1e5

#define B_4 (int)1e4

#define S_3 (int)1e3

#define B_3 (int)1e2
#define S_2 (int)1e2

#define B_2 (int)1e1

#define BOARD_SIZE (int)15

enum Role {
    NONE = -1,
    HUMAN,
    AI
};

struct Move {
    int x {}, y {};
    Move() = default;
    Move(int x, int y) : x(x), y(y) {}
    bool operator<(const Move& other) const {
        if (x == other.x) return y < other.y;
        return x < other.x;
    }
    bool operator==(const Move& other) const {
        return x == other.x && y == other.y;
    }
    size_t operator()(const Move& m) const {
        return std::hash<int>()(m.x) ^ (std::hash<int>()(m.y) << 1);
    }
};

namespace std {
    template<>
    struct hash<Move> {
        size_t operator()(const Move& m) const noexcept {
            return std::hash<int>()(m.x) ^ (std::hash<int>()(m.y) << 1);
        }
    };
}

int all_dir[8][2] = {
    {0, 1}, 
    {1, 0}, 
    {1, 1}, 
    {1, -1},
    {0, -1}, 
    {-1, 0}, 
    {-1, -1},
    {-1, 1}
};

int dir[4][2] = {
    {0, 1}, // 右
    {1, 1}, // 右下
    {1, 0}, // 下
    {1, -1} // 左下
};

