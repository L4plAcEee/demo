#pragma once
#include "Common.h"

class Board {
public:
    static constexpr int SIZE = 15;
    std::vector<std::vector<int>> board;
    Move lastMove;
    static Board& getInstance() {
        static Board instance;
        return instance;
    }

    // 打印棋盘（带坐标轴）
    void print() const {
        std::cout << "   ";
        for (int i = 0; i < SIZE; ++i)
            std::cout << static_cast<char>('A' + i) << ' ';
        std::cout << '\n';

        for (int i = 0; i < SIZE; ++i) {
            std::cout << (i < 10 ? " " : "") << i << ' ';
            for (int j = 0; j < SIZE; ++j) {
                char c = '.';
                if (board[i][j] == 0) c = 'X'; // Black stone
                else if (board[i][j] == 1) c = 'O'; // White stone
                std::cout << c << ' ';
            }
            std::cout << '\n';
        }
    }

    // 落子，成功返回 true
    bool place(int x, int y, int role) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || board[x][y] != -1)
            return false;
        board[x][y] = role;
        lastMove = {x, y, static_cast<Role>(role)};
        return true;
    }

    // 判断是否胜利（五子连珠）
    bool checkWin(int x, int y) const {
        int role = board[x][y];
        if (role == -1) return false;

        const int dx[4] = {1, 0, 1, 1};
        const int dy[4] = {0, 1, 1, -1};

        for (int dir = 0; dir < 4; ++dir) {
            int cnt = 1;
            for (int step = 1; step < 5; ++step) {
                int nx = x + dx[dir] * step;
                int ny = y + dy[dir] * step;
                if (nx >= 0 && ny >= 0 && nx < SIZE && ny < SIZE && board[nx][ny] == role)
                    cnt++;
                else break;
            }
            for (int step = 1; step < 5; ++step) {
                int nx = x - dx[dir] * step;
                int ny = y - dy[dir] * step;
                if (nx >= 0 && ny >= 0 && nx < SIZE && ny < SIZE && board[nx][ny] == role)
                    cnt++;
                else break;
            }
            if (cnt >= 5) return true;
        }
        return false;
    }

    void reset() {
        for (auto& row : board)
            std::fill(row.begin(), row.end(), -1);
    }

private:
    Board() {
        board.resize(SIZE, std::vector<int>(SIZE, -1));
    }

};
