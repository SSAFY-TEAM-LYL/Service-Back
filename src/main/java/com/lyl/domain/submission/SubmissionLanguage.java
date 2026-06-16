package com.lyl.domain.submission;

import java.util.Arrays;

public enum SubmissionLanguage {

    PYTHON3(71, "Python 3"),
    JAVA(62, "Java"),
    CPP(54, "C++");

    private final int judge0LanguageId;
    private final String label;

    SubmissionLanguage(int judge0LanguageId, String label) {
        this.judge0LanguageId = judge0LanguageId;
        this.label = label;
    }

    public int judge0LanguageId() {
        return judge0LanguageId;
    }

    public String label() {
        return label;
    }

    public static SubmissionLanguage from(String value) {
        return Arrays.stream(values())
                .filter(language -> language.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 언어입니다."));
    }
}
