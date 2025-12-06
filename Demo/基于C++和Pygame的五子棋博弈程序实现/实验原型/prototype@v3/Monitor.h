#pragma once
#include "Common.h"


struct MonitorDataAboutSearch {
    long long score {std::numeric_limits<long long>::min()};
    long long zobristHashHitCnt {};
    int maxDepth {};
    int maxWidth {};

    long long updateScore(long long s) {
        score = std::max(score, s);
        return s;
    }
    
    long long updateHashHitCnt(long long zobristHashHitResult) {
        zobristHashHitCnt++;
        return zobristHashHitResult;
    }

    int updateMaxDepth(int startD, int d) {
        maxDepth = std::max(maxDepth, startD - d);
        return d;
    }

    int updateMaxWidth(int w) {
        maxWidth = std::max(maxWidth, w);
        return w;
    }

    void reset() {
        score = std::numeric_limits<long long>::min();
        zobristHashHitCnt = 0;
        maxDepth = 0;
        maxWidth = 0;
    }

    void logging() {
        std::cout 
        << "=====AI本轮着法数据如下=====\n"
        << "评分：" << score << '\n'
        << "ZobristHash命中次数: " << zobristHashHitCnt << '\n'
        << "最大搜索深度: " << maxDepth << '\n'
        << "最大搜索宽度: " << maxWidth << '\n'
        << "=====AI本轮着法数据如上=====\n";
    }
};
