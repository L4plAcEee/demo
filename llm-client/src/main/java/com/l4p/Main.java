package com.l4p;

import com.l4p.utils.OllamaHelper;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {

    public static final String MODEL = "llama3.2";

    public static void main(String[] args) throws Exception {
        OllamaHelper ollama = new OllamaHelper();

        System.out.println("Test llama3.2:");
        ollama.chatStream(MODEL, "你是谁？");

//        KimiLLMHelper kimi = new KimiLLMHelper();
//
//        result = kimi.chat("moonshot-v1-8k", "用中文解释什么是二叉树", 0.7);
//        System.out.println("Kimi 回复: " + result);
    }
}
