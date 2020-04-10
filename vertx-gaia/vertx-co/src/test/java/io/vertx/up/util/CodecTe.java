package io.vertx.up.util;

import io.vertx.ext.unit.TestContext;
import io.vertx.quiz.StoreBase;
import io.vertx.up.eon.Values;
import org.junit.Test;
import io.vertx.up.util.CodecTc;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Author: chunmei deng
 * Modified: 2020/03/22
 */

public class CodecTe extends StoreBase {

    final static String input="  哈哈&%¥!~@#$^*()-=+_[]{};:'\"/?.>,<`……|  ～0【】「」《》，。？、";
    @Test
    public void md5(final TestContext context) {
        context.assertEquals(CodecTc.MD5EncodeUtf8(input), Codec.md5(input));
    }

    @Test
    public void sha256(final TestContext context) throws UnsupportedEncodingException {
        context.assertEquals(CodecTc.sha256code(input),Codec.sha256(input));
    }

    @Test
    public void sha512(final TestContext context) throws UnsupportedEncodingException {
        context.assertEquals(CodecTc.sha512code(input),Codec.sha512(input));
    }

    @Test
    public void base64(final TestContext context) throws Exception {

        context.assertEquals(encodeBase64(input.getBytes(Values.DEFAULT_CHARSET)), Codec.base64(input, true));

        String strOut=Codec.base64(input, true);
        context.assertEquals(new String(decodeBase64(strOut)),Codec.base64(strOut,false));
    }

    public static String encodeBase64(byte[] inputx) throws Exception {
        Class clazz = Class
                .forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
        Method mainMethod = clazz.getMethod("encode", byte[].class);
        mainMethod.setAccessible(true);
        Object retObj = mainMethod.invoke(null, new Object[] { inputx });
        return (String) retObj;
    }

    public static byte[] decodeBase64(String inputx) throws Exception {
        Class clazz = Class
                .forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");
        Method mainMethod = clazz.getMethod("decode", String.class);
        mainMethod.setAccessible(true);
        Object retObj = mainMethod.invoke(null, inputx);
        return (byte[]) retObj;
    }

    @Test
    public void url(final TestContext context) throws UnsupportedEncodingException {
        String StrOut = Codec.url(input, true);
        context.assertEquals(StrOut, CodecTc.urlcode(input, false));
    }




}