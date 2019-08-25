package net.badata.protobuf.converter.type;


import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class BooleanBoolValueConverterImpl implements TypeConverter<Boolean, BoolValue> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean toDomainValue(final Object instance) {
		return ((BoolValue)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoolValue toProtobufValue(final Object instance) {
		return BoolValue.of((boolean) instance);
	}
}
