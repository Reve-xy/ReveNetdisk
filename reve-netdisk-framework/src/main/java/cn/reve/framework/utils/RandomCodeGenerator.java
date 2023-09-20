package cn.reve.framework.utils;

import java.util.Random;

/**
 * 简要描述
 * 生成随机验证码
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/24 17:19
 */
public class RandomCodeGenerator {

    public static String generateRandomCode(int length) {
        String codeList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(codeList.length());
            char codeChar = codeList.charAt(index);
            sb.append(codeChar);
        }
        return sb.toString();
    }

    public static String getNumberId(int length) {
        StringBuilder buf = new StringBuilder();
        Random random = new Random();
        /*开头不为0,建议数据量较少时只放开部分，比如1至3开头的数，等业务达到一定数量时，再逐步放开剩余的号码段，由于是固定位数，总数量一定，生成的数越多，重复的几率越大**/
        int firstNumber = random.nextInt(9) + 1;
        buf.append(firstNumber);
        for(int i = 0; i < length - 1; ++i) {
            buf.append(random.nextInt(10));
        }

        return buf.toString();
    }


}
