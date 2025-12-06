#ifndef __USER_H
#define __USER_H

#define NAME_MAX_LEN 100
#define PASSWORD_MAX_LEN 50

typedef struct {
    int id;                      // 用户编号
    char name[NAME_MAX_LEN];     // 用户姓名
    char password[PASSWORD_MAX_LEN]; // 密码
} User;


int addUser(const User *user);
int searchUser(int id, User *result);
int deleteUser(int id);
int updateUser(const User *user);
int listUsers();

/* 
User登录权限验证
返回 1 表示成功验证权限
返回 0 表示未匹配用户名和密码
返回 -1 表示文件错误
*/
int loginUserAuthority(User *login_user);

#endif // __USER_H
