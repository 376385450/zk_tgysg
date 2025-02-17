package com.sinohealth.common.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Huangzk
 * @date 2021/4/16 14:32
 */
public class BeanUtil {

    public static <T> Object copyProperties(Object source, Supplier<T> targetSupplier) {
        if (source == null || targetSupplier == null) {
            return null;
        }

        if (source instanceof Collection) {
            Collection srcCol = (Collection) source;
            List targets = new ArrayList(srcCol.size());
            for (Object src : srcCol) {
                T target = targetSupplier.get();
                doCopyProperties(src, target);
                targets.add(target);
            }
            return targets;
        }

        if (source.getClass().isArray()) {
            int length = Array.getLength(source);
            Object[] targets = new Object[length];
            for (int i = 0; i < length; i++) {
                T target = targetSupplier.get();
                doCopyProperties(Array.get(source, i), target);
                targets[i] = target;
            }
            return targets;
        }

        T target = targetSupplier.get();
        doCopyProperties(source, target);
        return target;
    }

    protected static void doCopyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }

}
