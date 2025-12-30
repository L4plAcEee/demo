#pragma once
#include <cstdint>
#include <vector>
#include <iostream>
#include <random>
#include <unordered_map>
#include <algorithm>
#include <limits>
#include <vector>

#define LOG(X, S) std::cout << "[LOGGING!!!] [" << #X << "] : " << S << "  " << std::endl

#define S_5 (int)1145141919

#define S_4 (int)1e5

#define B_4 (int)1e4

#define S_3 (int)1e3

#define B_3 (int)1e2
#define S_2 (int)1e2

#define B_2 (int)1e1

#define BOARD_SIZE (int)15

enum PlayerType {
    EMPTY = -1,
    BLACK,
    WHITE
};

enum Role {
    NONE = -1,
    HUMAN,
    AI
};

struct Move {
    int x {}, y {};
    Role role {};
    Move() = default;
    Move(int x, int y) : x(x), y(y) {}
    Move(int x, int y, Role r) : x(x), y(y), role(r) {}
};

std::vector<std::pair<int, int>> all_dir = {
    {0, 1}, 
    {1, 0}, 
    {1, 1}, 
    {1, -1},
    {0, -1}, 
    {-1, 0}, 
    {-1, -1},
    {-1, 1}
};

std::vector<std::vector<int>> dir = {
    {0, 1}, // 右
    {1, 1}, // 右下
    {1, 0}, // 下
    {1, -1} // 左下
};


