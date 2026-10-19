package com.example.historymaster.data;
public final class Regions  {
    public static final String[] IDS= {
        "china","japan","korea","mongol","vietnam","cambodia","laos"
    }
    ;
    public static final String[] NAMES= {
        "中国","日本","朝鮮半島","モンゴル","ベトナム","カンボジア","ラオス"
    }
    ;
    public static final String[] ARTICLES= {
        "中国の歴史","日本の歴史","朝鮮の歴史","モンゴルの歴史","ベトナムの歴史","カンボジアの歴史","ラオスの歴史"
    }
    ;
    public static int index(String id) {
        for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;
        return 0;
    }
    public static String safe(String id) {
        return IDS[index(id)];
    }
}
