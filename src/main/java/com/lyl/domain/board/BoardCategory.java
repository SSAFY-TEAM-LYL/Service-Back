package com.lyl.domain.board;

public enum BoardCategory {

    NOTICE("공지"),
    FREE("자유"),
    QUESTION("질문");

    private final String label;

    BoardCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
