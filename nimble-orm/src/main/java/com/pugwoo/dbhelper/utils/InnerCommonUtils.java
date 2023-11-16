package com.pugwoo.dbhelper.utils;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 内部常用工具类
 */
public class InnerCommonUtils {

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static <T> Collection<T> filterNonNull(Collection<T> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        for (T t : list) {
            if (t == null) {
                // 因为list中出现null是小概率事件，所以这里不会每次都产生新list
                return filter(list, Objects::nonNull);
            }
        }
        return list;
    }

    /**
     * filter一个list
     */
    public static <T> List<T> filter(Collection<T> list, Predicate<? super T> predicate) {
        if(list == null) {
            return new ArrayList<>();
        }
        return list.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * filter一个数组
     */
    public static <T> List<T> filter(T[] array, Predicate<? super T> predicate) {
        if (array == null || array.length == 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        for (T t : array) {
            if (predicate.test(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public static <T> Stream<List<T>> partition(Stream<T> stream, int groupNum) {
        List<List<T>> currentBatch = new ArrayList<>(); //just to make it mutable
        currentBatch.add(new ArrayList<>(groupNum));
        return Stream.concat(stream
                .sequential()
                .map(t -> {
                    currentBatch.get(0).add(t);
                    return currentBatch.get(0).size() == groupNum ? currentBatch.set(0,new ArrayList<>(groupNum)) : null;
                }), Stream.generate(() -> currentBatch.get(0).isEmpty() ? null : currentBatch.get(0))
                .limit(1)
        ).filter(Objects::nonNull);
    }

    /**
     * 判断给定的数组是否非空
     */
    public static boolean isNotEmpty(String[] strings) {
        return strings != null && strings.length > 0;
    }

    /**
     * 检查strings中是否包含有checkStr字符串
     */
    public static boolean isContains(String checkStr, String[] strings) {
        if (!isNotEmpty(strings)) {
            return false;
        }

        boolean isContain = false;
        for (String str : strings) {
            if (Objects.equals(str, checkStr)) {
                isContain = true;
                break;
            }
        }

        return isContain;
    }

    /**
     * 将字符串str按间隔符sep分隔，返回分隔后的字符串
     * @param str 字符串
     * @param sep 间隔符
     * @return 会自动过滤掉空白(blank)的字符串；并且会自动trim()
     */
    public static List<String> split(String str, String sep) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }

        String[] splits = str.split(sep);
        List<String> result = new ArrayList<>();
        for (String s : splits) {
            if (InnerCommonUtils.isNotBlank(s)) {
                result.add(s.trim());
            }
        }
        return result;
    }

    public static String firstLetterUpperCase(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        String firstLetter = str.substring(0, 1).toUpperCase();
        return firstLetter + str.substring(1);
    }

    /**
     * 读取classpath目录下的资源，返回为String，默认是utf-8编码。
     * 如果需要其它编码，请获得byte[]之后自行转换。
     * 说明：当有多个同名的资源时，会返回第一个加载到jvm的资源内容，因此这里具有随机性。
     * @param path 路径，例如：abc.txt
     * @return 文件不存在返回null
     */
    public static String readClasspathResourceAsString(String path) {
        InputStream in = readClasspathResourceInputStream(path);
        if (in == null) {
            throw new RuntimeException(new FileNotFoundException(path));
        }
        return readAll(in, "UTF-8");
    }

    /**
     * 读取classpath目录下的资源，返回为InputStream。
     * 说明：当有多个同名的资源时，会返回第一个加载到jvm的资源内容，因此这里具有随机性。
     * @param path 路径，例如：abc.txt
     * @return 文件不存在返回null
     */
    private static InputStream readClasspathResourceInputStream(String path) {
        if (isBlank(path)) {
            return null;
        }
        // 分为以/开头和没有以/开头的path进行尝试，优先没有/开头的，以为classLoader的方式不需要/开头
        boolean beginWithSlash = path.startsWith("/");
        String noSlash = beginWithSlash ? path.substring(1) : path;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
        if (in != null) {
            return in;
        }

        // 尝试再用/开头的进行
        String withSlash = beginWithSlash ? path : "/" + path;
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(withSlash);
    }

    /**
     * 读取input所有数据到String中，可用于读取文件内容到String。
     * @param in 输入流会被关闭
     */
    private static String readAll(InputStream in, String charset) {
        BufferedReader reader = null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(in, charset);
            reader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String encodeBase64(byte[] bytes) {
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64(String str) {
        return java.util.Base64.getDecoder().decode(str);
    }

}
