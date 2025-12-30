package com.l4p;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
public class Main {
    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ScheduleService scheduleService = new ScheduleService();
        scheduleService.schedule(() -> {
            System.out.println(LocalDateTime.now().format(dtf) + "任务一");
        }, 100);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("添加一个 每200毫秒打印一个2 的定时任务");
        scheduleService.schedule(() -> {
            System.out.println(LocalDateTime.now().format(dtf) + "任务二");
        }, 200);
    }

}