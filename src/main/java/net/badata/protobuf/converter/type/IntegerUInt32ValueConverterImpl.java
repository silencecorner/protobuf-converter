package net.badata.protobuf.converter.type;


import com.google.protobuf.Int32Value;
import com.google.protobuf.UInt32Value;

import java.util.Date;

/**
 * Converts domain {@link Date Date} field value to protobuf {@link Long Long} field value.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public class IntegerUInt32ValueConverterImpl implements TypeConverter<Integer, UInt32Value> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer toDomainValue(final Object instance) {
		return ((Int32Value)instance).getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UInt32Value toProtobufValue(final Object instance) {
		return UInt32Value.of((int) instance);
	}
}
