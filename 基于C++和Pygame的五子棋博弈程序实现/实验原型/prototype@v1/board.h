#ifndef BOARD_H
#define BOARD_H
#include <iostream>
#include <vector>
#include <random>
#include <cstdint>

#define BOARD_SIZE (uint64_t)15

enum class player_t {
    EMPTY = -1,
    BLACK,
    WHITE
};

std::vector<std::vector<int>> dir = {
    {0, 1},
    {1, 1},
    {1, 0},
    {1, -1}
};

class Board {
private:
    uint64_t currentRound = 0;
    uint64_t zobristHash = 0LL;
    std::vector<std::vector<std::vector<uint64_t>>> zobristTable;

public:
    std::vector<std::vector<player_t>> board;
    Board() : board() {
        board.resize(BOARD_SIZE, std::vector<player_t>(BOARD_SIZE, player_t::EMPTY));
        zobristTable.resize(BOARD_SIZE, std::vector<std::vector<uint64_t>>(BOARD_SIZE, std::vector<uint64_t>(2)));
        std::mt19937_64 rng(std::random_device{}());
        std::uniform_int_distribution<uint64_t> dist;

        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                for (int p = 0; p < 2; ++p) {
                    zobristTable[x][y][p] = dist(rng);
                }
            }
        }
    }

    void reset() {
        zobristHash = 0LL;
        for (auto& row : board) {
            std::fill(row.begin(), row.end(), player_t::EMPTY);
        }
    }

    bool place(uint64_t x, uint64_t y, player_t player) {
        if (x >= BOARD_SIZE || y >= BOARD_SIZE || board[x][y] != player_t::EMPTY) {
            std::cerr << "非法的移动 " << x << ",  " << y << " 位置已被占用或越界。" << std::endl;
            return false;
        }
        board[x][y] = player;
        zobristHash ^= zobristTable[x][y][static_cast<int>(player)];
        currentRound++;
        return true;
    }

    void undo(uint64_t x, uint64_t y, player_t player) {
        board[x][y] = player_t::EMPTY;
        zobristHash ^= zobristTable[x][y][static_cast<int>(player)];
        currentRound--;
    }

    bool checkWin(uint64_t x, uint64_t y, player_t player) const {
        for (const auto &d : dir) {
            int count = 1;
            for (int i = 1; i < 5; ++i) {
                uint64_t nx = x + d[0] * i;
                uint64_t ny = y + d[1] * i;
                if (nx < BOARD_SIZE && ny < BOARD_SIZE && board[nx][ny] == player) {
                    count++;
                } else {
                    break;
                }
            }
            for (int i = 1; i < 5; ++i) {
                uint64_t nx = x - d[0] * i;
                uint64_t ny = y - d[1] * i;
                if (nx < BOARD_SIZE && ny < BOARD_SIZE && board[nx][ny] == player) {
                    count++;
                } else {
                    break;
                }
            }
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    uint64_t getZobristHash() const {
        return zobristHash;
    }

    uint64_t getCurrentRound() const {
        return currentRound;
    }
};

#endif // BOARD_H