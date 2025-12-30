#ifndef __BOOK_H
#define __BOOK_H

#define TITLE_MAX_LEN 100
#define AUTHOR_MAX_LEN 100

typedef struct {
    int id;                      // 图书编号
    char title[TITLE_MAX_LEN];   // 书名
    char author[AUTHOR_MAX_LEN]; // 作者
    int quantity;                // 库存数量
} Book;

int addBook(const Book *book);
int searchBook(int id, Book *result);
int deleteBook(int id);
int updateBook(const Book *book);
int listBooks();

#endif // __BOOK_H
