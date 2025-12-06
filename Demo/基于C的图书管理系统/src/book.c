#include "__book.h"
#include "__utils.h"
#include <stdio.h>
#include <string.h>

int addBook(const Book *book) {
    FILE *fp = fopen(getBooksFilePath(), "ab");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getBooksFilePath());
        return -1;
    }
    fwrite(book, sizeof(Book), 1, fp);
    fclose(fp);
    printf("图书添加成功！\n");
    return 0;
}

int searchBook(int id, Book *result) {
    FILE *fp = fopen(getBooksFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getBooksFilePath());
        return -1;
    }
    Book book;
    while (fread(&book, sizeof(Book), 1, fp)) {
        if (book.id == id) {
            *result = book;
            fclose(fp);
            return 1;
        }
    }
    fclose(fp);
    return 0;
}

int deleteBook(int id) {
    FILE *fp = fopen(getBooksFilePath(), "rb");
    FILE *temp = fopen("temp.dat", "wb");
    if (fp == NULL || temp == NULL) {
        printf("无法打开文件！\n");
        return -1;
    }
    Book book;
    int found = 0;
    while (fread(&book, sizeof(Book), 1, fp)) {
        if (book.id != id) {
            fwrite(&book, sizeof(Book), 1, temp);
        } else {
            found = 1;
        }
    }
    fclose(fp);
    fclose(temp);
    if (found) {
        remove(getBooksFilePath());
        rename("temp.dat", getBooksFilePath());
        printf("图书删除成功！\n");
        return 1;
    } else {
        remove("temp.dat");
        printf("未找到该图书！\n");
        return 0;
    }
}

int updateBook(const Book *updatedBook) {
    FILE *fp = fopen(getBooksFilePath(), "rb");
    FILE *temp = fopen("temp.dat", "wb");
    if (fp == NULL || temp == NULL) {
        printf("无法打开文件！\n");
        return -1;
    }
    Book book;
    int found = 0;
    while (fread(&book, sizeof(Book), 1, fp)) {
        if (book.id == updatedBook->id) {
            fwrite(updatedBook, sizeof(Book), 1, temp);
            found = 1;
        } else {
            fwrite(&book, sizeof(Book), 1, temp);
        }
    }
    fclose(fp);
    fclose(temp);
    if (found) {
        remove(getBooksFilePath());
        rename("temp.dat", getBooksFilePath());
        printf("图书更新成功！\n");
        return 1;
    } else {
        remove("temp.dat");
        printf("未找到该图书！\n");
        return 0;
    }
}

int listBooks() {
    FILE *fp = fopen(getBooksFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getBooksFilePath());
        return -1;
    }

    Book book;
    printf("图书列表:\n");
    printf("编号\t书名\t\t作者\t\t数量\n");
    printf("-----------------------------------------------------\n");
    while (fread(&book, sizeof(Book), 1, fp)) {
        printf("%d\t%s\t\t%s\t\t%d\n", book.id, book.title, book.author, book.quantity);
    }
    fclose(fp);
    return 0;
}
