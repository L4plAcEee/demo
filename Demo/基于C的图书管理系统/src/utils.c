#include "__utils.h"
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

static char booksFilePath[MAX_PATH_LEN];
static char usersFilePath[MAX_PATH_LEN];
static char borrowFilePath[MAX_PATH_LEN];

void getExecutablePath(char *path, size_t size) {
    // GetModuleFileName是一个WindowsAPI，用于返回当前模块的完整路径
    if (GetModuleFileName(NULL, path, (DWORD)size) == 0) {
        perror("GetModuleFileName failed");
        exit(1);
    }
    char *last_backslash = strrchr(path, '\\');
    if (last_backslash) {
        *(last_backslash + 1) = '\0';
    }
}

void buildFilePath(char *fullPath, size_t size, const char *dir, const char *filename) {
    // snprintf 是C标准库中的一个函数，属于标准输入输出库（stdio.h）它的作用是格式化字符串并将其写入一个缓冲区，同时确保不会超出缓冲区的大小，从而避免缓冲区溢出的问题。
    snprintf(fullPath, size, "%s%s", dir, filename);
}

void initializeFilePaths() {
    char exePath[MAX_PATH_LEN];
    getExecutablePath(exePath, MAX_PATH_LEN);
    
    char dataDir[MAX_PATH_LEN];
    snprintf(dataDir, MAX_PATH_LEN, "%sdata\\", exePath);
    
    CreateDirectory(dataDir, NULL);
    
    buildFilePath(booksFilePath, MAX_PATH_LEN, dataDir, "books.dat");
    buildFilePath(usersFilePath, MAX_PATH_LEN, dataDir, "users.dat");
    buildFilePath(borrowFilePath, MAX_PATH_LEN, dataDir, "borrow.dat");
}


const char* getBooksFilePath() {
    return booksFilePath;
}

const char* getUsersFilePath() {
    return usersFilePath;
}

const char* getBorrowFilePath() {
    return borrowFilePath;
}
