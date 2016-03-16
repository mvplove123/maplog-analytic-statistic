package com.jerry.map.utils;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by admin on 2016/3/2.
 */
public class Test {



    public static void main(String[] args) {
        String keyWord = "BEIJING Red Star 1,";



        if(StringUtils.isNotEmpty(keyWord)){
            if(keyWord.startsWith(",")){
                keyWord=keyWord.substring(1);
            }
            if(keyWord.endsWith(",")){
                keyWord = keyWord.substring(0,keyWord.length()-1);
            }
        }

        System.out.println(keyWord);
    }


}
