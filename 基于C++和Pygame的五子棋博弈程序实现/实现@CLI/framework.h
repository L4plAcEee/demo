#pragma once
// C风格的框架搭建尝试
#define BOARD_SIZE (int)(15)
#define UNK_VAL (int)(1e9 + 1)
#define HASH_MASK (int)((1 << 16) - 1)

struct move {
    int cx;
    int cy;

    move(int x, int y) {x = cx, y = cy;}
};

struct hash_item {
    enum flag {UNK, ALPHA, BETA, EXACT};
    int val;
    int d;
    move m;
    flag f;
};

struct cell {
    unsigned long long zobristhash[2];
    int score;
    player_t p;
};


enum player_t {
    FORBID = -2,
    ENPTY,
    BLACK,
    WHITE
};

enum pattern_t {
    FIVE = 0,
    FOUR,
    BLOCKED_FOUR,
    THREE,
    BLOCKED_THREE,
    TWO,
    BLOCKED_TWO,
    NONE, 
    PATTERN_T
};

const int pattern_score[PATTERN_T] = {
    100000,   // FIVE（必胜）
    10000,    // FOUR（活四）
    5000,     // BLOCKED_FOUR（冲四）
    1000,     // THREE（活三）
    500,      // BLOCKED_THREE（眠三）
    200,      // TWO（活二）
    100,       // BLOCKED_TWO（眠二）
    0
};

// 四个方向：横、竖、主对角线、副对角线
const int dir[4][2] = {{1, 0}, {0, 1}, {1, 1}, {1 , -1}};





hash_item hash_table[HASH_MASK];
cell board[BOARD_SIZE][BOARD_SIZE];
int turn;
player_t cureent_player;
unsigned long long zobristhash;

move get_best_move() {

}

int __negamax(int depth, int alpha, int beta) {
    int val = __probe_hash_hit(depth, alpha, beta);
    if (val != UNK_VAL) {
        return val;
    }

    if (depth == 0) {
        val = __evaluate();
        __record_hash();
        return val;
    }

    move* moves;
    unsigned int moves_s = __gen_moves(&moves);
    for (int i = 0; i < moves_s; ++i) {
        __make_move();
        val = -__negamax(depth - 1, -beta, -alpha);
        __unmake_move();
        alpha = val > alpha ? val : alpha;
        if (alpha > beta) {
            __record_hash();
            return beta;
        }
    }
    __record_hash();
    return alpha;
}

int __record_hash() {

}

int __evaluate_point() {

}

int __evaluate() {

}

int __probe_hash_hit(int d, int a, int b) {

}

int __make_move() {

}

int __unmake_move() {

}

unsigned int __gen_moves(move** m) {

}

int __rebuild_board() {

}

int __init() {
    zobristhash = __get_rand_ulld();
    cell *c;
    for (int i = 0; i < BOARD_SIZE; ++i) {
        for (int j = 0; j < BOARD_SIZE; ++j) {
            c = &board[i][j];
            c->zobristhash[0] = __get_rand_ulld();
            c->zobristhash[1] = __get_rand_ulld();
            c->p = player_t::ENPTY;
            c->score = 0;
        }
    }
    return 1;
}

bool __is_legal (int x, int y) {
    return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
}

unsigned long long __get_rand_ulld() {

}
