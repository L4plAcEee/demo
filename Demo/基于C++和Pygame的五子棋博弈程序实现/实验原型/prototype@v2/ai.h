#ifndef AI_H
#define AI_H
#include "board.h"


#include <unordered_map>
#include <algorithm>
#include <limits>

class AI {
public:
    AI(Board& board, int maxDepth = 4) : board(board), maxDepth(maxDepth) {
        neighberCounter = std::vector<std::vector<int>>(BOARD_SIZE, std::vector<int>(BOARD_SIZE, 0));
    }
    // AI主逻辑
    std::pair<int, int> makeMove(PlayerType player) {
        if (board.getCurrentRound() <= 3) {
            return startStrategy(player);
        }
        // 正常alpha-beta搜索
        int bestScore = std::numeric_limits<int>::min();
        std::pair<int, int> bestMove = {0, 0};
        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == PlayerType::EMPTY && neighberCounter[x][y] > 0) {
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

        return bestMove;
    }

    void updateNeighberCounterAfterMove(int x, int y) {
        int R = 2;
        int startX = std::max(0, x - R);
        int startY = std::max(0, y - R);
        int endX = std::min(x + R, BOARD_SIZE - 1);
        int endY = std::min(y + R, BOARD_SIZE - 1);
        
        for (int i = startX; i <= endX; ++i) {
            for (int j = startY; j <= endY; ++j) {
                neighberCounter[i][j]++;
            }
        }
    }

private:
    Board& board;
    int maxDepth;
    std::unordered_map<int, int> transpositionTable;

    std::vector<std::vector<int>> neighberCounter;

    PlayerType switchPlayer(PlayerType p) {
        return (p == PlayerType::BLACK) ? PlayerType::WHITE : PlayerType::BLACK;
    }

    std::pair<int, int> startStrategy(PlayerType player) {
        // 搜索所有黑子周围一格的空位
        std::vector<std::pair<int, int>> candidates;
        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == PlayerType::BLACK) {
                    for (int dx = -1; dx <= 1; ++dx) {
                        for (int dy = -1; dy <= 1; ++dy) {
                            if (dx == 0 && dy == 0) continue;
                            if ((dx < 0 && x == 0) || (dy < 0 && y == 0)) continue;
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx < BOARD_SIZE && ny < BOARD_SIZE && board.board[nx][ny] == PlayerType::EMPTY) {
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
        return {-1, -1}; // 如果没有可落子位置，返回无效位置
    }

    int evaluate(PlayerType player) {
        int score = 0;
        PlayerType opponent = switchPlayer(player);
        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == player) {
                    score += patternScore(x, y, player);
                } else if (board.board[x][y] == opponent) {
                    score -= patternScore(x, y, opponent) * 0.8; // 防守略弱于进攻
                }
            }
        }
        return score;
    }

    // 八方向滑动窗口统计所有长度为5的棋形
    int patternScore(int x, int y, PlayerType player) {
        int total = 0;
        int s4 = 0, b4 = 0, s3 = 0, b3 = 0, s2 = 0, b2 = 0;

        for (const auto &d : dir) {
            int startX = x - 4 * d[0] >= 0 ? x - 4 * d[0] : 0;
            int startY = y - 4 * d[1] >= 0 ? y - 4 * d[1] : 0;
            int endX = x + 4 * d[0] < BOARD_SIZE ? x + 4 * d[0] : BOARD_SIZE - 1;
            int endY = y + 4 * d[1] < BOARD_SIZE ? y + 4 * d[1] : BOARD_SIZE - 1;


            int leftX = startX, leftY = startY, rightX = startX, rightY = startY;
            int cnt = 0, block = 0, windowSize = 0;

            // 初始化第一个窗口
            for (int i = 0; i < 5 && rightX <= endX && rightY <= endY; ++i) {
                if (board.board[rightX][rightY] == player) cnt++;
                else if (board.board[rightX][rightY] != PlayerType::EMPTY) block++;
                rightX += d[0];
                rightY += d[1];
                windowSize++;
            }
            if (windowSize < 5) continue; // 如果窗口小于5，跳过

            updateScore(total, s4, b4, s3, b3, s2, b2, cnt, block);

            // 滑动窗口
            while (rightX <= endX && rightY <= endY) {

                if (board.board[rightX][rightY] == player) cnt++;
                else if (board.board[rightX][rightY] != PlayerType::EMPTY) block++;
                

                if (board.board[leftX][leftY] == player) cnt--;
                else if (board.board[leftX][leftY] != PlayerType::EMPTY) block--;
                
                updateScore(total, s4, b4, s3, b3, s2, b2, cnt, block);
                
                leftX += d[0]; leftY += d[1];
                rightX += d[0]; rightY += d[1];
            }
        }

        // 复合棋型评分
        if (s4 >= 2) total += S_4 * 5;     // 双活四
        if (s3 >= 2) total += S_3 * 3;     // 双活三，提高权重
        if (s4 >= 1 && s3 >= 1) total += S_4 * 2;  // 活四+活三，提高权重
        if (s4 >= 1 && b4 >= 1) total += S_4;      // 活四+冲四
        if (b4 >= 2) total += B_4;         // 双冲四，提高权重
        if (b3 >= 2) total += B_3;         // 双眠三
        return total;
    }



    void updateScore(int &total, int &s4, int &b4, int &s3, int &b3, int &s2, int &b2, int cnt, int block) {
        if (cnt >= 5) total += S_5;
        else if (cnt == 4) {
            if (block == 0) { total += S_4; s4++; }
            else if (block == 1) { total += B_4; b4++; }
        }
        else if (cnt == 3) {
            if (block == 0) { total += S_3; s3++; }
            else if (block == 1) { total += B_2; b3++; }
        }
        else if (cnt == 2) {
            if (block == 0) { total += S_2; s2++; }
            else if (block == 1) { total += B_2; b2++; }
        }
    }

    // alpha-beta剪枝+zobrist置换表缓存
    int alphaBetaSearch(int depth, PlayerType player, int alpha, int beta) {
        int hash = board.getZobristHash();
        if (transpositionTable.count(hash)) return transpositionTable[hash];

        if (depth == 0) {
            int val = evaluate(player);
            transpositionTable[hash] = val;
            return val;
        }
        std::vector<std::pair<int, int>> moves;
        int best = (player == PlayerType::BLACK) ? std::numeric_limits<int>::min() : std::numeric_limits<int>::max();
        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                if (board.board[x][y] == PlayerType::EMPTY && neighberCounter[x][y] > 0) {
                    moves.emplace_back(x, y);
                }
            }
        }

        if (!moves.empty()) {
            std::sort(moves.begin(), moves.end(), [&](const std::pair<int, int>& a, const std::pair<int, int>& b) {
                return neighberCounter[a.first][a.second] > neighberCounter[b.first][b.second];
            });
            
            for (const auto& [x, y] : moves) {  // 使用范围for循环，更安全且清晰
                if (board.board[x][y] == PlayerType::EMPTY) {
                    board.place(x, y, player);
                    int score = alphaBetaSearch(depth - 1, switchPlayer(player), alpha, beta);
                    board.undo(x, y, player);
                    if (player == PlayerType::BLACK) {
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