/**
 * @link http://rdbcache.com/
 * @copyright Copyright (c) 2017-2018 Sam Wen
 * @license http://rdbcache.com/license/
 */

package com.doitincloud.commons;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Character.toLowerCase;

public class Utils2 {

    private static PasswordEncoder pencoder;

    public static PasswordEncoder passwordEncoder() {
        if (pencoder == null) {
            pencoder = new BCryptPasswordEncoder();
        }
        return pencoder;
    }

    public static String encodePassword(String plainText) {
        return passwordEncoder().encode(plainText);
    }

    public static String generateNumericCode(int n) {
        return RandomStringUtils.random(n, "0123456789");
    }

    public static void sendTextMessage(String name, String to, String message) {
        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("/tmp/text-message.txt", false);
            out = new BufferedWriter(fstream);
            out.write("to: " + to + "\n");
            out.write("name: " + name + "\n");
            out.write("message: " + message + "\n");
        }
        catch (IOException e) { e.printStackTrace(); }
        finally { if(out != null) try { out.close(); } catch(IOException e) { e.printStackTrace(); } }
    }

    public static void sendTextCode(String name, String to, String code) {
        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("/tmp/text-code.txt", false);
            out = new BufferedWriter(fstream);
            out.write("to: " + to + "\n");
            out.write("name: " + name + "\n");
            out.write("code: " + code + "\n");
        }
        catch (IOException e) { e.printStackTrace(); }
        finally { if(out != null) try { out.close(); } catch(IOException e) { e.printStackTrace(); } }
    }

    public static  String toUnderscoreSeparatedName(String name) {
        char[] chars = name.toCharArray();
        StringBuffer sb = new StringBuffer();
        sb.append(toLowerCase(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                sb.append('_');
                sb.append(toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
