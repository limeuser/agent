package cn.oasistech.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import cn.oasistech.util.Logger;

public class Shell {
    private static final Logger logger = new Logger().addPrinter(System.out);
    
    public static final String run(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            StringBuilder str = new StringBuilder();
            while (line != null) {
                str.append(line).append("\r\n");
                line = reader.readLine();
            }
            return str.toString();
        } catch (Exception e) {
            logger.log("shell run cmd exception:", e);
            return "";
        }
    }
}
