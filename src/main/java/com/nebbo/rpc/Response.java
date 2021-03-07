package com.nebbo.rpc;

/**
 * Project: sms-service
 * Package: com.xl.rpc.rpc
 * FileName: Response
 * Author:   Administrator
 * Date:     2020/12/27 14:24
 */
public class Response {
    private long requestId; // 对应请求中携带的messageId
    private int status; // 99:异常 200:正常
    private Object content;// 响应内容 - 方法执行结果 、 异常信息

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "Response{" +
                "requestId=" + requestId +
                ", status=" + status +
                ", content=" + content +
                '}';
    }
}
