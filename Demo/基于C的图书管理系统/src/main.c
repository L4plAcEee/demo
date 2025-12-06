#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "__book.h"
#include "__user.h"
#include "__borrow.h"
#include "__utils.h"

typedef enum currUserPermisson{
    user,
    admin
}currUserPermisson;

User login_user;
currUserPermisson userSign;

void addBookUI() {
    Book book;
    printf("输入图书编号: ");
    scanf("%d", &book.id);
    printf("输入书名: ");
    getchar(); // 清除缓冲区
    fgets(book.title, sizeof(book.title), stdin);
    book.title[strcspn(book.title, "\n")] = '\0'; // 去除换行符

    printf("输入作者: ");
    fgets(book.author, sizeof(book.author), stdin);
    book.author[strcspn(book.author, "\n")] = '\0';

    printf("输入数量: ");
    scanf("%d", &book.quantity);

    addBook(&book);
}

void searchBookUI() {
    int id;
    printf("输入要查询的图书编号: ");
    scanf("%d", &id);
    Book book;
    if (searchBook(id, &book) == 1) {
        printf("图书编号: %d\n", book.id);
        printf("书名: %s\n", book.title);
        printf("作者: %s\n", book.author);
        printf("数量: %d\n", book.quantity);
    } else {
        printf("未找到该图书！\n");
    }
}

void deleteBookUI() {
    int id;
    printf("输入要删除的图书编号: ");
    scanf("%d", &id);
    deleteBook(id);
}

void updateBookUI() {
    Book book;
    printf("输入要更新的图书编号: ");
    scanf("%d", &book.id);
    printf("输入新的书名: ");
    getchar(); // 清除缓冲区
    fgets(book.title, sizeof(book.title), stdin);
    book.title[strcspn(book.title, "\n")] = '\0'; // 去除换行符

    printf("输入新的作者: ");
    fgets(book.author, sizeof(book.author), stdin);
    book.author[strcspn(book.author, "\n")] = '\0';

    printf("输入新的数量: ");
    scanf("%d", &book.quantity);

    updateBook(&book);
}

void listBooksUI() {
    listBooks();
}

void addUserUI() {
    User user;
    printf("输入用户编号: ");
    scanf("%d", &user.id);
    printf("输入用户姓名: ");
    getchar(); // 清除缓冲区
    fgets(user.name, sizeof(user.name), stdin);
    user.name[strcspn(user.name, "\n")] = '\0';

    printf("输入密码: ");
    fgets(user.password, sizeof(user.password), stdin);
    user.password[strcspn(user.password, "\n")] = '\0';

    addUser(&user);
}

void searchUserUI() {
    int id;
    printf("输入要查询的用户编号: ");
    scanf("%d", &id);
    User user;
    if (searchUser(id, &user) == 1) {
        printf("用户编号: %d\n", user.id);
        printf("姓名: %s\n", user.name);
    } else {
        printf("未找到该用户！\n");
    }
}

void deleteUserUI() {
    int id;
    printf("输入要删除的用户编号: ");
    scanf("%d", &id);
    deleteUser(id);
}

void updateUserUI() {
    User user;
    printf("输入要更新的用户编号: ");
    scanf("%d", &user.id);
    printf("输入新的用户姓名: ");
    getchar(); // 清除缓冲区
    fgets(user.name, sizeof(user.name), stdin);
    user.name[strcspn(user.name, "\n")] = '\0';

    printf("输入新的密码: ");
    fgets(user.password, sizeof(user.password), stdin);
    user.password[strcspn(user.password, "\n")] = '\0';

    updateUser(&user);
}

void listUsersUI() {
    listUsers();
}

void borrowBookUI() {
    int book_id, user_id;
    char borrow_date[20];
    printf("输入图书编号: ");
    scanf("%d", &book_id);
    if(userSign == admin) {
        printf("输入用户编号: ");
        scanf("%d", &user_id);
    }else {
        user_id = login_user.id;
    }
    printf("输入借出日期 (YYYY-MM-DD): ");
    getchar(); // 清除缓冲区
    fgets(borrow_date, sizeof(borrow_date), stdin);
    borrow_date[strcspn(borrow_date, "\n")] = '\0';
    borrowBook(book_id, user_id, borrow_date);
}

void returnBookUI() {
    int book_id, user_id;
    char return_date[20];
    printf("输入图书编号: ");
    scanf("%d", &book_id);
    if(userSign == admin) {
        printf("输入用户编号: ");
        scanf("%d", &user_id);
    }else {
        user_id = login_user.id;
    }
    printf("输入归还日期 (YYYY-MM-DD): ");
    getchar(); // 清除缓冲区
    fgets(return_date, sizeof(return_date), stdin);
    return_date[strcspn(return_date, "\n")] = '\0';
    returnBook(book_id, user_id, return_date);
}

void listBorrowRecordsUI() {
    listBorrowRecords();
}

// 用户管理菜单
void userMenu() {
    int choice;
    while (1) {
        printf("\n==== 用户管理 ====\n");
        printf("1. 添加用户\n");
        printf("2. 查询用户\n");
        printf("3. 删除用户\n");
        printf("4. 修改用户\n");
        printf("5. 显示所有用户\n");
        printf("6. 返回主菜单\n");
        printf("选择操作: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                addUserUI();
                break;
            case 2:
                searchUserUI();
                break;
            case 3:
                deleteUserUI();
                break;
            case 4:
                updateUserUI();
                break;
            case 5:
                listUsersUI();
                break;
            case 6:
                return;
            default:
                printf("无效选择，请重试。\n");
        }
    }
}

// 图书管理菜单
void bookMenu() {
    int choice;
    while (1) {
        printf("\n==== 图书管理 ====\n");
        printf("1. 添加图书\n");
        printf("2. 查询图书\n");
        printf("3. 删除图书\n");
        printf("4. 修改图书\n");
        printf("5. 显示所有图书\n");
        printf("6. 返回主菜单\n");
        printf("选择操作: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                addBookUI();
                break;
            case 2:
                searchBookUI();
                break;
            case 3:
                deleteBookUI();
                break;
            case 4:
                updateBookUI();
                break;
            case 5:
                listBooksUI();
                break;
            case 6:
                return;
            default:
                printf("无效选择，请重试。\n");
        }
    }
}

// 借阅管理菜单
void borrowMenu() {
    int choice;
    while (1) {
        printf("\n==== 借阅管理 ====\n");
        printf("1. 借书\n");
        printf("2. 还书\n");
        printf("3. 显示所有借阅记录\n");
        printf("4. 返回主菜单\n");
        printf("选择操作: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                borrowBookUI();
                break;
            case 2:
                returnBookUI();
                break;
            case 3:
                listBorrowRecordsUI();
                break;
            case 4:
                return;
            default:
                printf("无效选择，请重试。\n");
        }
    }
}

// 主菜单
void mainMenu() {
    int choice;
    while (1) {
        printf("\n==== 图书管理系统 ====\n");
        printf("1. 图书管理\n");
        printf("2. 用户管理\n");
        printf("3. 借阅管理\n");
        printf("4. 退出\n");
        printf("选择操作: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                bookMenu();
                break;
            case 2:
                userMenu();
                break;
            case 3:
                borrowMenu();
                break;
            case 4:
                printf("退出系统。\n");
                exit(0);
            default:
                printf("无效选择，请重试。\n");
        }
    }
}

void mainUserMenu() {
    int choice;
    while (1) {
        printf("\n==== 图书管理系统 ====\n");
        printf("1. 借阅管理\n");
        printf("2. 显示所有书籍\n");
        printf("3. 退出\n");
        printf("选择操作: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                borrowMenu();
                break;
            case 2:
                listBooksUI();
                break;
            case 3:
                printf("退出系统。\n");
                exit(0);
            default:
                printf("无效选择，请重试。\n");
        }
    }
}

void loginMenu() {
    char adminname[NAME_MAX_LEN] = "admin";
    char adminpassword[PASSWORD_MAX_LEN] = "admin";
    while (1) {
        printf("\n==== 登录到图书管理系统 ====\n");
        char username[NAME_MAX_LEN];
        char password[PASSWORD_MAX_LEN];
        printf("输入用户名:\n");
        scanf("%s", username);
        printf("输入密码:\n");
        scanf("%s", password);
        if(strcmp(username, adminname) == 0 && strcmp(password, adminpassword) == 0) {
            userSign = admin;
            mainMenu();
            break;
        }
        strcpy(login_user.name, username);
        strcpy(login_user.password, password);
        if(loginUserAuthority(&login_user) == 1) {
            userSign = user;
            mainUserMenu();
            break;
        }
        printf("身份验证失败，请重新登录\n");
    }
}

int main() {
    initializeFilePaths();
    loginMenu();
    return 0;
}
