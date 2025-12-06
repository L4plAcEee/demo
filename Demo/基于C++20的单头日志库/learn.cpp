#include <format>
#include <source_location>
#include <iostream>
#include <string>
#include <cstdint>


#define FOREACH_LOG_LEVEL(f) \
    f(trace) \
    f(debug) \
    f(info) \
    f(warn) \
    f(error) \
    f(fatal)

enum class log_level : std::uint8_t {
#define L4_FUNCTION(name) name,
    FOREACH_LOG_LEVEL(L4_FUNCTION)
#undef L4_FUNCTION
};

// enum class log_level : std::uint8_t {
//     trace,
//     debug,
//     info,
//     warn,
//     error,
//     fatal,
// };

static auto max_log_level = log_level::info;

template <class T>
struct with_source_location {
private:
    T inner;
    std::source_location loc;

public:
    template <class U> requires std::constructible_from<T, U>
    consteval with_source_location(U &&inner, std::source_location loc = std::source_location::current()) : inner(std::forward<U>(inner)), loc(std::move(loc)) {}
    constexpr T const &format() const { return inner; }
    constexpr std::source_location const &location() const { return loc; }
};

std::string log_level_name(log_level lvl) {
    switch (lvl) {
#define L4_FUNCTION(name) case log_level::name: return #name;
        FOREACH_LOG_LEVEL(L4_FUNCTION)
#undef L4_FUNCTION
    }
    return "unknown";
};


template <typename... Args>
void generic_log(log_level lvl, with_source_location<std::format_string<Args...>> fmt, Args &&...args) {
    auto const &loc = fmt.location();
    std::cout << loc.file_name() << ':' << loc.line() << " [" << log_level_name(lvl) << "] " << std::vformat(fmt.format().get(), std::make_format_args(args...)) <<'\n';
}

void log(std::string msg, const char *file, int line) {
    std::cout << file << ':' << line << "[Info] "<< msg << '\n';
}

// 默认参数实际上是在调用者的地方初始化的 -> 哪里调用行号就是多少
void log(std::string msg, std::source_location loc = std::source_location::current()) {
    std::cout << loc.file_name() << ':' << loc.line() << "[Info] " << msg << '\n';
}

#define LOG(msg) log(msg, __FILE__, __LINE__)
#define LOG2(msg) log(msg, std::source_location::current())

#define L4_FUNCTION(name) \
template <typename... Args> \
void log_##name(with_source_location<std::format_string<Args...>> fmt, Args &&...args) { \
    return generic_log(log_level::name, std::move(fmt), std::forward<Args> (args)...); \
}
FOREACH_LOG_LEVEL(L4_FUNCTION)
#undef L4_FUNCTION

#define LOG_P(x) log_debug(#x "={}", x)

int32_t _main() {
    std::string str = "world";
    char c = 'a';
    int num = 42;
    double d = 3.14;

    auto s1 = std::format("hello {}, {}, {} {}", str, c, num, d);
    auto s2 = std::format("hello {2}, {1} {0}", str, c, num);
    auto s3 = std::format("hello {:+>4d}", 42);
    int n = 4;
    auto s4 = std::format("hello [{: ^+10.{}f}]", 13.14, n);
    std::cout << s1 << '\n' <<  s2 << '\n' <<  s3 << '\n' <<  s4 << '\n';

    auto sl = std::source_location{std::source_location::current()};
    std::cout << std::format("line = {}, column = {}, filename = {}, func = {}",
        sl.line(), sl.column(), sl.file_name(), sl.function_name()) << '\n';
    LOG("hello1");
    LOG2("hello2");
    log("hello3");
    generic_log(log_level::trace, "The answer is {}", 42);

    log_info("123");
    log_warn("456");
    log_error("789");
    LOG_P("logging");
}