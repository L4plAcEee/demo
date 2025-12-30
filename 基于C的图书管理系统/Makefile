# 编译器和编译选项
CC = gcc
CFLAGS = -Wall -Iinclude -finput-charset=UTF-8 -fexec-charset=UTF-8

# 源代码文件夹和目标文件夹
SRC_DIR = src
OBJ_DIR = obj
EXEC_DIR = disk

# 获取所有的 .c 文件和对应的 .o 文件
SRC = $(wildcard $(SRC_DIR)/*.c)
OBJ = $(SRC:$(SRC_DIR)/%.c=$(OBJ_DIR)/%.o)

# 可执行文件名
EXEC = $(EXEC_DIR)/library.exe

# 默认目标
all: $(EXEC)

# 创建目标目录（obj 和 disk）
$(OBJ_DIR) $(EXEC_DIR):
	mkdir -p $(OBJ_DIR) $(EXEC_DIR)

# 链接目标文件生成可执行文件
$(EXEC): $(OBJ) | $(EXEC_DIR)
	$(CC) $(CFLAGS) -o $@ $^

# 编译源文件生成目标文件
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c | $(OBJ_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

# 清理编译生成的文件
clean:
	rm -f $(OBJ_DIR)/*.o $(EXEC)
