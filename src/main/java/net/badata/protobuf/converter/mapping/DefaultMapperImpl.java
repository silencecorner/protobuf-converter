package net.badata.protobuf.converter.mapping;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;
import com.google.protobuf.*;
import javafx.util.Pair;
import net.badata.protobuf.converter.exception.MappingException;
import net.badata.protobuf.converter.naming.NamingStrategy;
import net.badata.protobuf.converter.resolver.FieldResolver;
import net.badata.protobuf.converter.type.*;
import net.badata.protobuf.converter.utils.FieldUtils;

import java.lang.Enum;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Implementation of {@link net.badata.protobuf.converter.mapping.Mapper Mapper} that is applied by default.
 * This implementation maps fields values directly from domain instance to related protobuf instance and vice versa.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class DefaultMapperImpl implements Mapper {
	public static ConcurrentMap<Pair<?, ?>, TypeConverter> TYPE_CONVERTER_CACHE = new ConcurrentHashMap<Pair<?, ?>, TypeConverter>() {{
		put(new Pair<>(Date.class, Long.class), new DateLongConverterImpl());
		put(new Pair<>(Enum.class, String.class), new EnumStringConverter());
		put(new Pair<>(LocalDateTime.class, Timestamp.class), new LocalDateTimeConverterImpl());
		put(new Pair<>(Set.class, List.class), new SetListConverterImpl());
		put(new Pair<>(Boolean.class, BoolValue.class), new BooleanBoolValueConverterImpl());
		put(new Pair<>(Double.class, DoubleValue.class), new DoubleDoubleValueConverterImpl());
		put(new Pair<>(Float.class, FloatValue.class), new FloatFloatValueConverterImpl());
		put(new Pair<>(Integer.class, Int32Value.class), new IntegerInt32ValueConverterImpl());
		put(new Pair<>(Integer.class, UInt32Value.class), new IntegerUInt32ValueConverterImpl());
		put(new Pair<>(Long.class, Int64Value.class), new LongInt64ValueConverterImpl());
		put(new Pair<>(Long.class, UInt64Value.class), new LongUInt64ValueConverterImpl());
		put(new Pair<>(String.class, StringValue.class), new StringStringValueConverterImpl());
	}};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Message> MappingResult mapToDomainField(final FieldResolver fieldResolver, final T protobuf,
			final Object domain) throws MappingException {
		Object protobufFieldValue = getFieldValue(FieldUtils.createProtobufGetterName(fieldResolver), protobuf);
		if (FieldUtils.isComplexType(fieldResolver.getField())) {
			boolean hasFieldValue = true;
			try {
				String hasserName = FieldUtils.createProtobufHasserName(fieldResolver);
				if (hasserName != null) {
					hasFieldValue = hasFieldValue(hasserName, protobuf);
				}
			} catch (MappingException ignored) {} // not `has` method, continue
			if (hasFieldValue) {
				return new MappingResult(MappingResult.Result.NESTED_MAPPING, protobufFieldValue, domain);
			}
			return new MappingResult(MappingResult.Result.MAPPED, null, domain);
		}
		if (FieldUtils.isCollectionType(fieldResolver.getField())) {
			return new MappingResult(MappingResult.Result.COLLECTION_MAPPING, protobufFieldValue, domain);
		}
		TypeConverter<?, ?> typeConverter = TYPE_CONVERTER_CACHE.get(new Pair<>(Primitives.wrap(fieldResolver.getField().getType()),
				Primitives.wrap(protobufFieldValue.getClass())));
		if (typeConverter != null) {
			fieldResolver.setTypeConverter(typeConverter);
		}
		return new MappingResult(MappingResult.Result.MAPPED, protobufFieldValue, domain);
	}
	private boolean hasFieldValue(final String hasserName, final Object source) throws MappingException {
		Class<?> sourceClass = source.getClass();
		try {
			return (boolean) sourceClass.getMethod(hasserName).invoke(source);
		} catch (IllegalAccessException e) {
			throw new MappingException(
					String.format("Access denied. '%s.%s()'", sourceClass.getName(), hasserName));
		} catch (InvocationTargetException e) {
			throw new MappingException(
					String.format("Can't decide if field has value through '%s.%s()'", sourceClass.getName(), hasserName));
		} catch (NoSuchMethodException e) {
			throw new MappingException(
					String.format("Hasser not found. '%s.%s()'", sourceClass.getName(), hasserName));
		}
	}

	private Object getFieldValue(final String getterName, final Object source) throws MappingException {
		Class<?> sourceClass = source.getClass();
		try {
			return sourceClass.getMethod(getterName).invoke(source);
		} catch (IllegalAccessException e) {
			throw new MappingException(
					String.format("Access denied. '%s.%s()'", sourceClass.getName(), getterName));
		} catch (InvocationTargetException e) {
			throw new MappingException(
					String.format("Can't set field value through '%s.%s()'", sourceClass.getName(), getterName));
		} catch (NoSuchMethodException e) {
			throw new MappingException(
					String.format("Getter not found. '%s.%s()'", sourceClass.getName(), getterName));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Message.Builder> MappingResult mapToProtobufField(final FieldResolver fieldResolver, final
	Object domain, final T
			protobufBuilder) throws MappingException {
		Descriptors.FieldDescriptor fieldDescriptor;
		if ( (fieldDescriptor = findFieldByName(protobufBuilder,fieldResolver)) != null){
			Object domainFieldValue = getFieldValue(FieldUtils.createDomainGetterName(fieldResolver), domain);
			if (FieldUtils.isComplexType(fieldResolver.getField())) {
				return new MappingResult(MappingResult.Result.NESTED_MAPPING, domainFieldValue, protobufBuilder);
			}
			if (FieldUtils.isCollectionType(fieldResolver.getField())) {
				return new MappingResult(MappingResult.Result.COLLECTION_MAPPING, domainFieldValue, protobufBuilder);
			}
			Object obj = protobufBuilder.getField(fieldDescriptor);
			TypeConverter<?, ?> typeConverter = TYPE_CONVERTER_CACHE.get(new Pair<>(Primitives.wrap(fieldResolver.getField().getType()),
					Primitives.wrap(obj.getClass())));
			if (typeConverter != null) {
				fieldResolver.setTypeConverter(typeConverter);
			}
			return new MappingResult(MappingResult.Result.MAPPED, domainFieldValue, protobufBuilder);
		}
		return null;
	}

	private static Descriptors.FieldDescriptor findFieldByName(Message.Builder builder, FieldResolver fieldResolver) {
		return builder.getDescriptorForType().findFieldByName(translate(fieldResolver.getNamingStrategy(),fieldResolver.getProtobufName()));
	}

	private static String translate(NamingStrategy namingStrategy, String fieldName){
		switch (namingStrategy){
			case NO_OP:
				return fieldName;
			case UNDERSCORE_SEPARATED_CASE:
				return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
			default:
				return fieldName;
		}
	}
}
