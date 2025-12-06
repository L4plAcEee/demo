#include "board.h"
#include "AI.h"
#include <iostream>

void printBoard(const Board& board) {
    // 打印列坐标
    std::cout << "   ";
    for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
        if (x < 10) std::cout << x << "  ";
        else std::cout << x << " ";
    }
    std::cout << std::endl;
    for (uint64_t y = 0; y < BOARD_SIZE; ++y) {
        // 打印行坐标
        if (y < 10) std::cout << y << "  ";
        else std::cout << y << " ";
        for (uint64_t x = 0; x < BOARD_SIZE; ++x) {
            if (board.board[x][y] == player_t::BLACK) std::cout << "B  ";
            else if (board.board[x][y] == player_t::WHITE) std::cout << "W  ";
            else std::cout << ".  ";
        }
        std::cout << std::endl;
    }
}

int main() {
    Board board;
    AI ai(board, 4); // 可调整搜索深度
    player_t human = player_t::BLACK;
    player_t ai_player = player_t::WHITE;
    printBoard(board);
    while (true) {
        // 人类回合
        uint64_t x, y;
        std::cout << "请输入你的落子坐标 x y (0~" << (BOARD_SIZE-1) << "): ";
        std::cin >> x >> y;
        if (!board.place(x, y, human)) {
            std::cout << "无效落子，请重试。" << std::endl;
            continue;
        }
        ai.updateBadGridAfterMove(x, y);
        printBoard(board);
        if (board.checkWin(x, y, human)) {
            std::cout << "你赢了！" << std::endl;
            break;
        }
        // AI回合
        std::cout << "AI 正在思考..." << std::endl;
        auto aiMove = ai.makeMove(ai_player);
        board.place(aiMove.first, aiMove.second, ai_player);
        ai.updateBadGridAfterMove(aiMove.first, aiMove.second);
        std::cout << "AI 落子: (" << aiMove.first << ", " << aiMove.second << ")" << std::endl;
        printBoard(board);
        if (board.checkWin(aiMove.first, aiMove.second, ai_player)) {
            std::cout << "AI 获胜！" << std::endl;
            break;
        }
    }
    return 0;
}
