#pragma once
#include <string>
#include <utility>
#include <chrono>
#include <random>
#include <iostream>
#include <iomanip>
#include <algorithm>
#include <unordered_map>
#include <sstream>


using namespace std;


#define LOG(Level, S) std::cout << "[调试信息] : [" << #Level << "] : " << S << "  " << std::endl

#define BOARD_SIZE (int)(15)

enum PatternType {
    FIVE = 0,
    FOUR,
    BLOCKED_FOUR,
    THREE,
    BLOCKED_THREE,
    TWO,
    BLOCKED_TWO,
    PATTERN_TYPE_COUNT // 自动获取枚举数量
};

const int patternScore[PATTERN_TYPE_COUNT] = {
    100000,   // FIVE（必胜）
    10000,    // FOUR（活四）
    5000,     // BLOCKED_FOUR（冲四）
    1000,     // THREE（活三）
    500,      // BLOCKED_THREE（眠三）
    200,      // TWO（活二）
    100       // BLOCKED_TWO（眠二）
};


enum Role {
    NONE = -1,
    HUMAN,
    COMPUTER
};

// 四个方向：横、竖、主对角线、副对角线
const int dx[4] = {1, 0, 1, 1};
const int dy[4] = {0, 1, 1, -1};

bool isLegalPoint (int x, int y) {
    return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
}

class GameEngine {

public:
    static GameEngine& getInstance() {
        static GameEngine instance;
        return instance;
    }

    pair<int, int>  humanMove(pair<int, int> m) {
        doMove(m, Role::HUMAN);
        return m;
    }
    
    pair<int, int> computerStartMove() {
        doMove({BOARD_SIZE/2, BOARD_SIZE/2}, Role::COMPUTER);
        return {BOARD_SIZE/2, BOARD_SIZE/2};
    }

    pair<int, int> computerMove(pair<int, int>) {
        using namespace std::chrono;
        
        md = MoveData();
        int CurrentMaxDepth = decideSearchDepth();

        vector<pair<int, int>> moves = genMoves(Role::COMPUTER);

        auto startTime = steady_clock::now();
        for (int depth = 1, alpha = -INF, beta = INF; depth <= CurrentMaxDepth; ++depth) {
            md.maxDepth = maxDepth = depth;
            md.bestScore = INT_MIN;

            for (auto &mv : moves) {
                auto now = steady_clock::now();
                auto elapsed = duration_cast<milliseconds>(now - startTime).count();
                if (elapsed >= MAX_TIME_MS) {
                    goto TIMEOUT_EXIT;
                }

                doMove(mv, Role::COMPUTER);
                int score = -negaMax(0, alpha, beta, Role::HUMAN);
                undoMove(mv, Role::COMPUTER);

                if (score > md.bestScore) {
                    md.bestScore = score;
                    md.bestMove = mv;
                }
            }
        }

    TIMEOUT_EXIT:

        doMove(md.bestMove, Role::COMPUTER);
        clearHashTable();
        md.summarize();
        
        return md.bestMove;
    }


private:
    struct PointStatus {
        unsigned long long zobristHash[2] = { 0 };
        Role role;
        int neighborCnt {};
        int score {};
    };

    struct HashItem {
        enum class Flag { F_NONE, F_EXACT, F_ALPHA, F_BETA };

        int depth = 0;
        int score = INT_MIN;
        std::pair<int, int> bestMove = {-1, -1};
        Flag flagType = Flag::F_NONE;

        HashItem() = default;
        HashItem(int d, int s) : depth(d), score(s) {}
        HashItem(int d, int s, Flag f) : depth(d), score(s), flagType(f) {}
    };
    struct MoveData {
        chrono::steady_clock::time_point startTime{chrono::steady_clock::now()};
        
        double evaluateMoveMaxTimeInMs = 0.0;
        double recognizePatternMaxTimeInMs = 0.0;
        double thinkingTimeInMs = 0.0;
        pair<int, int> bestMove = {-1, -1};
        int bestScore = INT_MIN;
        int alphaCutCnt = 0;
        int betaCutCnt = 0;
        int hashHitCnt = 0;
        int searchCnt = 0;
        int maxDepth = 0;

        void summarize() {
            thinkingTimeInMs = chrono::duration<double, milli>(
                chrono::steady_clock::now() - startTime).count();

            double hitRate = (searchCnt > 0) ? (100.0 * hashHitCnt / searchCnt) : 0.0;

            cout 
                << "===[统计信息]===\n"
                << "思考时间(ms): " << thinkingTimeInMs << "\n"
                << "选择着法: [" << bestMove.first << ", " << bestMove.second << "]\n"
                << "着法评分: " << bestScore << '\n' 
                // << "evaluateMove函数最大耗时: " << evaluateMoveMaxTimeInMs << "\n"
                // << "recognizePatter函数最大耗时: " << recognizePatternMaxTimeInMs << '\n'
                << "总搜索次数: " << searchCnt << "\n"
                << "最大搜索深度: " << maxDepth << '\n'
                << "缓存命中数: " << hashHitCnt << "\n"
                << "缓存命中率: " << fixed << setprecision(2) << hitRate << " %\n"
                // << "发生 Alpha 截断数: " << alphaCutCnt << "\n"
                << "发生 Beta 截断数: " << betaCutCnt << "\n";
        }
    };


    unsigned long long zobristHash = 0ULL;
    const int UNK = 1e9 + 1;
    const int INF = 1e9;
    const int MAX_DEPTH = 20;
    const int MAX_TIME_MS = 1e5;
    pair<int, int> currentMove;
    int turn = 0;
    int maxDepth = 1;
    unordered_map<unsigned long long, HashItem> hashTable;
    PointStatus board[BOARD_SIZE][BOARD_SIZE];
    MoveData md;
    

    int decideSearchDepth() {
        int stones = turn;

        if (stones <= 4) return 2;              // 开局，快速启发判断
        else if (stones <= 8) return 4;         // 初期
        else if (stones <= 14) return 6;        // 中期
        else if (stones <= 22) return 8;        // 中后期
        else return MAX_DEPTH;              // 终局力争精准终结
    }

    int negaMax(int depth, int alpha, int beta, Role role) {
        md.searchCnt++;
        int val = probeHashHIt(depth, alpha, beta);
        if (val != UNK) return val;

        if (depth == maxDepth) return (hashTable[zobristHash] = HashItem(depth, evaluate(role), HashItem::Flag::F_EXACT)).score;

        for (auto &move : genMoves(role)) {
            doMove(move, role);
            alpha = max(alpha, -negaMax(depth + 1, -beta, -alpha, (Role)(role ^ 1)));
            undoMove(move, role);
        
            if (alpha >= beta) {
                md.betaCutCnt++;
                return (hashTable[zobristHash] = HashItem(depth, beta, HashItem::Flag::F_BETA)).score;
            }
        }
        return (hashTable[zobristHash] = HashItem(depth, alpha, HashItem::Flag::F_ALPHA)).score;
    }

    int probeHashHIt(int depth, int alpha, int beta) {
        auto it = hashTable.find(zobristHash);
        if (it != hashTable.end()) {
            md.hashHitCnt++;
            HashItem hi = it->second;
            if ((hi.flagType != HashItem::Flag::F_NONE) && (hi.depth <= depth)) {
                if (hi.flagType == HashItem::Flag::F_EXACT) return hi.score;
                if ((hi.flagType == HashItem::Flag::F_ALPHA) && (hi.score <= alpha)) return alpha;
                if ((hi.flagType == HashItem::Flag::F_BETA) && (hi.score >= beta)) return beta;
            }
        }
        return UNK;
    }

    int evaluate(Role role) {
        long long scoreCurr = 0;
        long long scoreEnemy = 0;
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (board[i][j].role > -1) {
                    if (board[i][j].role == role) scoreCurr += evaluateMove({i, j}, board[i][j].role);
                    else scoreEnemy += evaluateMove({i, j}, board[i][j].role);
                }
            }
        }


        return scoreCurr - scoreEnemy;
    }

    int evaluateMove(pair<int, int> m, Role role) {
        // chrono::steady_clock::time_point evaluateMoveStartTime = chrono::steady_clock::now();

        int totalScore = 0;
        int x = m.first;
        int y = m.second;

        for (int dir = 0; dir < 4; ++dir) {
            totalScore += recognizePattern(getLinePattern(x, y, dx[dir], dy[dir], role));
        }

        // md.evaluateMoveMaxTimeInMs = max(md.evaluateMoveMaxTimeInMs, chrono::duration<double, milli>(chrono::steady_clock::now() - evaluateMoveStartTime).count());
        return totalScore;
    }


    int evaluatePoint(pair<int, int> p) {
        return board[p.first][p.second].neighborCnt;
    }

    int evaluatePoint(pair<int, int> p, Role r) {
        return evaluateMove(p, r);
    }

    vector<pair<int, int>> genMoves(Role r) {
        vector<pair<int ,int>> moves;
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (board[i][j].role == Role::NONE && board[i][j].neighborCnt > 0) {
                    moves.emplace_back(i, j);
                    board[i][j].score = evaluatePoint({i, j}, r);
                }
            }
        }

        sort(moves.begin(), moves.end(), [&](const pair<int, int>& a, const pair<int, int>& b) {
            return board[a.first][a.second].score > board[b.first][b.second].score;
        });

        return moves;
    }

    void doMove(pair<int, int> m, Role r) {
        int R = 3;
        turn++;
        zobristHash ^= board[m.first][m.second].zobristHash[(int)r];
        board[m.first][m.second].role = r;
        for (int i = m.first - R; i <= m.first + R; ++i) {
            for (int j = m.second - R; j <= m.second + R; ++j) {
                if (isLegalPoint(i, j)) {
                    board[i][j].neighborCnt++;
                }
            }
        }
    }

    void undoMove(pair<int, int> m, Role r) {
        int R = 3;
        turn--;
        zobristHash ^= board[m.first][m.second].zobristHash[(int)r];
        board[m.first][m.second].role = Role::NONE;
        for (int i = m.first - R; i <= m.first + R; ++i) {
            for (int j = m.second - R; j <= m.second + R; ++j) {
                if (isLegalPoint(i, j)) {
                    board[i][j].neighborCnt--;
                }
            }
        }
    }
    
    string getLinePattern(int x, int y, int dx, int dy, Role role) {
        string pattern;
        for (int i = -4; i <= 4; ++i) {
            int nx = x + i * dx;
            int ny = y + i * dy;

            if (!isLegalPoint(nx, ny)) {
                pattern += 'B'; // 边界视为阻挡
            } else if (nx == x && ny == y) {
                pattern += '1'; // 假设当前点落子
            } else {
                Role r = board[nx][ny].role;
                if (r == Role::NONE) pattern += '0';
                else if (r == role) pattern += '1';
                else pattern += '2';
            }
        }
        return pattern;
    }

    int recognizePattern(const string& s) {
        // chrono::steady_clock::time_point recognizePatternStartTime = chrono::steady_clock::now();
        int score = 0;
        int count[PATTERN_TYPE_COUNT] = {0};

        // 检测各类模式出现次数
        if (s.find("11111") != string::npos) count[FIVE]++;
        
        // 活四
        for (const string& pat : {"011110"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[FOUR]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 冲四
        for (const string& pat : {"011112", "211110", "10111", "11011", "11101"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[BLOCKED_FOUR]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 活三
        for (const string& pat : {"01110", "010110", "011010"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[THREE]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 眠三
        for (const string& pat : {"001112", "211100", "021110", "011012"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[BLOCKED_THREE]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 活二
        for (const string& pat : {"00110", "01010", "01100", "00110"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[TWO]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 眠二
        for (const string& pat : {"000112", "211000", "021100", "001102"}) {
            size_t pos = s.find(pat);
            while (pos != string::npos) {
                count[BLOCKED_TWO]++;
                pos = s.find(pat, pos + 1);
            }
        }

        // 组合所有得分
        for (int i = 0; i < PATTERN_TYPE_COUNT; ++i) {
            score += count[i] * patternScore[i];
        }
        // md.recognizePatternMaxTimeInMs = max(md.recognizePatternMaxTimeInMs, chrono::duration<double, milli>(chrono::steady_clock::now() - recognizePatternStartTime).count());
        return score;
    }

    void clearHashTable() {
        LOG(INFO, "已经清理 [" << hashTable.size() <<"] 个缓存记录 ");
        hashTable.clear();
    }


    GameEngine() {
        mt19937_64 rng(random_device{}());
        uniform_int_distribution<uint64_t> dist;

        zobristHash = dist(rng);
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                board[i][j].role = Role::NONE;
                board[i][j].zobristHash[0] = dist(rng);
                board[i][j].zobristHash[1] = dist(rng);
            }
        }
    };

    ~GameEngine() = default;

    GameEngine(const GameEngine&) = delete;

    GameEngine& operator=(const GameEngine&) = delete;

};

class Board {
private:
    Role board[BOARD_SIZE][BOARD_SIZE];
    Role currentPlayer;
    Role firstPlayer;            // ✅ 新增：记录本局的先手
    int turnCount;
    bool gameOver;
    Role winner;
    GameEngine& engine;

public:
    // 构造函数
    Board() : engine(GameEngine::getInstance()) {
        initializeBoard(Role::HUMAN);  // 默认人类先手
    }


    // 游戏控制函数
    // ✅ 支持选择先手玩家
    void startGame(Role firstPlayer) {
        this->firstPlayer = firstPlayer;
        initializeBoard(firstPlayer);
    }

    // ✅ 重置时也要恢复先手
    void resetGame() {
        initializeBoard(firstPlayer);
    }

    bool isGameOver() const {
        return gameOver;
    }

    Role getWinner() const {
        return winner;
    }

    bool makeMove(int x, int y, Role role) {
        if (!isValidMove(x, y) || gameOver) {
            return false;
        }
        
        board[x][y] = role;
        turnCount++;
        
        // 通知游戏引擎
        if (role == Role::HUMAN) {
            engine.humanMove({x, y});
        }
        
        // 检查是否获胜
        if (checkWin(x, y, role)) {
            gameOver = true;
            winner = role;
            return true;
        }
        
        // 检查是否平局
        if (turnCount >= BOARD_SIZE * BOARD_SIZE) {
            gameOver = true;
            winner = Role::NONE;
            return true;
        }
        
        switchPlayer();
        return true;
    }

    bool isValidMove(int x, int y) const {
        return isLegalPoint(x, y) && board[x][y] == Role::NONE;
    }

    // 显示函数
    void display() const {
        std::cout << "\n";
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                std::cout << getRoleSymbol(board[i][j]) << " ";
            }
            std::cout << "\n";
        }
    }

    void displayWithCoordinates() const {
        std::cout << "\n   ";
        for (int j = 0; j < BOARD_SIZE; ++j) {
            std::cout << std::setw(2) << j << " ";
        }
        std::cout << "\n";
        
        for (int i = 0; i < BOARD_SIZE; ++i) {
            std::cout << std::setw(2) << i << " ";
            for (int j = 0; j < BOARD_SIZE; ++j) {
                std::cout << getRoleSymbol(board[i][j]) << "  ";
            }
            std::cout << "\n";
        }
    }

    // 获取信息函数
    Role getRole(int x, int y) const {
        if (isLegalPoint(x, y)) {
            return board[x][y];
        }
        return Role::NONE;
    }

    int getTurnCount() const {
        return turnCount;
    }

    Role getCurrentPlayer() const {
        return currentPlayer;
    }

private:
    // ✅ 初始化棋盘时传入先手方
    void initializeBoard(Role startingPlayer) {
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                board[i][j] = Role::NONE;
            }
        }
        currentPlayer = startingPlayer;
        turnCount = 0;
        gameOver = false;
        winner = Role::NONE;
    }

    bool checkWin(int x, int y, Role role) {
        // 检查四个方向
        for (int dir = 0; dir < 4; ++dir) {
            if (checkDirection(x, y, dx[dir], dy[dir], role)) {
                return true;
            }
        }
        return false;
    }

    bool checkDirection(int x, int y, int dx, int dy, Role role) {
        int count = 1; // 包括当前位置
        
        // 向前检查
        int nx = x + dx, ny = y + dy;
        while (isLegalPoint(nx, ny) && board[nx][ny] == role) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        // 向后检查
        nx = x - dx;
        ny = y - dy;
        while (isLegalPoint(nx, ny) && board[nx][ny] == role) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        return count >= 5;
    }

    void switchPlayer() {
        currentPlayer = (currentPlayer == Role::HUMAN) ? Role::COMPUTER : Role::HUMAN;
    }

    char getRoleSymbol(Role role) const {
        switch (role) {
            case Role::HUMAN:
                return 'O';
            case Role::COMPUTER:
                return 'X';
            case Role::NONE:
            default:
                return '.';
        }
    }

    bool isLegalPoint(int x, int y) const {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }
};
