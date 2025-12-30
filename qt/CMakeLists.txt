cmake_minimum_required(VERSION 3.31)
project(qt_learn)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 自动处理 Qt 的 UIC/MOC/RCC
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

# 查找 Qt 包（确保环境变量里 QT 路径是 MinGW 的）
find_package(Qt5 REQUIRED COMPONENTS Core Widgets)

# 添加源码
file(GLOB_RECURSE sources CONFIGURE_DEPENDS
        ${CMAKE_SOURCE_DIR}/*.cpp
)

# 手动过滤掉 build 目录下的 cpp 文件
list(FILTER sources EXCLUDE REGEX ".*/cmake-build-.*")

add_executable(qt_learn ${sources})

# 链接 Qt 库
target_link_libraries(qt_learn PRIVATE Qt5::Core Qt5::Widgets)