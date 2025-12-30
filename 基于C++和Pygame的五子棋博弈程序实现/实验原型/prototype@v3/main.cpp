#include "main.h"

int main() {
    Board& board = Board::getInstance();
    board.reset();

    Role currentRole = Role::HUMAN;
    Computer& com = Computer::getInstance(); 

    while (true) {
        board.print();

        std::string input;
        std::cout << "黑方 X 落子 (如 H8): ";
        std::cin >> input;

        if (input.length() < 2) {
            std::cout << "输入无效，请重新输入。\n";
            continue;
        }

        int y = toupper(input[0]) - 'A';
        int x = std::stoi(input.substr(1));
        Move move = Move(x, y, Role::HUMAN);
        if (!board.place(x, y, static_cast<int>(currentRole))) {
            std::cout << "该位置已被占用或非法，请重新输入。\n";
            continue;
        }

        if (board.checkWin(x, y)) {
            board.print();
            std::cout << "黑方胜利！\n";
            break;
        }

        std::cout << "AI 正在思考中...\n";
        Move bestMove = com.genMove(board.board, move);
        if (!board.place(bestMove.x, bestMove.y, static_cast<int>(currentRole ^ 1))) {
            std::cerr << "AI 落子失败，位置非法。\n";
            break;
        }
        std::cout << "白方 O 落子：" << static_cast<char>('A' + bestMove.y) << bestMove.x << "\n";

        if (board.checkWin(bestMove.x, bestMove.y)) {
            board.print();
            std::cout << "白方胜利！\n";
            break;
        }
    }

    return 0;
}
