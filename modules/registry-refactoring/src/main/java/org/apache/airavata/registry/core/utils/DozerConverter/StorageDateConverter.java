package org.apache.airavata.registry.core.utils.DozerConverter;

import org.dozer.DozerConverter;

import java.sql.Timestamp;

/**
 * Created by skariyat on 4/11/18.
 */
public class StorageDateConverter extends DozerConverter {

    public StorageDateConverter(Class prototypeA, Class prototypeB) {
        super(prototypeA, prototypeB);
    }

    @Override
    public Object convertTo(Object source, Object dest) {

        if (source != null) {
            if (source instanceof Long) {
                return new Timestamp((long) source);
            } else if (source instanceof Timestamp) {
                return ((Timestamp)source).getTime();
            }
        }
        return null;
    }

    @Override
    public Object convertFrom(Object source, Object dest) {
        return convertTo(source, dest);
    }

}
