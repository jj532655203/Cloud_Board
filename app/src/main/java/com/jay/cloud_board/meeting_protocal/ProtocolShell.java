package com.jay.cloud_board.meeting_protocal;

/**
 * Created by Jay on 2019/3/3.
 */

public class ProtocolShell {
    private Object body;

    public ProtocolShell(Object body) {
        this.body = body;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
