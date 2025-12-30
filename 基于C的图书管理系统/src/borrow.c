#include "__borrow.h"
#include "__utils.h"
#include "__book.h"
#include "__user.h"
#include <stdio.h>
#include <string.h>

// 借书
int borrowBook(int book_id, int user_id, const char *borrow_date) {
    // 检查图书是否存在且有库存
    Book book;
    if (searchBook(book_id, &book) != 1) {
        printf("图书不存在！\n");
        return -1;
    }
    if (book.quantity <= 0) {
        printf("图书库存不足！\n");
        return -1;
    }

    // 检查用户是否存在
    User user;
    if (searchUser(user_id, &user) != 1) {
        printf("用户不存在！\n");
        return -1;
    }

    // 减少图书库存
    book.quantity -= 1;
    if (updateBook(&book) != 1) {
        printf("更新图书库存失败！\n");
        return -1;
    }

    // 添加借阅记录
    BorrowRecord record;
    record.book_id = book_id;
    record.user_id = user_id;
    strncpy(record.borrow_date, borrow_date, DATE_MAX_LEN);
    record.borrow_date[DATE_MAX_LEN - 1] = '\0';
    record.return_date[0] = '\0'; // 未归还

    FILE *fp = fopen(getBorrowFilePath(), "ab");
    if (fp == NULL) {
        printf("无法打开借阅记录文件！\n");
        return -1;
    }
    fwrite(&record, sizeof(BorrowRecord), 1, fp);
    fclose(fp);
    printf("借书成功！\n");
    return 0;
}

// 还书
int returnBook(int book_id, int user_id, const char *return_date) {
    FILE *fp = fopen(getBorrowFilePath(), "rb");
    FILE *temp = fopen("temp_borrow.dat", "wb");
    if (fp == NULL || temp == NULL) {
        printf("无法打开借阅记录文件！\n");
        return -1;
    }

    BorrowRecord record;
    int found = 0;
    while (fread(&record, sizeof(BorrowRecord), 1, fp)) {
        if (record.book_id == book_id && record.user_id == user_id && strlen(record.return_date) == 0) {
            strncpy(record.return_date, return_date, DATE_MAX_LEN);
            record.return_date[DATE_MAX_LEN - 1] = '\0';
            fwrite(&record, sizeof(BorrowRecord), 1, temp);
            found = 1;
        } else {
            fwrite(&record, sizeof(BorrowRecord), 1, temp);
        }
    }
    fclose(fp);
    fclose(temp);

    if (found) {
        remove(getBorrowFilePath());
        rename("temp_borrow.dat", getBorrowFilePath());
        // 增加图书库存
        Book book;
        if (searchBook(book_id, &book) == 1) {
            book.quantity += 1;
            updateBook(&book);
        }
        printf("还书成功！\n");
        return 1;
    } else {
        remove("temp_borrow.dat");
        printf("未找到对应的借阅记录！\n");
        return 0;
    }
}

// 显示所有借阅记录
int listBorrowRecords() {
    FILE *fp = fopen(getBorrowFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开借阅记录文件：%s\n", getBorrowFilePath());
        return -1;
    }

    BorrowRecord record;
    printf("借阅记录列表:\n");
    printf("图书编号\t用户编号\t借出日期\t归还日期\n");
    printf("-------------------------------------------------------------\n");
    while (fread(&record, sizeof(BorrowRecord), 1, fp)) {
        printf("%d\t\t%d\t\t%s\t%s\n",
               record.book_id,
               record.user_id,
               record.borrow_date,
               strlen(record.return_date) ? record.return_date : "未归还");
    }
    fclose(fp);
    return 0;
}
