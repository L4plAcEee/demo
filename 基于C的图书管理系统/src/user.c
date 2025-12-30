#include "__user.h"
#include "__utils.h"
#include <stdio.h>
#include <string.h>

int addUser(const User *user) {
    FILE *fp = fopen(getUsersFilePath(), "ab");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getUsersFilePath());
        return -1;
    }
    fwrite(user, sizeof(User), 1, fp);
    fclose(fp);
    printf("用户添加成功！\n");
    return 0;
}

int searchUser(int id, User *result) {
    FILE *fp = fopen(getUsersFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getUsersFilePath());
        return -1;
    }
    User user;
    while (fread(&user, sizeof(User), 1, fp)) {
        if (user.id == id) {
            *result = user;
            fclose(fp);
            return 1;
        }
    }
    fclose(fp);
    return 0;
}

int loginUserAuthority(User *login_user) {
    FILE *fp = fopen(getUsersFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getUsersFilePath());
        return -1;
    }
    User user;
    while (fread(&user, sizeof(User), 1, fp)) {
        if (strcmp(user.name, login_user->name) == 0 && strcmp(user.password, login_user->password) == 0) {
            login_user->id = user.id;
            fclose(fp);
            return 1;
        }
    }
    fclose(fp);
    return 0;
}

int deleteUser(int id) {
    FILE *fp = fopen(getUsersFilePath(), "rb");
    FILE *temp = fopen("temp_users.dat", "wb");
    if (fp == NULL || temp == NULL) {
        printf("无法打开文件！\n");
        return -1;
    }
    User user;
    int found = 0;
    while (fread(&user, sizeof(User), 1, fp)) {
        if (user.id != id) {
            fwrite(&user, sizeof(User), 1, temp);
        } else {
            found = 1;
        }
    }
    fclose(fp);
    fclose(temp);
    if (found) {
        remove(getUsersFilePath());
        rename("temp_users.dat", getUsersFilePath());
        printf("用户删除成功！\n");
        return 1;
    } else {
        remove("temp_users.dat");
        printf("未找到该用户！\n");
        return 0;
    }
}

int updateUser(const User *updatedUser) {
    FILE *fp = fopen(getUsersFilePath(), "rb");
    FILE *temp = fopen("temp_users.dat", "wb");
    if (fp == NULL || temp == NULL) {
        printf("无法打开文件！\n");
        return -1;
    }
    User user;
    int found = 0;
    while (fread(&user, sizeof(User), 1, fp)) {
        if (user.id == updatedUser->id) {
            fwrite(updatedUser, sizeof(User), 1, temp);
            found = 1;
        } else {
            fwrite(&user, sizeof(User), 1, temp);
        }
    }
    fclose(fp);
    fclose(temp);
    if (found) {
        remove(getUsersFilePath());
        rename("temp_users.dat", getUsersFilePath());
        printf("用户更新成功！\n");
        return 1;
    } else {
        remove("temp_users.dat");
        printf("未找到该用户！\n");
        return 0;
    }
}

int listUsers() {
    FILE *fp = fopen(getUsersFilePath(), "rb");
    if (fp == NULL) {
        printf("无法打开文件：%s\n", getUsersFilePath());
        return -1;
    }

    User user;
    printf("用户列表:\n");
    printf("编号\t姓名\n");
    printf("-----------------------\n");
    while (fread(&user, sizeof(User), 1, fp)) {
        printf("%d\t%s\n", user.id, user.name);
    }
    fclose(fp);
    return 0;
}
