package com.recitation.utils;

import java.util.HashMap;
import java.util.Map;

public class AuthorNameUtils {

    private static final Map<String, String> ALIAS_TO_CANONICAL = new HashMap<>();

    static {
        registerAlias("鲁迅", "周树人");
        registerAlias("Lao She", "老舍");
        registerAlias("LaoShe", "老舍");
        registerAlias("Ba Jin", "巴金");
        registerAlias("BaJin", "巴金");
        registerAlias("Mao Dun", "茅盾");
        registerAlias("MaoDun", "茅盾");
        registerAlias("Cao Yu", "曹禺");
        registerAlias("CaoYu", "曹禺");
        registerAlias("Guo Moruo", "郭沫若");
        registerAlias("GuoMoruo", "郭沫若");
        registerAlias("Zhu Ziqing", "朱自清");
        registerAlias("ZhuZiqing", "朱自清");
        registerAlias("Bing Xin", "冰心");
        registerAlias("BingXin", "冰心");
        registerAlias("Xie Wanying", "冰心");
        registerAlias("XieWanying", "冰心");
        registerAlias("Li Bai", "李白");
        registerAlias("LiBai", "李白");
        registerAlias("Du Fu", "杜甫");
        registerAlias("DuFu", "杜甫");
        registerAlias("Bai Juyi", "白居易");
        registerAlias("BaiJuyi", "白居易");
        registerAlias("Su Shi", "苏轼");
        registerAlias("SuShi", "苏轼");
        registerAlias("Su Dongpo", "苏轼");
        registerAlias("SuDongpo", "苏轼");
        registerAlias("Xin Qiji", "辛弃疾");
        registerAlias("XinQiji", "辛弃疾");
        registerAlias("Lu You", "陆游");
        registerAlias("LuYou", "陆游");
        registerAlias("Wang Wei", "王维");
        registerAlias("WangWei", "王维");
        registerAlias("Tao Yuanming", "陶渊明");
        registerAlias("TaoYuanming", "陶渊明");
        registerAlias("Qu Yuan", "屈原");
        registerAlias("QuYuan", "屈原");
        registerAlias("Han Yu", "韩愈");
        registerAlias("HanYu", "韩愈");
        registerAlias("Liu Zongyuan", "柳宗元");
        registerAlias("LiuZongyuan", "柳宗元");
        registerAlias("Ouyang Xiu", "欧阳修");
        registerAlias("OuyangXiu", "欧阳修");
        registerAlias("Wang Anshi", "王安石");
        registerAlias("WangAnshi", "王安石");
        registerAlias("Zeng Gong", "曾巩");
        registerAlias("ZengGong", "曾巩");
        registerAlias("Su Zhe", "苏辙");
        registerAlias("SuZhe", "苏辙");
        registerAlias("Su Xun", "苏洵");
        registerAlias("SuXun", "苏洵");
        registerAlias("Fan Zhongyan", "范仲淹");
        registerAlias("FanZhongyan", "范仲淹");
        registerAlias("Wen Tianxiang", "文天祥");
        registerAlias("WenTianxiang", "文天祥");
        registerAlias("Yu Guangzhong", "余光中");
        registerAlias("YuGuangzhong", "余光中");
        registerAlias("Haizi", "海子");
        registerAlias("Hai Zi", "海子");
        registerAlias("Zha Haisheng", "海子");
        registerAlias("ZhaHaisheng", "海子");
        registerAlias("Bei Dao", "北岛");
        registerAlias("BeiDao", "北岛");
        registerAlias("Zhao Zhenkai", "北岛");
        registerAlias("ZhaoZhenkai", "北岛");
        registerAlias("Shu Ting", "舒婷");
        registerAlias("ShuTing", "舒婷");
        registerAlias("Gu Cheng", "顾城");
        registerAlias("GuCheng", "顾城");
        registerAlias("Gorky", "高尔基");
        registerAlias("Gorkiy", "高尔基");
        registerAlias("Pushkin", "普希金");
        registerAlias("Tolstoy", "托尔斯泰");
        registerAlias("Lev Tolstoy", "托尔斯泰");
        registerAlias("Dostoevsky", "陀思妥耶夫斯基");
        registerAlias("Chekhov", "契诃夫");
        registerAlias("Shakespeare", "莎士比亚");
        registerAlias("Dickens", "狄更斯");
        registerAlias("Hugo", "雨果");
        registerAlias("Balzac", "巴尔扎克");
        registerAlias("Maupassant", "莫泊桑");
        registerAlias("Flaubert", "福楼拜");
        registerAlias("Zola", "左拉");
        registerAlias("Goethe", "歌德");
        registerAlias("Schiller", "席勒");
        registerAlias("Kafka", "卡夫卡");
        registerAlias("Hemingway", "海明威");
        registerAlias("Twain", "马克·吐温");
        registerAlias("Mark Twain", "马克·吐温");
        registerAlias("Whitman", "惠特曼");
        registerAlias("Dickinson", "狄金森");
        registerAlias("Frost", "弗罗斯特");
        registerAlias("Tagore", "泰戈尔");
        registerAlias("Dante", "但丁");
        registerAlias("Boccaccio", "薄伽丘");
        registerAlias("Cervantes", "塞万提斯");
        registerAlias("Ibsen", "易卜生");
        registerAlias("Andersen", "安徒生");
        registerAlias("Grimm", "格林兄弟");
    }

    private static void registerAlias(String alias, String canonical) {
        ALIAS_TO_CANONICAL.put(alias.toLowerCase(), canonical);
    }

    public static String normalize(String authorName) {
        if (authorName == null) {
            return null;
        }

        String normalized = authorName.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        normalized = normalized.replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("　+", " ");
        normalized = normalized.replaceAll("[\\u00A0\\u2007\\u202F]+", " ");
        normalized = normalized.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        String lowerKey = normalized.toLowerCase();
        if (ALIAS_TO_CANONICAL.containsKey(lowerKey)) {
            return ALIAS_TO_CANONICAL.get(lowerKey);
        }

        String noSpaceKey = normalized.replaceAll("\\s+", "").toLowerCase();
        if (ALIAS_TO_CANONICAL.containsKey(noSpaceKey)) {
            return ALIAS_TO_CANONICAL.get(noSpaceKey);
        }

        return normalized;
    }

    public static String normalizeForQuery(String authorName) {
        return normalize(authorName);
    }

    public static boolean isSameAuthor(String name1, String name2) {
        String n1 = normalize(name1);
        String n2 = normalize(name2);
        if (n1 == null && n2 == null) return true;
        if (n1 == null || n2 == null) return false;
        return n1.equals(n2);
    }
}
