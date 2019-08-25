package net.badata.protobuf.converter.type;


import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class FloatFloatValueConverterImpl implements TypeConverter<Float, FloatValue> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Float toDomainValue(final Object instance) {
		return ((FloatValue)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FloatValue toProtobufValue(final Object instance) {
		return FloatValue.of((float) instance);
	}
}
