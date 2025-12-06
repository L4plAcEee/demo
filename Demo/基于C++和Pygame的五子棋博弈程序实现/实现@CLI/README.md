# mini-gomoku

## 优势

- 单头文件实现，可以直接引入并快速制作游戏原型（说人话就是一件生成课设）

## 核心算法实现

### 负极大搜索

- 参考 [https://web.archive.org/web/20071030220820/http://www.brucemo.com/compchess/programming/minmax.htm](https://web.archive.org/web/20071030220820/http://www.brucemo.com/compchess/programming/minmax.htm)

### Alpha-Beta 剪枝

- 参考 [https://web.archive.org/web/20071030084528/http://www.brucemo.com/compchess/programming/alphabeta.htm](https://web.archive.org/web/20071030084528/http://www.brucemo.com/compchess/programming/alphabeta.htm)

### ZobristHash置换表

- 参考 [https://web.archive.org/web/20071031100051/http://www.brucemo.com/compchess/programming/hashing.htm](https://web.archive.org/web/20071031100051/http://www.brucemo.com/compchess/programming/hashing.htm)

### 启发函数策略

通过模式匹配，简单的匹配了各种不同棋形，并统计得分。  
匹配函数可以优化为KMP或者AC自动机，效率更高。  
