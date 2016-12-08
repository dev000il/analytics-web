package com.toucha.analytics.common.util;

public class NextPowerOf2Util {

    /**
     * 
     * @Title: 		 nextPowerOf2   
     * @Description:  calculate the smallest number which bigger than param of a
     * advice use this method to initialization the initial size of Map or List   
     * @param a
     * @return   
     * @throws
     */
    public static Integer nextPowerOf2(final int inputNum) {
        int outputNum = 1;
        while (outputNum < inputNum) {
            outputNum = outputNum << 1;
        }
        return outputNum;
    }
}
