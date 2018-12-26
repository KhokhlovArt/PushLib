package com.mks.pushlib.ExternalLibServicer;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dalvik.system.DexClassLoader;

public class ExternalLibServicer {
    public ExternalLibServicer(){

    }

    //*********************************************************************************************
    //*********************************************************************************************
    //*********************** Методы запуска кода из DEX-а через рефлексию ************************
    //*********************************************************************************************
    //*********************************************************************************************

    public Class getClass(String className)
    {

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("" + e.getMessage());
        };
        return null;
    }


    public Object getInstance(Class clazz, Object[] arg, Class[] argClass )
    {
        Object objOfClass = null;
        Constructor<?> constructor = null;
        if (clazz != null) {
            try {
                if (arg.length > 0) {
                    constructor = clazz.getDeclaredConstructor(argClass);
                    objOfClass = constructor == null ? null : constructor.newInstance(arg);
                } else {
                    constructor = clazz.getDeclaredConstructor();
                    objOfClass = constructor == null ? null : constructor.newInstance();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return objOfClass;
    }

    public <T> T callMethod(Class clazz, Object objOfClass, String methodName, Object[] arg, Class[] argClass )
    {
        if ((clazz != null) && (objOfClass != null)) {
            try {
                Method exists = clazz.getMethod(methodName, argClass);
                exists.setAccessible(true);
                return (T) exists.invoke(objOfClass, arg);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public <T> T callStaticMethod(Class clazz, String methodName, Object[] arg, Class[] argClass )
    {
        if (clazz != null) {
            try {
                Method exists = clazz.getMethod(methodName, argClass);
                exists.setAccessible(true);
                return (T) exists.invoke(null, arg);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public <T> T getAttribute(Class clazz, Object objOfClass, String fildName)
    {
        Field field = null;
        if ((clazz != null) && (objOfClass != null)) {
            try {
                field = clazz.getDeclaredField(fildName);
                field.setAccessible(true);
                return (T) field.get(objOfClass);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean setAttribute(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }
    /*
    Метод возвращает параметр из Enum-а из внешнего dex-а
    @params clazz - Класс Enum-а из которого
    @params i     - Номер по порядку какой параметр вернуть (при "Enum Color {RED, GREEN, BLUE}" 0 - вернеет RED, 1 - вернеет GREEN ...)
    Пример использования:
        Class IApi_RestReadType = loader.getExternalClass(getApplicationContext(), "com.advertising_id_service.appclick.googleadvertisingidservice.REST.IApi$RestReadType");
        loader.getEnumValue(IApi_RestReadType, 0)
    */
    public <T> T getEnumValue(Class clazz, int i)
    {
        if ((clazz != null) && (clazz.isEnum())) {
            return (T) clazz.getEnumConstants()[i];
        }
        return null;
    }
    //*********************************************************************************************
}
