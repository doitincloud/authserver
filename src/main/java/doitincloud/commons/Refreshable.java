package doitincloud.commons;

import java.util.Map;
import java.util.concurrent.Callable;

public abstract class Refreshable implements Callable<Map<String, Object>> {

    public Refreshable clone() throws CloneNotSupportedException {
        return (Refreshable) super.clone();
    }
}
