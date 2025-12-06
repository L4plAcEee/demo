#pragma once
#ifdef _WIN32
#  define API_EXPORT __declspec(dllexport)
#else
#  define API_EXPORT
#endif

#ifdef __cplusplus
extern "C" {
#endif

/// 不透明的游戏句柄
typedef void* GameHandle;

/// 创建游戏句柄
API_EXPORT GameHandle gi_create();

/// 销毁游戏句柄
API_EXPORT void     gi_destroy(GameHandle handle);

/// 开始新游戏；firstPlayer: 0=HUMAN, 1=COMPUTER
API_EXPORT void     gi_start(GameHandle handle, int firstPlayer);

/// 人类落子；返回 1=成功，0=失败
API_EXPORT int      gi_human_move(GameHandle handle, int x, int y);

/// 获取电脑落子；输入上一步人类落子坐标（x,y），返回新坐标 via 指针
API_EXPORT void     gi_computer_move(GameHandle handle, int lastX, int lastY, int* outX, int* outY);

/// 重置到开局状态
API_EXPORT void     gi_reset(GameHandle handle);

/// 是否游戏结束；返回 1=是，0=否
API_EXPORT int      gi_is_over(GameHandle handle);

/// 获取胜者：0=HUMAN,1=COMPUTER,2=DRAW,-1=未结束
API_EXPORT int      gi_get_winner(GameHandle handle);

#ifdef __cplusplus
}
#endif
