package net.badata.protobuf.converter;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.support.PropertyNameCapturer;
import net.badata.protobuf.converter.support.PropertyNameCapturingInterceptor;
import net.badata.protobuf.converter.support.SFunction;
import net.badata.protobuf.converter.utils.FieldUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.Arrays.asList;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Stores fields and classes that have to be ignored during conversion.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class FieldsIgnore{

    private final Map<Class<?>, Set<String>> ignoreMapping = new HashMap<Class<?>, Set<String>>();
    public static  <T>  FieldsIgnore lambda(final Class<T> ignoredClass, SFunction<T, ?>... sFunctions) {
		T capturer = getPropertyNameCapturer(ignoredClass);
		FieldsIgnore fieldsIgnore = new FieldsIgnore();
		if (sFunctions != null && sFunctions.length > 0){
			for (int index = 0; index < sFunctions.length; index ++){
				if (!sFunctions[index].getClass().isSynthetic()){
					throw new RuntimeException("不能使用实现类，只能使用lambda式");
				}
				sFunctions[index].apply( capturer );
				String propertyName = ( (PropertyNameCapturer) capturer ).getPropertyName();
				fieldsIgnore.add(ignoredClass, propertyName);
			}
		}else{
			fieldsIgnore.add(ignoredClass);
		}
		return fieldsIgnore;
    }


	public <T> FieldsIgnore add(final Class<T> ignoredClass, SFunction<T, ?>... sFunctions) {
		T capturer = getPropertyNameCapturer(ignoredClass);
		if (sFunctions != null && sFunctions.length > 0){
			for (int index = 0; index < sFunctions.length; index ++){
				if (!sFunctions[index].getClass().isSynthetic()){
					throw new RuntimeException("不能使用实现类，只能使用lambda式");
				}
				sFunctions[index].apply( capturer );
				String propertyName = ( (PropertyNameCapturer) capturer ).getPropertyName();
				this.add(ignoredClass, propertyName);
			}
		}else{
			this.add(ignoredClass);
		}
		return this;
	}

    /**
     * Add class to ignore map. Method {@link #ignored(ProtoClass,Field) ignored()} return true if field type is similar to
     * ignoreClass or field owner class is ignoreClass.
     *
     * @param ignoredClass Class for ignore.
     * @return Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     */
    public FieldsIgnore add(final Class<?> ignoredClass) {
        ignoreMapping.put(ignoredClass, Collections.<String>emptySet());
        return this;
    }

    /**
     * Add class field to ignore map.
     *
     * @param ignoredClass Owner of the ignored field.
     * @param fields       Fields for ignore.
     * @return Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     */
    public FieldsIgnore add(final Class<?> ignoredClass, final String... fields) {
        if (fields != null) {
            Set<String> ignoredFields = ignoreMapping.get(ignoredClass);
            if (ignoredFields == null || ignoredFields.isEmpty()) {
                ignoredFields = new HashSet<String>();
                ignoreMapping.put(ignoredClass, ignoredFields);
            }
            ignoredFields.addAll(asList(fields));
        }
        return this;
    }


    /**
     * Add all fields from another {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore} instance.
     *
     * @param ignoredFields Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     * @return Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     */
    public FieldsIgnore addAll(final FieldsIgnore ignoredFields) {
        ignoreMapping.putAll(ignoredFields.ignoreMapping);
        return this;
    }

    /**
     * Remove class field from ignore map.
     *
     * @param ignoredClass Owner of the ignored field.
     * @param fields       Fields for ignore.
     * @return Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     */
    public FieldsIgnore remove(final Class<?> ignoredClass, final String... fields) {
        if (fields != null) {
            Set<String> ignoredFields = ignoreMapping.get(ignoredClass);
            if (ignoredFields != null && !ignoredFields.isEmpty()) {
                ignoredFields.removeAll(asList(fields));
                if (ignoredFields.isEmpty()) {
                    ignoreMapping.remove(ignoredClass);
                }
            }
        }
        return this;
    }

    /**
     * Remove class from ignore map. Class and all its fields will not be ignored any more.
     *
     * @param ignoredClass Class to remove from ignore map.
     * @return Instance of {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore}.
     */
    public FieldsIgnore remove(final Class<?> ignoredClass) {
        ignoreMapping.remove(ignoredClass);
        return this;
    }

    /**
     * Clear ignore map.
     */
    public void clear() {
        ignoreMapping.clear();
    }

    /**
     * Check whether field has to be ignored.
     *
     * @param field Field instance for test.
     * @return true if field class or field name in the ignore or field is not annotated by
     * {@link net.badata.protobuf.converter.annotation.ProtoField ProtoField}.
     */
    protected boolean ignored(ProtoClass protoClass, final Field field) {
        return (!field.isAnnotationPresent(ProtoField.class) && protoClass.protoFieldAnnotationRequired()) || isClassIgnored(field) || isFieldIgnored(field);
    }

    protected boolean ignored(final Field field) {
        return !field.isAnnotationPresent(ProtoField.class) || isClassIgnored(field) || isFieldIgnored(field);
    }
    private boolean isClassIgnored(final Field field) {
        Class<?> verifiedClass = FieldUtils.isCollectionType(field) ? FieldUtils.extractCollectionType(field) :
                field.getType();
        Set<String> ignoredFields = ignoreMapping.get(verifiedClass);
        return ignoredFields != null && ignoredFields.isEmpty();
    }

    private boolean isFieldIgnored(final Field field) {
        Set<String> ignoredFields = ignoreMapping.get(field.getDeclaringClass());
        if (ignoredFields != null) {
            ProtoField annotation = field.getAnnotation(ProtoField.class);
            if (annotation != null) {
                return ignoredFields.isEmpty() || ignoredFields.contains(field.getName())
                        || ignoredFields.contains(annotation.name());
            }else{
                return ignoredFields.isEmpty() || ignoredFields.contains(field.getName());
            }
        }
        return false;
    }


    /**
     * Create copy of the instance.
     *
     * @return new instance of the {@link net.badata.protobuf.converter.FieldsIgnore FieldsIgnore} that is identical
     * with this instance.
     */
    public FieldsIgnore copy() {
        FieldsIgnore copy = new FieldsIgnore();
        copy.ignoreMapping.putAll(ignoreMapping);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        FieldsIgnore that = (FieldsIgnore) obj;

        if (!ignoreMapping.equals(that.ignoreMapping)) {
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * ignoreMapping.hashCode() + result;
        return result;
    }

    private static  <T>  T getPropertyNameCapturer(Class<T> type) {
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(type.isInterface() ? Object.class : type);

        if (type.isInterface()) {
            builder = builder.implement(type);
        }

        Class<?> proxyType = builder
                .implement(PropertyNameCapturer.class)
                .defineField("propertyName", String.class, Visibility.PRIVATE)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(PropertyNameCapturingInterceptor.class))
                .method(named("setPropertyName").or(named("getPropertyName")))
                .intercept(FieldAccessor.ofBeanProperty())
                .make()
                .load(
                        PropertyNameCapturer.class.getClassLoader(),
                        ClassLoadingStrategy.Default.WRAPPER
                )
                .getLoaded();

        try {
            @SuppressWarnings("unchecked")
            Class<T> typed = (Class<T>) proxyType;
            return typed.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Couldn't instantiate proxy for method name retrieval", e
            );
        }
    }
}
