package com.hdu.sink;

import com.hdu.result.RiskResult;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class ExternalSystemSink extends RichSinkFunction<RiskResult> {
    @Override
    public void invoke(RiskResult value, Context context) throws Exception {
        // 推送到AAA或派网系统，例如通过HTTP接口、消息队列等
        System.out.println("推送风险结果：" + value);
    }
}
