package com.sinohealth.system.biz.application.util;


import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-01 10:26
 */
public interface Lambda {


    static <P, K> boolean hasDuplicateKey(List<P> list, Function<P, K> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        return list.stream().collect(Collectors.groupingBy(keyFunc))
                .entrySet().stream().anyMatch(v -> v.getValue().size() > 1);
    }

    static <P, R> List<R> buildListPost(List<P> list, Function<P, R> keyFunc, Predicate<R> post) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(keyFunc).filter(post).collect(Collectors.toList());
    }

    static <P, R> List<R> buildNonNullList(List<P> list, Function<P, R> keyFunc) {
        return buildListPost(list, keyFunc, Objects::nonNull);
    }

    static <P extends IdTable> Set<Long> buildSet(List<P> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashSet<>();
        }
        return list.stream().map(IdTable::getId).collect(Collectors.toSet());
    }


    static <P, R> Set<R> buildSet(List<P> list, Function<P, R> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashSet<>();
        }
        return list.stream().map(keyFunc).collect(Collectors.toSet());
    }

    static <P, R> Set<R> buildSet(List<P> list, Function<P, R> keyFunc, Predicate<R> filter) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashSet<>();
        }
        return list.stream().map(keyFunc).filter(filter).collect(Collectors.toSet());
    }

    static <P, R> List<R> buildList(Collection<P> list, Function<P, R> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(keyFunc).collect(Collectors.toList());
    }

    static <P extends IdTable> List<Long> buildList(List<P> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(IdTable::getId).collect(Collectors.toList());
    }


    static <P, R> String buildIdList(Collection<P> list, Function<P, R> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        return list.stream().map(keyFunc).map(Object::toString).collect(Collectors.joining(","));
    }

    static <E, K> Map<K, E> buildMap(List<E> list, Function<E, K> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(keyFunc, Function.identity(), (front, current) -> current));
    }

    static <E, K, V> Map<K, V> buildMap(List<E> list, Function<E, K> keyFunc, Function<E, V> valFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(keyFunc, valFunc, (front, current) -> current));
    }

    static <E, K, V> Map<K, V> buildMap(List<E> list, Function<E, K> keyFunc, Function<E, V> valFunc, Predicate<E> filter) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().filter(filter).collect(Collectors.toMap(keyFunc, valFunc, (front, current) -> current));
    }

    static <E, K> Map<K, List<E>> buildGroupMap(List<E> list, Function<E, K> keyFunc) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyFunc));
    }

    static <E, K> Map<K, List<E>> buildGroupMap(List<E> list, Function<E, K> keyFunc, Predicate<E> filter) {
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>();
        }
        return list.stream().filter(filter).collect(Collectors.groupingBy(keyFunc));
    }

    static <P, K, V> Map<K, Set<V>> buildGroupMapSet(Collection<P> params,
                                                     Function<P, K> keyFunc,
                                                     Function<P, V> valFunc) {
        if (CollectionUtils.isEmpty(params)) {
            return new HashMap<>();
        }
        return params.stream().collect(Collectors.groupingBy(keyFunc,
                Collectors.mapping(valFunc, Collectors.toCollection(HashSet::new))
        ));
    }

    static <P, K> Map<K, String> buildGroupMapString(Collection<P> params,
                                                     Function<P, K> keyFunc,
                                                     Function<P, String> valFunc,
                                                     String delimiter) {
        if (CollectionUtils.isEmpty(params)) {
            return new HashMap<>();
        }
        return params.stream().collect(Collectors.groupingBy(keyFunc,
                Collectors.mapping(valFunc, Collectors.joining(delimiter))
        ));
    }

    /**
     * @see Lambda#concatId(List)
     */
    static List<Long> splitId(String val) {
        if (StringUtils.isBlank(val)) {
            return new ArrayList<>();
        }

        return Arrays.stream(StringUtils.split(val, ",")).map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * @see Lambda#splitId(String)
     */
    static String concatId(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return "";
        }
        return ids.stream().map(v -> v + "").collect(Collectors.joining(","));
    }

    // 服务查询
    static <R, K, P extends Collection> Map<K, R> queryMapIfExist(P params, Function<P, Map<K, R>> apply) {
        if (CollectionUtils.isNotEmpty(params)) {
            return apply.apply(params);
        }
        return new HashMap<>();
    }


    static <R, P> Optional<R> queryOneIfExist(P params, Function<P, R> apply) {
        if (Objects.isNull(params)) {
            return Optional.empty();
        }

        return Optional.ofNullable(apply.apply(params));
    }

    static <R, P extends Collection> List<R> queryListIfExist(P params, Function<P, List<R>> apply) {
        if (CollectionUtils.isNotEmpty(params)) {
            return apply.apply(params);
        }
        return new ArrayList<>();
    }

    static <R, K, P extends Collection> Map<K, R> queryMapIfExist(P params, Function<P, List<R>> queryFunc, Function<R, K> keyFunc) {
        if (CollectionUtils.isNotEmpty(params)) {
            return queryFunc.apply(params).stream().collect(Collectors.toMap(keyFunc, v -> v, (front, current) -> current));
        }
        return new HashMap<>();
    }

    static <R, K, V, P extends Collection> Map<K, V> queryMapIfExist(P params, Function<P, List<R>> queryFunc,
                                                                     Function<R, K> keyFunc, Function<R, V> valFunc) {
        if (CollectionUtils.isNotEmpty(params)) {
            return queryFunc.apply(params).stream().collect(Collectors.toMap(keyFunc, valFunc, (front, current) -> current));
        }
        return new HashMap<>();
    }
}
