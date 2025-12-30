# 学习笔记
## 核心实现
### 前置知识：基于 format 和 source_location
#### 一、std::format — 类型安全的字符串格式化
C++20 引入了 std::format，类似于 Python 的 str.format() 或 C# 的 string.Format()，它具有：  

| 特性   | 说明                                                    |
|------|-------------------------------------------------------|
| 类型安全 | 编译期检查格式字符串是否与参数匹配（如果使用 `std::format_string<Args...>`） |
| 性能好  | 比 `stringstream` 更快                                   |
| 语法直观 | 使用 `{}` 占位符，例如 `"Hello {}, age {}"`                   |

#### 二、std::source_location — 自动记录调用位置
C++20 新增 std::source_location，可在不手动传参的前提下，捕捉调用点的：
- 文件名
- 行号
- 函数名
- 列号

| 特性   | 说明                             |
|------|--------------------------------|
| 零侵入  | 不需要手动传 `__FILE__` 和 `__LINE__` |
| 自动捕捉 | 默认参数即可自动获取调用者位置                |
| 更安全  | 不依赖宏，作用于真正调用处而不是展开处            |


### 日志等级与输出控制实现
```c++
inline log_level g_max_level = [] () -> log_level {
    if (auto lev = std::getenv("MINILOG_LEVEL")) {
        return details::log_level_from_name(lev);
    }
    return log_level::info;
} ();

inline std::ofstream g_log_file = [] () -> std::ofstream {
    if (auto path = std::getenv("MINILOG_FILE")) {
        return std::ofstream(path, std::ios::app);
    }
    return std::ofstream();
} ();

inline void output_log(log_level lev, std::string msg, std::source_location const &loc) {
    std::chrono::zoned_time now{std::chrono::current_zone(), std::chrono::system_clock::now()};
    msg = std::format("{} {}:{} [{}] {}", now, loc.file_name(), loc.line(), details::log_level_name(lev), msg);
    if (g_log_file) {
        g_log_file << msg + '\n';
    }
    if (lev >= g_max_level) {
        std::cout << _MINILOG_IF_HAS_ANSI_COLORS(k_level_ansi_colors[(std::uint8_t)lev] +)
                    msg _MINILOG_IF_HAS_ANSI_COLORS(+ k_reset_ansi_color) + '\n';
    }
```
主要功能：  
1. 从环境变量读取默认日志等级和日志文件路径；
2. 使用 std::source_location 提供精确的调用位置；
3. 根据当前日志等级决定是否输出到 stdout；
4. 格式化输出内容并写入日志文件（如有）。

- 全局变量定义（使用 C++17/20 的 inline + lambda 初始化）
1. 获取最大日志等级 
    ```c++
    inline log_level g_max_level = [] () -> log_level {
        if (auto lev = std::getenv("MINILOG_LEVEL")) {
            return details::log_level_from_name(lev);
        }
        return log_level::info;
    } ();
    ```
    g_max_level：控制是否输出到 stdout 的最低日志等级；  
    从环境变量 MINILOG_LEVEL 获取用户设定值；  
    若未设置，则默认为 log_level::info；  
    details::log_level_from_name()：将字符串转换为枚举（如 "error" → log_level::error）；  

2. 获取日志文件路径  
    ```c++
    inline std::ofstream g_log_file = [] () -> std::ofstream {
        if (auto path = std::getenv("MINILOG_FILE")) {
            return std::ofstream(path, std::ios::app);  // 追加写入
        }
        return std::ofstream();  // 无效文件流
    } ();
    ```
    g_log_file：用于写入日志文件的 ofstream；
    从 MINILOG_FILE 环境变量中读取文件路径；
    若未设置则不启用文件写入；  

- 日志输出函数 output_log
```c++
inline void output_log(log_level lev, std::string msg, std::source_location const &loc) {
```
1. 时间戳与信息拼接
    ```cpp
    std::chrono::zoned_time now{std::chrono::current_zone(), std::chrono::system_clock::now()};
    msg = std::format("{} {}:{} [{}] {}", now, loc.file_name(), loc.line(), details::log_level_name(lev), msg); 
    ```
    使用 C++20 的 std::chrono::zoned_time 获取当前带时区的时间；
    source_location 提供调用点的文件名与行号；

2. 文件输出
    ```c++
    if (g_log_file) {
        g_log_file << msg + '\n';
    }
    ```
    如果 g_log_file 有效，就写入一行日志；  
    利用 ofstream 的隐式 bool 转换来判断是否可写；  

3. 控制台输出（条件等级控制 + 可选颜色）
```c++
if (lev >= g_max_level) {
    std::cout << _MINILOG_IF_HAS_ANSI_COLORS(k_level_ansi_colors[(std::uint8_t)lev] +)
                msg _MINILOG_IF_HAS_ANSI_COLORS(+ k_reset_ansi_color) + '\n';
}
``` 
若日志等级 ≥ g_max_level，才输出到 stdout；  
_MINILOG_IF_HAS_ANSI_COLORS(...) 是条件宏，用于在支持 ANSI 颜色时添加颜色（终端美化）；  
k_level_ansi_colors[...]：等级对应颜色码；  
k_reset_ansi_color：颜色重置码。  

### 日志功能实现
- `with_source_location<T>` 模板结构体  
```c++
template <class T>
struct with_source_location {
private:
    T inner;
    std::source_location loc;
```
这是一个泛型包装器，用于包装任意类型 T，  
并记录该对象创建时的 源代码位置（行号、文件、函数名等）。  

- 构造函数
```c++
template <class U> requires std::constructible_from<T, U>
consteval with_source_location(U &&inner, std::source_location loc = std::source_location::current())
    : inner(std::forward<U>(inner)), loc(std::move(loc)) {}
```
consteval：编译期常量求值，要求必须在编译期调用（这是很强的约束）。  
requires std::constructible_from<T, U>：约束 U 能够构造 T。  
std::source_location::current()：默认记录调用位置。  
> 整体逻辑是：用 U 构造 T，并记录调用位置。

- 提供接口
```c++
constexpr T const &format() const { return inner; }
constexpr std::source_location const &location() const { return loc; }
```
format() 返回封装的 T 对象（这里实际用于日志格式字符串）。  
location() 返回调用点的位置对象。  

- generic_log 日志函数模板
```c++
template <typename... Args>
void generic_log(log_level lev, details::with_source_location<std::format_string<Args...>> fmt, Args &&...args)
```
这是一个泛型日志函数：  
lev：日志级别。  
fmt：包装过源位置的格式字符串（std::format_string<Args...>）。  
args...：用于格式化的参数。  

- 实现逻辑
```c++
auto const &loc = fmt.location();
auto msg = std::vformat(fmt.format().get(), std::make_format_args(args...));
details::output_log(lev, std::move(msg), loc);
```
提取源代码位置。  
通过 std::vformat() 格式化字符串（fmt.format().get() 获取裸 format_string，然后与参数组装成 msg）。  
调用 details::output_log(...) 输出最终日志。  

## 工程技巧
### 宏孩儿养成计划之快速函数展开
```c++
#define _FUNCTION(name) \
template <typename... Args> \
void log_##name(details::with_source_location<std::format_string<Args...>> fmt, Args &&...args) { \
    return generic_log(log_level::name, std::move(fmt), std::forward<Args>(args)...); \
}
MINILOG_FOREACH_LOG_LEVEL(_FUNCTION)
#undef _FUNCTION
```
这段宏的目的是：为每个日志等级自动生成一组格式安全的日志函数（如 log_info(...), log_error(...) 等）。  

#### 宏展开前的结构
```c++
#define _FUNCTION(name) \
template <typename... Args> \
void log_##name(details::with_source_location<std::format_string<Args...>> fmt, Args &&...args) { \
    return generic_log(log_level::name, std::move(fmt), std::forward<Args>(args)...); \
}
```
这段宏做了以下几件事：  
定义一个函数模板 log_<name>（例如 log_info, log_debug 等）  
参数使用了封装过 source_location 的 std::format_string 和变参模板 Args...  
内部调用统一的 generic_log() 实现日志逻辑，传入当前日志等级  

#### 展开后的结构
```c++
template <typename... Args>
void log_trace(details::with_source_location<std::format_string<Args...>> fmt, Args &&...args) {
    return generic_log(log_level::trace, std::move(fmt), std::forward<Args>(args)...);
}

template <typename... Args>
void log_debug(details::with_source_location<std::format_string<Args...>> fmt, Args &&...args) {
    return generic_log(log_level::debug, std::move(fmt), std::forward<Args>(args)...);
}

// ...以及 info、critical、warn、error、fatal
```

####  宏作用域清理
`#undef _FUNCTION`
宏定义在调用完后立即取消，防止污染其他代码区域。
这是一种良好的 局部宏使用习惯。

#### 优势
批量生成代码  
编译期格式检查  
自动记录源位置  
接口友好  

### 宏孩儿养成计划之快速枚举展开
```c++
#define MINILOG_FOREACH_LOG_LEVEL(f) \
    f(trace) \
    f(debug) \
    f(info) \
    f(critical) \
    f(warn) \
    f(error) \
    f(fatal)
```
#### 工程示例
```c++
enum class log_level : std::uint8_t {
#define _FUNCTION(name) name,
    MINILOG_FOREACH_LOG_LEVEL(_FUNCTION)
#undef _FUNCTION
};
```
展开后等价于 ->  
```c++
enum class log_level : std::uint8_t {
    trace,
    debug,
    info,
    critical,
    warn,
    error,
    fatal
};
```
####  优势
统一管理日志等级，避免多处手动维护（减少错误、便于扩展）
宏元编程风格，可用于代码生成、重复性结构优化  

### 约定俗成：details命名空间
```c++
namespace details {...}
```
#### 用途
这种命名空间的用法，在大型项目或库开发中非常常见，  
尤其是在涉及库内部实现细节封装的时候。  
details（或 detail） 命名空间通常遵循如下约定： 
> 表示“内部实现细节”，不建议外部代码直接使用。

这是一种非强制的“访问控制”约定，用于辅助模块化设计。
#### 优势
增强封装性：隐藏实现细节，防止误用。  
命名冲突隔离：避免与公共命名空间发生冲突。  
提升代码可维护性：实现细节集中管理，未来修改不会影响外部接口。  



