#pragma once
#include <string>
#include <vector>
#include <stack>
#include <random>
#include <set>

namespace GoBangLiteEngine {

constexpr int BOARD_SIZE = 15;
constexpr int UNKNOWN_SCORE = (int)0xDeadBeef;
constexpr int HASH_ITEM_INDEX_MASK = 0xffff;
constexpr int MAX_SCORE = 1145141919;
constexpr int MIN_SCORE = -1145141919;
constexpr int START_SEARCH_DEPTH = 7;
constexpr std::pair<int, int> directions[8] = {
    {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
    {1, 0},  {1, -1}, {0, -1}, {-1, -1}
};

// C++20支持开始聚合初始化 + 默认成员值
struct Move {
    int x, y, score;
    Move() = default;
    Move(int cx, int cy) : x(cx), y(cy), score(0) {}
    Move(int cx, int cy, int cs) : x(cx), y(cy), score(cs) {}
};

struct Pattern {
    std::string pattern;
    int score;
};

struct HashItem {
    long long checksum;
    int depth;
    int score;
    enum Flag { ALPHA = 0, BETA = 1, EXACT = 2, EMPTY = 3 } flag;
};

enum class Role {
    EMPTY = -1,
    HUMAN = 0,
    COMPUTER = 1
};

std::vector<Pattern> patterns = {
    { "11111",  50000 },
    { "011110", 4320 },
    { "011100", 720 },
    { "001110", 720 },
    { "011010", 720 },
    { "010110", 720 },
    { "11110",  720 },
    { "01111",  720 },
    { "11011",  720 },
    { "10111",  720 },
    { "11101",  720 },
    { "001100", 120 },
    { "001010", 120 },
    { "010100", 120 },
    { "000100", 20 },
    { "001000", 20 },
};


class GameMonitor {
public:
    int board[BOARD_SIZE][BOARD_SIZE];
    int scoreTable[BOARD_SIZE][BOARD_SIZE];
    int humanScore, computerScore;
    int winner;

    static GameMonitor& getInstance() {
        static GameMonitor gm;
        return gm;
    }

private:
    GameMonitor() {
        // TODO
    }
    GameMonitor(const GameMonitor&) = delete;
    GameMonitor& operator=(const GameMonitor&) = delete;
};

class ZobristHashMonitor {
public:
    HashItem hashTable[HASH_ITEM_INDEX_MASK + 1];
    unsigned long long zobristValBoard[BOARD_SIZE][BOARD_SIZE][2]; 
    unsigned long long zobristVal;

    static ZobristHashMonitor& getInstance() {
        static ZobristHashMonitor zhm;
        return zhm;
    }

    void recordCurrentStatus(int depth, int score, HashItem::Flag flag) {
        int idx = (int)(zobristVal & HASH_ITEM_INDEX_MASK);
        HashItem *it = &hashTable[idx];
        
        // 只保存最短步数结果
        if (it->flag != HashItem::EMPTY && it->depth >= depth) {
            return;
        }

        it->checksum = zobristVal;
        it->score = score;
        it->flag = flag;
        it->depth = depth;
    }

    int getHashItemScore(int depth, int alpha, int beta) {
        int idx = (int)(zobristVal & HASH_ITEM_INDEX_MASK);
        HashItem *it = &hashTable[idx];
        if (it->flag == HashItem::EMPTY) return UNKNOWN_SCORE;
        // 如果当前的 Zobrist 值与哈希表中的值匹配，并且深度足够，才进行下一步逻辑。
        if (it->checksum == zobristVal) {
            if (it->depth >= depth) {
                if (it->flag == HashItem::EXACT) return it->score;
                if (it->flag == HashItem::ALPHA && it->score <= alpha) return alpha;
                if (it->flag == HashItem::BETA && it->score >= beta) return beta;
            }
        }
        return UNKNOWN_SCORE;
    }

private:
    ZobristHashMonitor() {
        zobristVal = __utils::getRandUINT64();
        for (int i = 0; i < BOARD_SIZE; ++i) {
            for (int j = 0; j < BOARD_SIZE; ++j) {
                for (int k = 0; k < 2; ++k) {
                    zobristValBoard[i][j][k] = __utils::getRandUINT64();
                }
            }
        }
    };
    ZobristHashMonitor(const ZobristHashMonitor&) = delete;
    ZobristHashMonitor& operator=(const ZobristHashMonitor&) = delete;
};

class HistoryMonitor {
public:
    struct MoveHistoryItem {
        std::set<Move> addedMoves;
        Move removedMove;
    };

    std::vector<MoveHistoryItem> moveHistory;
    std::set<Move> possibleMoves;

    static HistoryMonitor& getInstance() {
        static HistoryMonitor instance;
        return instance;
    }

    void updatePossibleMoves(Move const& addMove) {

        std::set<Move> added;

        for (int d = 0; d < 8; ++d) {
            int nx = addMove.x + directions[d].first, ny = addMove.y + directions[d].second;
            if (!__utils::isLegalMove(nx, ny)) continue;
            if (gm.board[nx][ny] == (int)Role::EMPTY) {
                Move mv(nx, ny);
                if (possibleMoves.insert(mv).second) {
                    added.insert(mv);
                }
            }
        }

        MoveHistoryItem hi;
        hi.addedMoves = added;

        if (possibleMoves.find(addMove) != possibleMoves.end()) {
            possibleMoves.erase(addMove);
            hi.removedMove = addMove;
        }
        else {
            hi.removedMove.x = 0xDeadBeef;
            hi.removedMove.y = 0xDeadBeef;
        }

        moveHistory.push_back(hi);
    }

    void rollback() {
        if (possibleMoves.empty())
            return;

        MoveHistoryItem hi = moveHistory[moveHistory.size() - 1];
        moveHistory.pop_back();

        std::set<Move>::iterator iter;

        //清除掉前一步加入的着法
        for (iter = hi.addedMoves.begin(); iter != hi.addedMoves.end(); iter++) 
            possibleMoves.erase(*iter);

        //加入前一步删除的着法
        if(hi.removedMove.x != 0xDeadBeaf)
            possibleMoves.insert(hi.removedMove);
    }

private:
    HistoryMonitor() {
        // TODO
    };
    HistoryMonitor(const HistoryMonitor&) = delete;
    HistoryMonitor& operator=(const HistoryMonitor&) = delete;
};


int DEPTH = START_SEARCH_DEPTH;


std::stack<Move> moves;


GameMonitor gm;
ZobristHashMonitor zhm;
HistoryMonitor hm;


namespace __core {
inline int alphaBeta(int board[BOARD_SIZE][BOARD_SIZE], int depth, int alpha, int beta, Role role) {
    HashItem::Flag flag = HashItem::ALPHA;
    int score = zhm.getHashItemScore(depth, alpha, beta);
    if (score != UNKNOWN_SCORE && depth != DEPTH) return score;

    int scoreCurr = evaluate();
    int scoreEnemy = evaluate();

    if (scoreCurr >= (int)5e4) return MAX_SCORE - 1337 - (DEPTH - depth);
    if (scoreEnemy >= (int)5e4) return MIN_SCORE + 1337 + (DEPTH - depth);
    if (depth == 0) {
        zhm.recordCurrentStatus(depth, scoreCurr - scoreEnemy, HashItem::EXACT);
        return scoreCurr - scoreEnemy;
    }

    int count = 0;
    std::set<Move> possibleMoves;
    std::set<Move>::iterator iter;
    for (iter = hm.possibleMoves.begin(); iter != hm.possibleMoves.end(); ++iter) {
        possibleMoves.insert(Move(iter->x, iter->y, evaluatePointScore(board, *iter)))
    }
}

inline int evaluatePointScore(int const board[BOARD_SIZE][BOARD_SIZE], Move move) {
    int res = 0;
    int role = (int)Role::HUMAN;
    std::string lines[4];
    std::string lines1[4];
    // TODO
    // AC自动机
}

inline int evaluate(){}
    // TODO
};

namespace __utils{
inline std::mt19937_64& getRandGen() {
    static std::random_device rd;
    static std::seed_seq seed{rd(), rd(), rd(), rd(), rd()};
    static std::mt19937_64 gen(seed);
    return gen;
}

inline uint64_t getRandUINT64() {
    std::uniform_int_distribution<uint64_t> dist;
    return dist(getRandGen());
}  
inline bool isLegalMove (int x, int y) {
    return (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE);
}

};
};

