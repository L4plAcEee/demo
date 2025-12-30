#include<iostream>
#include<string>
#include <mutex>
using namespace std;

class singleton {
    singleton() {}
    /*
     *这是 C++11 引入的一种语法，
     *用于显式禁用类的拷贝构造函数与赋值运算符。
     *在实现单例模式时，它非常关键，
     *目的是防止程序员通过拷贝或赋值创建多个实例，破坏单例的唯一性。
     *
     */
    singleton(const singleton&) = delete;
    singleton& operator=(const singleton&) = delete;
    /*
     *防止用户意外或故意创建多个实例，保持单例模式的唯一性语义。
     *
     */
public:
    string name;
    int age;

    /*
     * 使用 new 创建对象却没有释放。
     * 可以考虑用智能指针或者注册 atexit 在程序退出时删除指针。
     * 更现代做法如下
     * 这不仅线程安全，而且不会内存泄漏。
     * ✅ C++11+ 推荐写法（使用局部静态变量）：
     *
     */
    static singleton* getInstance() {
        static singleton instance;
        return &instance;
    }
    /*
     *在 C++11 及以后的标准中，这种局部 static 实例在第一次调用时构造，
     *且线程安全（由编译器自动管理）。
     *避免了显式使用 new 和 delete，杜绝了内存泄漏问题。
     *
     */

    /*
     *展示了如何在单例中添加行为方法。
     *使用静态方法封装业务逻辑是非常实用的技巧，
     *适合封装工具类或状态控制器等。
     *
     */
    static void sayHello() {
        singleton* s = singleton::getInstance();
        cout << "I am " << s->name << ", I am " << s->age << " old !" << '\n';
    }
};

// 懒汉式
class lazy_singleton{
    static lazy_singleton* instance;
    static mutex mtx;

    lazy_singleton(){}
    lazy_singleton(const lazy_singleton&) = delete;
    lazy_singleton& operator=(const lazy_singleton&) = delete;
public:
    /*
     *双锁法构造lazy_singleton
     *
     */
    static lazy_singleton* getInstance() {
        if (instance == nullptr) {
            std::lock_guard<std::mutex> lock(mtx); // 线程锁
                if (instance == nullptr) {
                    instance = new lazy_singleton();
                }
            return instance;
        }

    }
};

lazy_singleton* lazy_singleton::instance = nullptr;
std::mutex lazy_singleton::mtx;

// 饿汉式
class eager_singleton {
    static eager_singleton instance;
    eager_singleton() {}
    eager_singleton(const eager_singleton&) = delete;
    eager_singleton& operator=(const eager_singleton&) = delete;

public:
    static eager_singleton& getInstance() {
        return instance;
    }
};

eager_singleton eager_singleton::instance;

int main() {
    singleton* s1 = singleton::getInstance();
    s1->name = "Wang ni ma";
    s1->age = 18;
    s1->sayHello();
    singleton* s2 = singleton::getInstance();
    s2->sayHello();
    s2->name = "Xiao ming";
    s2->age = 16;
    s2->sayHello();

    cout << string(60, '-') << '\n';


    return 0;
}
