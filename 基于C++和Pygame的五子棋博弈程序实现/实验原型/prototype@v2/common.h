#ifndef COMMON_H
#define COMMON_H

#include <vector>

#define S_5 (int)1145141919

#define S_4 (int)1e5

#define B_4 (int)1e4

#define S_3 (int)1e3

#define B_3 (int)1e2
#define S_2 (int)1e2

#define B_2 (int)1e1

#define BOARD_SIZE (int)15

enum class PlayerType {
    EMPTY = -1,
    BLACK,
    WHITE
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


#endif // COMMON_H