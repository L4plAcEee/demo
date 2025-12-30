#ifndef __BORROW_H
#define __BORROW_H

#define DATE_MAX_LEN 20

typedef struct {
    int book_id;                 // 图书编号
    int user_id;                 // 用户编号
    char borrow_date[DATE_MAX_LEN]; // 借出日期
    char return_date[DATE_MAX_LEN]; // 归还日期（未归还时为空）
} BorrowRecord;


int borrowBook(int book_id, int user_id, const char *borrow_date);
int returnBook(int book_id, int user_id, const char *return_date);
int listBorrowRecords();

#endif // __BORROW_H
