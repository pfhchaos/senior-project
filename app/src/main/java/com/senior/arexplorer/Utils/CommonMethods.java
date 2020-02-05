package com.senior.arexplorer.Utils;

//If you put something non static in here you're going to Hell.
public class CommonMethods {

    public static int xMody (int x, int y){
        int result = x % y;
        return result < 0 ? result + y : result;
    }

    public static float xMody (float x, int y){
        float result = x % y;
        return result < 0 ? result + y : result;
    }
}
