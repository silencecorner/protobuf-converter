package net.badata.protobuf.converter.support;

import net.badata.protobuf.converter.exception.ConverterException;
import net.badata.protobuf.converter.exception.MappingException;
import net.badata.protobuf.converter.exception.TypeRelationException;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

public class PropertyNameCapturingInterceptor {

    @RuntimeType
    public static void intercept(@This PropertyNameCapturer capturer, @Origin Method method) {
        capturer.setPropertyName(getPropertyName(method));
    }

    private static String getPropertyName(Method method) {
        final boolean hasGetterSignature = method.getParameterTypes().length == 0
                && method.getReturnType() != null;

        String name = method.getName();
        String propName = null;

        if (hasGetterSignature) {
            if (name.startsWith("get") && hasGetterSignature) {
                propName = name.substring(3, 4).toLowerCase() + name.substring(4);
            } else if (name.startsWith("is") && hasGetterSignature) {
                propName = name.substring(2, 3).toLowerCase() + name.substring(3);
            }
        } else {
            throw new RuntimeException("Only property getter methods are expected to be passed");
        }

        return propName;
    }
}
