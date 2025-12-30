#pragma once
#include "Common.h"
#include "Monitor.h"

class Computer {
public:
    static Computer& getInstance() {
        static Computer instance;
        return instance;
    }

    Move genMove(std::vector<std::vector<int>> board, Move &currentMove) {
        mdas.reset();
        // 同步Zobrist哈希，因为board已更新但zobristHash尚未更新
        doMove(board, currentMove);
        std::vector<Move> possibleMoves = genLegalMoves(board, currentMove.role);
        if (!possibleMoves.empty()) {
           std::sort(possibleMoves.begin(), possibleMoves.end(), [&](const Move& a, const Move& b) {
                int cnt_a = boardStatus[a.x][a.y].HumanMoveNeighbor - boardStatus[a.x][a.y].AIMoveNeighbor;
                int cnt_b = boardStatus[b.x][b.y].HumanMoveNeighbor - boardStatus[b.x][b.y].AIMoveNeighbor;
                return cnt_a > cnt_b;
            }); 
        }
        long long maxScore = std::numeric_limits<long long>::min();
        Move ret;
        for (int i = 0; i < possibleMoves.size(); ++i) {
            doMove(board, possibleMoves[i]);
            long long score = -alphaBeta(board, mdas.updateMaxDepth(startSearchDepth, startSearchDepth), std::numeric_limits<long long>::min() , std::numeric_limits<long long>::max(), (Role)(currentMove.role ^ 1), possibleMoves);
            if (score > maxScore) {
                maxScore = mdas.updateScore(score);
                ret = possibleMoves[i];
            }
            undoMove(board, possibleMoves[i]);
        }
        // 同样的
        doMove(board, ret);
        mdas.logging();
        return ret;
    }

private:
    struct HashItem {
        Move move;
        long long score;
    };

    struct BoardStatus {
        unsigned long long zobristHash[2] = {0ULL};
        int HumanMoveNeighbor = 0;
        int AIMoveNeighbor = 0;
    };

    int startSearchDepth = 7; 
    bool isBlack = false; // 默认后手
    unsigned long long zobristHash = 0ULL;
    BoardStatus boardStatus[BOARD_SIZE][BOARD_SIZE];
    std::unordered_map<unsigned long long, HashItem> zobristHashTable;
    MonitorDataAboutSearch mdas;

    Computer() {
        std::mt19937_64 rng(std::random_device{}());
        std::uniform_int_distribution<uint64_t> dist;

        zobristHash = dist(rng);
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                boardStatus[i][j].zobristHash[0] = dist(rng);
                boardStatus[i][j].zobristHash[1] = dist(rng);
            }
        }
    };

    ~Computer() = default;

    Computer(const Computer&) = delete;

    Computer& operator=(const Computer&) = delete;

    long long alphaBeta(std::vector<std::vector<int>> &board, int depth, long long alpha, long long beta, Role role, std::vector<Move> &possibleMoves) {
        if (zobristHashTable.count(zobristHash)) return mdas.updateHashHitCnt(zobristHashTable[zobristHash].score);
        
        if (depth == 0) return zobristHashTable[zobristHash].score = evaluate(board, role);

        long long maxScore = std::numeric_limits<long long>::min();
        
        int cnt = 0;

        for (int i = 0; i < possibleMoves.size(); ++i) {
            if (board[possibleMoves[i].x][possibleMoves[i].y] != Role::NONE) continue;
            mdas.updateMaxWidth(++cnt);
            possibleMoves[i].role = role;
            doMove(board, possibleMoves[i]);
            possibleMoves[i].role = Role::NONE;
            long long score = alphaBeta(board, mdas.updateMaxDepth(startSearchDepth, depth - 1), -beta, -alpha, (Role)(role ^ 1), possibleMoves);
            possibleMoves[i].role = role;
            undoMove(board, possibleMoves[i]);
            
            if (score > maxScore) {
                maxScore = score;
            }

            alpha = std::max(alpha, score);

            if (alpha >= beta) {
                break; // beta 剪枝
            }
        }


        return zobristHashTable[zobristHash].score = maxScore;;
    }

    long long evaluate(std::vector<std::vector<int>> const &board, Role role) {
        long long scoreCurr = 0;
        long long scoreEnemy = 0;
        Move m;
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (board[i][j] > -1) {
                    m.x = i;
                    m.y = j;
                    m.role = (Role)board[i][j];
                    if (m.role == role) scoreCurr += evaluatePoint(board, m);
                    else scoreEnemy += evaluatePoint(board, m);
                }
            }
        }
        return scoreCurr - scoreEnemy;
    }

    // 滑动窗口法评估每个点的评分
    long long evaluatePoint(const std::vector<std::vector<int>> &board, const Move &m) {
        long long total = 0;
        int role = m.role;
        int opponent = 3 - role; // 因为 Role::BLACK = 1, Role::WHITE = 2

        for (const auto &d : dir) {
            int dx = d[0], dy = d[1];

            int startX = std::max(0, m.x - 4 * dx);
            int startY = std::max(0, m.y - 4 * dy);
            int endX = std::min(BOARD_SIZE - 1, m.x + 4 * dx);
            int endY = std::min(BOARD_SIZE - 1, m.y + 4 * dy);

            int leftX = startX, leftY = startY;
            int rightX = startX, rightY = startY;

            int cnt = 0, block = 0, windowSize = 0;

            // 初始化第一个窗口
            for (int i = 0; i < 5 && rightX >= 0 && rightX < BOARD_SIZE && rightY >= 0 && rightY < BOARD_SIZE; ++i) {
                if (board[rightX][rightY] == role) cnt++;
                else if (board[rightX][rightY] == opponent) block++;
                rightX += dx;
                rightY += dy;
                windowSize++;
            }
            if (windowSize < 5) continue;

            // 滑动窗口打分
            while (rightX >= 0 && rightX < BOARD_SIZE && rightY >= 0 && rightY < BOARD_SIZE) {
                if (cnt >= 5) total += S_5;
                else if (cnt == 4) {
                    if (block == 0) total += S_4;
                    else if (block == 1) total += B_4;
                } else if (cnt == 3) {
                    if (block == 0) total += S_3;
                    else if (block == 1) total += B_3;
                } else if (cnt == 2) {
                    if (block == 0) total += S_2;
                    else if (block == 1) total += B_2;
                }

                // 滑动窗口更新
                if (board[rightX][rightY] == role) cnt++;
                else if (board[rightX][rightY] == opponent) block++;

                if (board[leftX][leftY] == role) cnt--;
                else if (board[leftX][leftY] == opponent) block--;

                leftX += dx; leftY += dy;
                rightX += dx; rightY += dy;
            }

            // 最后一个窗口的评分（滑动结束后最后一次未评分）
            if (cnt >= 5) total += S_5;
            else if (cnt == 4) {
                if (block == 0) total += S_4;
                else if (block == 1) total += B_4;
            } else if (cnt == 3) {
                if (block == 0) total += S_3;
                else if (block == 1) total += B_3;
            } else if (cnt == 2) {
                if (block == 0) total += S_2;
                else if (block == 1) total += B_2;
            }
        }

        return total;
    }

    std::vector<Move> genLegalMoves(std::vector<std::vector<int>> &board, Role role) {
        std::vector<Move> possibleMoves;
        // 从每次扫描改为维护一个空格队列是好想法
        for (int x = 0; x < BOARD_SIZE; ++x) {
            for (int y = 0; y < BOARD_SIZE; ++y) {
                if (board[x][y] == -1 && boardStatus[x][y].HumanMoveNeighbor + boardStatus[x][y].AIMoveNeighbor > 0) {
                    possibleMoves.emplace_back(x, y, role);
                }
            }
        }
        return possibleMoves;
    }
    void doMove (std::vector<std::vector<int>> &board, const Move &m) {
        board[m.x][m.y] = m.role;
        zobristHash ^= boardStatus[m.x][m.y].zobristHash[m.role];
        updateBoardStatus(m);
    }

    void undoMove(std::vector<std::vector<int>> &board, const Move &m) {
        board[m.x][m.y] = -1;
        zobristHash ^= boardStatus[m.x][m.y].zobristHash[m.role];
        updateBoardStatus(m, true);
    }

    void updateBoardStatus(const Move& m, bool isUndo = false) {
        int R = 1;
        int startX = std::max(0, m.x - R);
        int startY = std::max(0, m.y - R);
        int endX = std::min(BOARD_SIZE - 1, m.x + R);
        int endY = std::min(BOARD_SIZE - 1, m.y + R);
        for (int i = startX; i <= endX; ++i) {
            for (int j = startY; j <= endY; ++j) {
                if (m.role == Role::HUMAN) isUndo ? boardStatus[i][j].HumanMoveNeighbor-- : boardStatus[i][j].HumanMoveNeighbor++;
                else isUndo ? boardStatus[i][j].AIMoveNeighbor-- : boardStatus[i][j].AIMoveNeighbor++;
            }
        }
    }
};