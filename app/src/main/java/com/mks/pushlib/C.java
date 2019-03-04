package com.mks.pushlib;

public class C {
    public static String CODE_VERSION = "1.0.0";  //Версия кода
    public static boolean NEED_LOG    = false;     //Надо ли вести логирование
    public static String BASE_URL = "https://intlib.mks.group/logs-__DATA__/log/?pipeline=timestamper";  //Ссылка по которой будет отправляться статистика
    public static String PROXY_BASE_URL = "https://intlib.mks.group"; //По какой ссылке проверяем соединение для установки прокси //"https://fakegaid.appclick.org/gaid"
}
