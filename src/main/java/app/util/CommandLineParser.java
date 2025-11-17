package main.java.app.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CommandLineParser {
    private CommandLineParser() {
    }

    // 옵션 값 조회
    // 예: args=["commit","-m","msg","-a","me"], option="-m" → "msg"
    public static String findOptionValue(String[] args, String option) {
        if (args == null || option == null) {
            return null;
        }
        for (int i = 1; i < args.length - 1; i++) {
            if (option.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }

    // 첫 토큰(명령어)을 제외한 나머지를 path 리스트로 추출
    // 예: ["add","a.txt","b.txt"] → ["a.txt","b.txt"]
    public static List<String> extractPaths(String[] args) {
        if (args == null || args.length <= 1) {
            return List.of();
        }
        return new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
    }
}
