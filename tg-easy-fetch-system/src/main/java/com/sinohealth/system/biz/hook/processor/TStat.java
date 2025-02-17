package com.sinohealth.system.biz.hook.processor;

import com.sinohealth.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-03-07 17:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TStat {
    private String name;
    /**
     * 执行中的任务
     */
    private Integer run;
    /**
     * 启动的线程
     */
    private Integer thread;

    private Integer core;
    private Integer max;
    private Long sum;
    private Integer queue;


    private Pair<String, String> buildName(String name, long act, int queue) {
        if (StringUtils.isNotBlank(name) && name.endsWith("-")) {
            name = name.substring(0, name.length() - 1);
        }
        if (act > 0 || queue > 0) {
            return Pair.of("🔴" + name, " Interrupt");
        }
        return Pair.of(name, "");
    }

    public String buildRow() {
        Pair<String, String> p = this.buildName(this.getName(), this.getRun(), this.getQueue());
        return String.format("%s run:%s thread:%s core:%s max:%s sum:%s wait:%s%s", p.getKey(),
                this.getRun(), this.getThread(), this.getCore(), this.getMax(),
                this.getSum(), this.getQueue(), p.getValue());
    }
}
