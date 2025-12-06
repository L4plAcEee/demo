
### 配置 Qt 安装路径
CLion 中的设置方式：  
打开 File > Settings > Build, Execution, Deployment > CMake  

在 CMake options 添加：  
`-DCMAKE_PREFIX_PATH="D:\Qt\5.14.2\msvc2017_64\lib\cmake"`

### 快速复制qt的 dll
PowerShell环境下使用：  
`& "D:\Qt\5.14.2\mingw73_64\bin\windeployqt.exe" "D:\coding\just-learn\qt-learn\cmake-build-release-mingw730----qt5"`  

