#include <unordered_map>
#include <mutex>
#include <vector>
#include <optional>
#include <cstdint>
#include <iostream>
#include <thread>
#include <vector>
#include <random>
#include <chrono>

using namespace std;

struct HashEntry {
    int score;
    int depth;
    int flag;
};

class ThreadSafeHashTable {
private:
    static const int SHARD_COUNT = 64;

    struct Shard {
        unordered_map<uint64_t, HashEntry> table;
        mutex lock;
    };

    Shard shards[SHARD_COUNT];

    int getShardIndex(uint64_t key) const {
        return key % SHARD_COUNT;
    }

public:
    optional<HashEntry> probe(uint64_t key) {
        int i = getShardIndex(key);
        lock_guard<mutex> guard(shards[i].lock);
        auto it = shards[i].table.find(key);
        if (it != shards[i].table.end()) {
            return it->second;
        }
        return nullopt;
    }

    void store(uint64_t key, const HashEntry& entry) {
        int i = getShardIndex(key);
        lock_guard<mutex> guard(shards[i].lock);
        shards[i].table[key] = entry;
    }

    void clear() {
        for (int i = 0; i < SHARD_COUNT; ++i) {
            lock_guard<mutex> guard(shards[i].lock);
            shards[i].table.clear();
        }
    }
};


uint64_t mockZobristHash(int threadId, int moveId) {
    // 用线程 ID 和模拟的落子序列组合成哈希值
    return static_cast<uint64_t>(threadId * 1000 + moveId);
}

void worker(ThreadSafeHashTable& table, int threadId) {
    for (int i = 0; i < 5; ++i) {
        uint64_t hash = mockZobristHash(threadId, i);

        // 模拟 negamax 计算出的得分
        int score = 100 + threadId * 10 + i;
        int depth = 3 + (i % 2);
        int flag = 0;

        HashEntry entry{score, depth, flag};
        table.store(hash, entry);

        // 模拟读取
        auto result = table.probe(hash);
        if (result) {
            cout << "[线程 " << threadId << "] 找到 hash=" << hash
                 << "，score=" << result->score << ", depth=" << result->depth << endl;
        } else {
            cout << "[线程 " << threadId << "] 没有找到 hash=" << hash << endl;
        }

        this_thread::sleep_for(chrono::milliseconds(50)); // 模拟计算时间
    }
}

int main() {
    ThreadSafeHashTable table;
    const int threadCount = 4;

    vector<thread> threads;
    for (int i = 0; i < threadCount; ++i) {
        threads.emplace_back(worker, std::ref(table), i);
    }

    for (auto& t : threads) {
        t.join();
    }

    cout << "\n测试完成，全部线程已退出。\n";

    return 0;
}