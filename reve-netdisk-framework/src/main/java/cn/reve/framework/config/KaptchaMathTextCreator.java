package cn.reve.framework.config;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

import java.util.Random;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/4/22 18:03
 */
public class KaptchaMathTextCreator extends DefaultTextCreator {
    private static final String[] CNUMBERS = "0,1,2,3,4,5,6,7,8,9,10".split(",");

    @Override
    public String getText() {
        Integer result = 0;
        Random random = new Random();
        int x = random.nextInt(10);
        int y = random.nextInt(10);
        StringBuilder strRes = new StringBuilder();

        int operator = (int) Math.round(Math.random() * 3);
        if (operator == 0) {
            result = x + y;
            strRes.append(CNUMBERS[x]);
            strRes.append("+");
            strRes.append(CNUMBERS[y]);
        } else if (operator == 1) {
            if (x >= y) {
                result = x - y;
                strRes.append(CNUMBERS[x]);
                strRes.append("-");
                strRes.append(CNUMBERS[y]);
            } else {
                result = y - x;
                strRes.append(CNUMBERS[y]);
                strRes.append("-");
                strRes.append(CNUMBERS[x]);
            }
        } else if (operator == 2) {
            result = x * y;
            strRes.append(CNUMBERS[x]);
            strRes.append("*");
            strRes.append(CNUMBERS[y]);
        } else {
            //若y不为0，且y可以整除
            if(!(y==0)&&x%y==0){
                result=x/y;
                strRes.append(CNUMBERS[x]);
                strRes.append("/");
                strRes.append(CNUMBERS[y]);
            }//防除法出错，转加法
            else{
                result = x + y;
                strRes.append(CNUMBERS[x]);
                strRes.append("+");
                strRes.append(CNUMBERS[y]);
            }
        }
        //返回运算表达式和结果，用@符号分割
        strRes.append("=?@" + result);
        return strRes.toString();
    }
}
