package net.badata.protobuf.converter.type;


import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class DoubleDoubleValueConverterImpl implements TypeConverter<Double, DoubleValue> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double toDomainValue(final Object instance) {
		return ((DoubleValue)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DoubleValue toProtobufValue(final Object instance) {
		return DoubleValue.of((double) instance);
	}
}
