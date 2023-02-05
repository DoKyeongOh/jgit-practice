package org.example;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

public class JGitUtilTest extends TestCase {

    JGitUtil jGitUtil = JGitUtil.builder()
            .remoteRepo("https://github.com/DoKyeongOh/GithubPracticeRepo.git")
            .localRepo("/Users/dokyeongoh/toy_project/gitTestDirectory")
            .username("dkproh")
            .email("dkproh@gmail.com")
            .build();

    public void testPushIfChanged() {
        Set<String> scanFilenameSet = new HashSet<>();
        scanFilenameSet.add("hello.txt");
        scanFilenameSet.add("test.txt");
        jGitUtil.pushIfChanged(scanFilenameSet);
    }

    public void test() {
        System.out.println("hello");
    }
}