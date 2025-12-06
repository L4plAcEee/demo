#ifndef __UTILS_H
#define __UTILS_H

#define MAX_PATH_LEN 260

#include <stddef.h>
// 获取当前执行文件的目录
void getExecutablePath(char *path, size_t size);

// 拼接路径
void buildFilePath(char *fullPath, size_t size, const char *dir, const char *filename);

// 初始化文件路径
void initializeFilePaths();

// 访问Borrow.dat的函数
const char* getBorrowFilePath();

// 访问Users.dat的函数
const char* getUsersFilePath();

// 访问Books.dat的函数
const char* getBooksFilePath();

#endif // __UTILS_H
