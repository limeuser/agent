package cn.oasistech.util;

public class ClassUtil {
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public final static <T> T newInstance(String name) {
        try {
            Object o = Class.forName(name).newInstance();
            return ((T) o);
        } catch (Exception e) {
            logger.log("can't create class:%s", e, name);
            return null;
        }
    }
}
