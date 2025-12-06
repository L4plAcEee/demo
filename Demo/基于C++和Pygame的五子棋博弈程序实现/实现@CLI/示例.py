import ctypes
import os

# 加载 DLL 或 SO
os.add_dll_directory(r"D:\coding\TheWayofCode\demo\mini-gomoko\lib")
lib = ctypes.CDLL(r"D:\coding\TheWayofCode\demo\mini-gomoko\lib\GameEngine.dll")  # Windows 下为 .dll，Linux/macOS 下为 .so

# 定义函数签名
lib.gi_create.restype = ctypes.c_void_p
lib.gi_destroy.argtypes = [ctypes.c_void_p]
lib.gi_start.argtypes   = [ctypes.c_void_p, ctypes.c_int]
lib.gi_human_move.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int]
lib.gi_human_move.restype  = ctypes.c_int
lib.gi_computer_move.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int, ctypes.POINTER(ctypes.c_int), ctypes.POINTER(ctypes.c_int)]
lib.gi_is_over.argtypes  = [ctypes.c_void_p]
lib.gi_is_over.restype   = ctypes.c_int
lib.gi_get_winner.argtypes = [ctypes.c_void_p]
lib.gi_get_winner.restype  = ctypes.c_int
lib.gi_reset.argtypes    = [ctypes.c_void_p]

# 使用示例
handle = lib.gi_create()
lib.gi_start(handle, 0)  # human first

# 人类落子 (7,7)
ok = lib.gi_human_move(handle, 7, 7)
if ok:
    x, y = ctypes.c_int(), ctypes.c_int()
    lib.gi_computer_move(handle, 7, 7, ctypes.byref(x), ctypes.byref(y))
    print("电脑落子：", x.value, y.value)

# 查询结果
if lib.gi_is_over(handle):
    winner = lib.gi_get_winner(handle)
    print("胜者：", {0:"人类",1:"电脑",2:"平局"}[winner])

lib.gi_destroy(handle)
