package com.toucha.analytics.common.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.toucha.analytics.common.model.Tuple2;

/**
 * This helper use for improve the SQL query result more useful.
 * 
 * Ex. points: "{"10",20},{"30":30}" => sum point: 50
 * 
 * @author senhui.li
 */
public class QueryResultHelper {

    // And remember that in past time, there we need judge which type point user
    // win. So defined that -2 is basic point type (-1 is default value for
    // empty), other type were means win from lottery. In here I use -1 instead
    // of it.
    public static final String BASIC_POINT_TYPE = "-2";
    public static final String WIN_POINT_TYPE = "-1";

    /**
     * sum the JSON struct point together
     * 
     * @param pointsStr
     *            JSON point group
     * @return sum all points amount
     */
    public static int sumJSONPoints(String pointsStr) {
        int amount = 0;

        if (!Strings.isNullOrEmpty(pointsStr)) {
            String[] points = pointsStr.split(";");
            for (String point : points) {
                JSONObject json = JSON.parseObject(point);
                for (String key : json.keySet()) {
                    Integer tmp = json.getInteger(key);
                    amount += (tmp == null ? 0 : tmp.intValue());
                }
            }
        }

        return amount;
    }

    /**
     * Sum different type points amount
     * 
     * @param pointsStr
     *            JSON point group
     * @return a tuple, first value is basic point amount, second value is win
     *         point amount
     */
    public static Tuple2<Integer, Integer> sumJSONPointsGroupByType(String pointsStr) {

        int basicPointAmt = 0;
        int winPointAmt = 0;

        if (!Strings.isNullOrEmpty(pointsStr)) {
            String[] points = pointsStr.split(";");
            for (String point : points) {
                JSONObject json = JSON.parseObject(point);
                for (String key : json.keySet()) {
                    int tmp = json.getInteger(key) == null ? 0 : json.getInteger(key).intValue();
                    if (key.equals(BASIC_POINT_TYPE)) {
                        basicPointAmt += tmp;
                    } else {
                        winPointAmt += tmp;
                    }
                }
            }
        }


        return TupleUtil.tuple2(basicPointAmt, winPointAmt);
    }
}
