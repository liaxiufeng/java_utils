package com.liujun.pinyin;

import java.util.Scanner;

/**
 * 类说明
 *
 * @author liujun
 * @date 2023/1/10
 */
public class PinYinMain {
    public static void main(String[] args) {
        //获取输入的内容，获取到一行为c时结束
        Scanner scanner = new Scanner(System.in);
        StringBuffer sb = new StringBuffer();
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            if ("exit".equals(input)) {
                break;
            }
            sb.append(PinYinUtil.getPingYin(input)+"\n");
        }
        System.out.println(sb.toString().replaceAll(" ",""));
    }
}

