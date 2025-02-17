package com.sinohealth.system.domain.value.deliver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 资源类型
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-25 13:43
 */
public interface Resource extends Closeable {

    InputStream getInputStream() throws Exception;

    String getName();

    void clean();

    @Override
    default void close() throws IOException {
        try {
            InputStream inputStream = getInputStream();
            if (inputStream != null) {
                inputStream.close();
            }
            this.clean();
        } catch (Exception e) {

        }
    }

}
