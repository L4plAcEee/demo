#include "GameInterface.h"
#include "GameEngine.h"
#include <utility>
#include <windows.h>

using namespace std;


struct GameWrapper {
    Board board;
    GameEngine& engine = GameEngine::getInstance();
};

extern "C" {
// 在 main 或 DLL 初始化时调用
SetConsoleOutputCP(CP_UTF8);
SetConsoleCP(CP_UTF8);

GameHandle gi_create() {
    return new GameWrapper();
}

void gi_destroy(GameHandle h) {
    delete static_cast<GameWrapper*>(h);
}

void gi_start(GameHandle h, int firstPlayer) {
    auto gw = static_cast<GameWrapper*>(h);
    gw->board.startGame(firstPlayer == 1 ? Role::COMPUTER : Role::HUMAN);
    if (firstPlayer == 1) {
        // 电脑先手
        auto mv = gw->engine.computerStartMove();
        gw->board.makeMove(mv.first, mv.second, Role::COMPUTER);
    }
}

int gi_human_move(GameHandle h, int x, int y) {
    auto gw = static_cast<GameWrapper*>(h);
    if (!gw->board.isValidMove(x, y)) return 0;
    return gw->board.makeMove(x, y, Role::HUMAN) ? 1 : 0;
}

void gi_computer_move(GameHandle h, int lastX, int lastY, int* outX, int* outY) {
    auto gw = static_cast<GameWrapper*>(h);
    auto mv = gw->engine.computerMove({lastX, lastY});
    gw->board.makeMove(mv.first, mv.second, Role::COMPUTER);
    if (outX) *outX = mv.first;
    if (outY) *outY = mv.second;
}

void gi_reset(GameHandle h) {
    auto gw = static_cast<GameWrapper*>(h);
    gw->board.resetGame();
}

int gi_is_over(GameHandle h) {
    auto gw = static_cast<GameWrapper*>(h);
    return gw->board.isGameOver() ? 1 : 0;
}

int gi_get_winner(GameHandle h) {
    auto gw = static_cast<GameWrapper*>(h);
    if (!gw->board.isGameOver()) return -1;
    auto w = gw->board.getWinner();
    if (w == Role::HUMAN)    return 0;
    if (w == Role::COMPUTER) return 1;
    return 2;
}

}  // extern "C"
