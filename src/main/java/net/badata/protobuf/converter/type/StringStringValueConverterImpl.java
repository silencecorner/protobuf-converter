package net.badata.protobuf.converter.type;


import com.google.protobuf.StringValue;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class StringStringValueConverterImpl implements TypeConverter<String, StringValue> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toDomainValue(final Object instance) {
		return ((StringValue)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StringValue toProtobufValue(final Object instance) {
		return StringValue.of((String) instance);
	}
}
