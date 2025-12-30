#pragma once
#include "Common.h"

class Computer {
public:
    static Computer& getInstance() {
        static Computer instance;
        return instance;
    }

    Move genMove(Move &currentMove, Role currentRole) {

        preprocess(currentMove, currentRole);

        Role role = (Role)(currentRole ^ 1); 

        std::vector<Move> possibleMoves = genPossibleMoves(role);

        long long maxScore = std::numeric_limits<long long>::min();
        Move ret;
        for (int i = 0; i < possibleMoves.size(); ++i) {
            doMove(possibleMoves[i], role);
            long long score = -alphaBeta(mdas.updateMaxDepth(startSearchDepth, startSearchDepth), std::numeric_limits<long long>::min() , std::numeric_limits<long long>::max(), (Role)(role ^ 1));
            if (score > maxScore) {
                maxScore = mdas.updateScore(score);
                ret = possibleMoves[i];
            }
            undoMove(possibleMoves[i], role);
        }

        
        postprocess(ret, role);
        return ret;
    }

    void printBoardStatus() {
        LOG(DEBUG, "=====[正在打印当前AI棋盘状态记录]=====");
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                std::cout << boardStatus[i][j].score << " ";
            }
            std::cout << "\n"; 
        }
    }

private:
    struct MonitorDataAboutSearch {
        using Clock = std::chrono::high_resolution_clock;
        using TimePoint = Clock::time_point;

        long long score {std::numeric_limits<long long>::min()};
        long long zobristHashHitCnt {};
        int alphaCutCnt {};
        int betaCutCnt {};
        int maxMovesPoolSize {};
        int maxDepth {};
        int minWidth {std::numeric_limits<int>::max()};
        int maxWidth {};

        TimePoint startTime {};
        double thinkingTimeInMs {};

        int updateMovesPoolSize(int size) {
            maxMovesPoolSize = std::max(maxMovesPoolSize, size);
            return size;
        }

        long long updateScore(long long s) {
            score = std::max(score, s);
            return s;
        }

        long long updateHashHitCnt(long long zobristHashHitResult) {
            zobristHashHitCnt++;
            return zobristHashHitResult;
        }

        int updateMaxDepth(int startD, int d) {
            maxDepth = std::max(maxDepth, startD - d);
            return d;
        }

        int updateWidth(int w) {
            maxWidth = std::max(maxWidth, w);
            minWidth = std::min(minWidth, w);
            return w;
        }

        void startTimer() {
            startTime = Clock::now();
        }

        void endTimer() {
            auto endTime = Clock::now();
            thinkingTimeInMs = std::chrono::duration<double, std::milli>(endTime - startTime).count();
        }

        void reset() {
            score = std::numeric_limits<long long>::min();
            zobristHashHitCnt = 0;
            maxDepth = 0;
            minWidth = std::numeric_limits<int>::max();
            maxWidth = 0;
            alphaCutCnt = 0;
            betaCutCnt = 0;
            thinkingTimeInMs = 0;
            startTimer();
        }

        void logging(int currentTurn) {
            endTimer();
            LOG(INFO, "=====[当前AI思考数据]=====");
            std::cout 
                << "当前轮次: " << currentTurn << '\n'
                << "评分: " << score << '\n'
                << "ZobristHash命中次数: " << zobristHashHitCnt << '\n'
                // << "当前最大候选集Size: " << maxMovesPoolSize << '\n'
                << "发生alpha截断次数: " << alphaCutCnt << '\n'
                << "发生beta截断次数: " << betaCutCnt << '\n'
                << "最大搜索深度: " << maxDepth << '\n'
                << "最小搜索宽度: " << minWidth << '\n'
                << "最大搜索宽度: " << maxWidth << '\n'
                << "思考耗时: " << thinkingTimeInMs << " ms\n";
            LOG(INFO, "=====[当前AI思考数据]=====");
        }
    };
    struct HashItem {
        Move move;
        long long score;
    };

    struct BoardStatus {
        Role role;
        long long score;
        unsigned long long zobristHash[2] = {0ULL};
        int HumanMoveNeighbor = 0;
        int AIMoveNeighbor = 0;
    };

    const int MAX_CANDIDATES = 50;
    const int RADIUS_OF_UPDATE = 3;
    const int RADIUS_OF_UPDATE_NUM = 1e2;
    int turn = 0;
    int startSearchDepth = 5; 
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
                boardStatus[i][j].role = Role::NONE;
                boardStatus[i][j].zobristHash[0] = dist(rng);
                boardStatus[i][j].zobristHash[1] = dist(rng);
            }
        }
    };

    ~Computer() = default;

    Computer(const Computer&) = delete;

    Computer& operator=(const Computer&) = delete;

    long long alphaBeta(int depth, long long alpha, long long beta, Role role) {
        if (zobristHashTable.count(zobristHash)) return mdas.updateHashHitCnt(zobristHashTable[zobristHash].score);
        
        if (depth == 0) return zobristHashTable[zobristHash].score = evaluate(role);

        long long maxScore = std::numeric_limits<long long>::min();
        
        int cnt = 0;

        std::vector<Move> possibleMoves = genPossibleMoves(role);

        for (int i = 0; i < possibleMoves.size(); ++i) {
            // LOG(Debug, possibleMoves[i].x << ", " << possibleMoves[i].y);
            if (boardStatus[possibleMoves[i].x][possibleMoves[i].y].role != Role::NONE) continue;
            mdas.updateWidth(++cnt);

            doMove(possibleMoves[i], role);

            long long score = alphaBeta(mdas.updateMaxDepth(startSearchDepth, depth - 1), -beta, -alpha, (Role)(role ^ 1));

            undoMove(possibleMoves[i], role);
            
            if (score > maxScore) {
                maxScore = score;
            }

            alpha = std::max(alpha, score);

            if (alpha >= beta) {
                if (role == Role::HUMAN)
                    mdas.betaCutCnt++;  // 当前是 Max 节点，发生的是 Beta 剪枝
                else
                    mdas.alphaCutCnt++; // 当前是 Min 节点，发生的是 Alpha 剪枝
                return zobristHashTable[zobristHash].score = maxScore;
            }
        }


        return zobristHashTable[zobristHash].score = maxScore;;
    }

    long long evaluate(Role role) {
        long long scoreCurr = 0;
        long long scoreEnemy = 0;
        Move m;
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (boardStatus[i][j].role > -1) {
                    m = Move(i, j);
                    if (boardStatus[i][j].role == role) scoreCurr += evaluatePoint(m, boardStatus[i][j].role);
                    else scoreEnemy += evaluatePoint(m, boardStatus[i][j].role);
                }
            }
        }
        return scoreCurr - scoreEnemy;
    }

    // 滑动窗口法评估每个点的评分
    long long evaluatePoint(const Move &m, Role role) {
        long long total = 0;
        Role opponent = (Role)(role ^ 1);

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
                if (boardStatus[rightX][rightY].role == role) cnt++;
                else if (boardStatus[rightX][rightY].role == opponent) block++;
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
                if (boardStatus[rightX][rightY].role == role) cnt++;
                else if (boardStatus[rightX][rightY].role == opponent) block++;

                if (boardStatus[leftX][leftY].role == role) cnt--;
                else if (boardStatus[leftX][leftY].role == opponent) block--;

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

    std::vector<Move> genPossibleMoves(Role role) {
        std::vector<Move> possibleMoves;

        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (boardStatus[i][j].role == Role::NONE && boardStatus[i][j].HumanMoveNeighbor + boardStatus[i][j].AIMoveNeighbor != 0) {
                    possibleMoves.emplace_back(i, j);
                    boardStatus[i][j].score = boardStatus[i][j].HumanMoveNeighbor + boardStatus[i][j].AIMoveNeighbor;
                }
            }
        }

        if (possibleMoves.empty()) {
            LOG(WARN, "候选集为空，已经无子可落");
        }
        
        std::sort(possibleMoves.begin(), possibleMoves.end(), [&](const Move& a, const Move& b) {
            return boardStatus[a.x][a.y].score > boardStatus[b.x][b.y].score;
        }); 


        size_t realSize = std::min(possibleMoves.size(), static_cast<size_t>(MAX_CANDIDATES));
        return std::vector<Move>(possibleMoves.begin(), possibleMoves.begin() + realSize);
    }

    void doMove (const Move &m, Role r) {
        turn++;
        boardStatus[m.x][m.y].role = r;
        zobristHash ^= boardStatus[m.x][m.y].zobristHash[r];
        // movesPool.erase(m);
        updateBoardStatus(m, r);
    }

    void undoMove(const Move &m, Role r) {
        turn--;
        boardStatus[m.x][m.y].role = Role::NONE;
        zobristHash ^= boardStatus[m.x][m.y].zobristHash[r];
        // movesPool.insert(m);
        updateBoardStatus(m, r, true);
    }

    void preprocess(Move &currentMove, Role currentRole) {
        if (turn <= 10) startSearchDepth = 3;
        else if (turn <= 20) startSearchDepth++;
        else if (turn <= 30) startSearchDepth++;
        else if (turn <= 40) startSearchDepth++;
        doMove(currentMove, currentRole);
        mdas.reset();
    }

    void postprocess(Move &currentMove, Role currentRole) {
        mdas.logging(turn);
        doMove(currentMove, currentRole);
    }

    void updateBoardStatus(const Move& m, Role role, bool isUndo = false) {
        int R = RADIUS_OF_UPDATE;
        int baseWeight = RADIUS_OF_UPDATE_NUM;

        for (int dx = -R; dx <= R; ++dx) {
            for (int dy = -R; dy <= R; ++dy) {
                int nx = m.x + dx;
                int ny = m.y + dy;

                if (nx < 0 || nx >= BOARD_SIZE || ny < 0 || ny >= BOARD_SIZE) continue;
                if (dx == 0 && dy == 0) continue;

                int dist = std::abs(dx) + std::abs(dy);
                if (dist > R) continue;

                int weight = baseWeight;
                for (int i = 1; i < dist; ++i) weight /= 10;

                if (role == Role::HUMAN)
                    isUndo ? boardStatus[nx][ny].HumanMoveNeighbor -= weight
                        : boardStatus[nx][ny].HumanMoveNeighbor += weight;
                else
                    isUndo ? boardStatus[nx][ny].AIMoveNeighbor -= weight
                        : boardStatus[nx][ny].AIMoveNeighbor += weight;

                // if (boardStatus[nx][ny].role == Role::NONE &&
                //     (boardStatus[nx][ny].HumanMoveNeighbor > 0 || boardStatus[nx][ny].AIMoveNeighbor > 0)) {
                //     movesPool.emplace(nx, ny);
                // }
            }
        }
    }

};