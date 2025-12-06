#ifndef AI_H
#define AI_H
#include "board.h"


#include <unordered_map>
#include <limits>

class AI {
public:
    AI(Board& board, int maxDepth = 3) : board(board), maxDepth(maxDepth) {
        badGrid = std::vector<std::vector<bool>>(BOARD_SIZE, std::vector<bool>(BOARD_SIZE, true));
    }
    // AI主逻辑
    std::pair<uint64_t, uint64_t> makeMove(player_t player) {
        if (board.getCurrentRound() <= 3) {
            return startStrategy(player);
        }
        // 正常alpha-beta搜索
        int bestScore = std::numeric_limits<int>::min();
        std::pair<uint64_t, uint64_t> bestMove = {0, 0};
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player_t::EMPTY && !badGrid[x][y]) {
                    if (board.place(x, y, player)) {
                        int score = alphaBetaSearch(maxDepth - 1, switchPlayer(player), std::numeric_limits<int>::min(), std::numeric_limits<int>::max());
                        board.undo(x, y, player);
                        if (score > bestScore) {
                            bestScore = score;
                            bestMove = {x, y};
                        }
                    }
                }
            }
        }
        board.place(bestMove.first, bestMove.second, player);
        return bestMove;
    }

    void updateBadGridAfterMove(uint64_t x, uint64_t y) {
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dy = -3; dy <= 3; ++dy) {
                int nx = (int)x + dx;
                int ny = (int)y + dy;
                if (nx >= 0 && ny >= 0 && nx < (int)BOARD_SIZE && ny < (int)BOARD_SIZE) {
                    badGrid[nx][ny] = false;
                }
            }
        }
    }

private:
    Board& board;
    int maxDepth;
    std::unordered_map<uint64_t, int> transpositionTable;
    // badGrid: true表示该点为bad，不考虑落子
    std::vector<std::vector<bool>> badGrid;

    player_t switchPlayer(player_t p) {
        return (p == player_t::BLACK) ? player_t::WHITE : player_t::BLACK;
    }

    std::pair<uint64_t, uint64_t> startStrategy(player_t player) {
        // 搜索所有黑子周围一格的空位
        std::vector<std::pair<uint64_t, uint64_t>> candidates;
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player_t::BLACK) {
                    for (int dx = -1; dx <= 1; ++dx) {
                        for (int dy = -1; dy <= 1; ++dy) {
                            if (dx == 0 && dy == 0) continue;
                            uint64_t nx = x + dx;
                            uint64_t ny = y + dy;
                            if (nx < BOARD_SIZE && ny < BOARD_SIZE && board.board[nx][ny] == player_t::EMPTY && !badGrid[nx][ny]) {
                                candidates.emplace_back(nx, ny);
                            }
                        }
                    }
                }
            }
        }
        // 若有候选点，随机选一个
        if (!candidates.empty()) {
            return candidates[rand() % candidates.size()];
        }
        // 否则全局第一个非bad空位
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player_t::EMPTY && !badGrid[x][y]) {
                    return {x, y};
                }
            }
        }
        return {-1, -1}; // 如果没有可落子位置，返回无效位置
    }

    // 优化评估函数：活四、冲四、活三、活二等棋型
    int evaluate(player_t player) {
        int score = 0;
        player_t opponent = switchPlayer(player);
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player) {
                    score += patternScore(x, y, player);
                } else if (board.board[x][y] == opponent) {
                    score -= patternScore(x, y, opponent) * 0.8; // 防守略弱于进攻
                }
            }
        }
        return score;
    }

    // 检查(x, y)为起点的各方向棋型分数
    int patternScore(uint64_t x, uint64_t y, player_t player) {
        int total = 0;
        for (const auto& d : dir) {
            int cnt = 1;
            int block = 0;
            // 正向
            for (int i = 1; i < 5; ++i) {
                uint64_t nx = x + d[0] * i;
                uint64_t ny = y + d[1] * i;
                if (nx >= BOARD_SIZE || ny >= BOARD_SIZE) { block++; break; }
                if (board.board[nx][ny] == player) cnt++;
                else if (board.board[nx][ny] == player_t::EMPTY) break;
                else { block++; break; }
            }
            // 反向
            for (int i = 1; i < 5; ++i) {
                uint64_t nx = x - d[0] * i;
                uint64_t ny = y - d[1] * i;
                if (nx >= BOARD_SIZE || ny >= BOARD_SIZE) { block++; break; }
                if (board.board[nx][ny] == player) cnt++;
                else if (board.board[nx][ny] == player_t::EMPTY) break;
                else { block++; break; }
            }
            // 评分
            if (cnt >= 5) total += 100000;
            else if (cnt == 4 && block == 0) total += 10000; // 活四
            else if (cnt == 4 && block == 1) total += 1000;  // 冲四
            else if (cnt == 3 && block == 0) total += 1000;  // 活三
            else if (cnt == 3 && block == 1) total += 100;   // 眠三
            else if (cnt == 2 && block == 0) total += 100;   // 活二
            else if (cnt == 2 && block == 1) total += 10;    // 眠二
        }
        return total;
    }

    // alpha-beta剪枝+zobrist置换表缓存
    int alphaBetaSearch(int depth, player_t player, int alpha, int beta) {
        uint64_t hash = board.getZobristHash();
        if (transpositionTable.count(hash)) return transpositionTable[hash];

        if (depth == 0) {
            int val = evaluate(player);
            transpositionTable[hash] = val;
            return val;
        }

        int best = (player == player_t::BLACK) ? std::numeric_limits<int>::min() : std::numeric_limits<int>::max();
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player_t::EMPTY) {
                    board.place(x, y, player);
                    int score = alphaBetaSearch(depth - 1, switchPlayer(player), alpha, beta);
                    board.undo(x, y, player);
                    if (player == player_t::BLACK) {
                        best = std::max(best, score);
                        alpha = std::max(alpha, score);
                    } else {
                        best = std::min(best, score);
                        beta = std::min(beta, score);
                    }
                    if (beta <= alpha) break;
                }
            }
        }
        transpositionTable[hash] = best;
        return best;
    }
};

#endif // AI_H