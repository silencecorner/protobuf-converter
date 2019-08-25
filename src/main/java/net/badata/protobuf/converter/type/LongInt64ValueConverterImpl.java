package net.badata.protobuf.converter.type;


import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.UInt32Value;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class LongInt64ValueConverterImpl implements TypeConverter<Long, Int64Value> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long toDomainValue(final Object instance) {
		return ((Int64Value)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Int64Value toProtobufValue(final Object instance) {
		return Int64Value.of((long) instance);
	}
}
