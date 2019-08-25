package net.badata.protobuf.converter.type;


import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class LongUInt64ValueConverterImpl implements TypeConverter<Long, UInt64Value> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long toDomainValue(final Object instance) {
		return ((UInt64Value)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UInt64Value toProtobufValue(final Object instance) {
		return UInt64Value.of((long) instance);
	}
}
