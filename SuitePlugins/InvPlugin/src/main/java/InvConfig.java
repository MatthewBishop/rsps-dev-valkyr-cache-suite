import com.google.common.collect.Maps;
import javafx.util.Pair;
import com.displee.io.impl.InputBuffer;
import com.displee.io.impl.OutputBuffer;
import store.plugin.extension.ConfigExtensionBase;
import store.utilities.ReflectionUtils;
import suite.annotation.OrderType;

import java.lang.reflect.Field;
import java.util.Map;

public class InvConfig extends ConfigExtensionBase {

    private static Map<Field, Integer> fieldPriorities;

    @OrderType(priority = 1)
    public int size;

    @Override
    public void decode(int opcode, InputBuffer buffer) {
        if (opcode == 2)
        {
            size = buffer.readUnsignedShort();
        }
    }

    @Override
    public OutputBuffer encode(OutputBuffer buffer) {
        buffer.writeByte(2);
        buffer.writeShort(size);
        return buffer;
    }

    @Override
    public String toString() {
        return "" + id;
    }

    @Override
    public Map<Field, Integer> getPriority() {
        if (fieldPriorities != null)
            return fieldPriorities;
        Map<String, Pair<Field, Object>> values = ReflectionUtils.getValues(this);
        fieldPriorities = Maps.newHashMap();
        values.values().stream().forEach(pair -> {
            Field field = pair.getKey();
            int priority = field.isAnnotationPresent(OrderType.class) ? field.getAnnotation(OrderType.class).priority() : 1000;
            fieldPriorities.put(field, priority);
        });
        return fieldPriorities;
    }
}