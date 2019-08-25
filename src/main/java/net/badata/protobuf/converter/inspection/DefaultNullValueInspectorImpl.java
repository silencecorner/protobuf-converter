package net.badata.protobuf.converter.inspection;

import com.google.common.primitives.Primitives;
import com.google.protobuf.*;

/**
 * Implementation of {@link net.badata.protobuf.converter.inspection.NullValueInspector NullValueInspector} that is
 * applied by default.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public final class DefaultNullValueInspectorImpl implements NullValueInspector {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isNull(final Object value) {
		if (value == null){
			return true;
		}
		Class clazz = Primitives.wrap(value.getClass());
		if (Byte.class.isAssignableFrom(clazz)) {
			return (Byte) value == 0;
		}
		if (Short.class.isAssignableFrom(clazz)) {
			return (Short) value == 0;
		}
		if (Integer.class.isAssignableFrom(clazz)) {
			return (Integer) value == 0;
		}
		if (Long.class.isAssignableFrom(clazz)) {
			return (Long) value == 0L;
		}
		if (Float.class.isAssignableFrom(clazz)) {
			return (Float) value == 0.0f;
		}
		if (Double.class.isAssignableFrom(clazz)) {
			return (Double) value == 0.0;
		}
		if (String.class.isAssignableFrom(clazz)) {
			return ((String) value).isEmpty();
		}
		if (value instanceof Timestamp){
			return Timestamp.getDefaultInstance().equals(value);
		}
		if (value instanceof Duration){
			return Duration.getDefaultInstance().equals(value);
		}
		if (value instanceof BytesValue) {
			return BytesValue.getDefaultInstance().equals(value);
		}
		if (value instanceof Int32Value) {
			return Int32Value.getDefaultInstance().equals(value);
		}
		if (value instanceof Int64Value) {
			return Int64Value.getDefaultInstance().equals(value);
		}
		if (value instanceof UInt32Value) {
			return UInt32Value.getDefaultInstance().equals(value);
		}
		if (value instanceof UInt64Value) {
			return UInt64Value.getDefaultInstance().equals(value);
		}
		if (value instanceof FloatValue) {
			return FloatValue.getDefaultInstance().equals(value);
		}
		if (value instanceof DoubleValue) {
			return DoubleValue.getDefaultInstance().equals(value);
		}
		if (value instanceof StringValue) {
			return StringValue.getDefaultInstance().equals(value);
		}
		if (value instanceof BoolValue) {
			return BoolValue.getDefaultInstance().equals(value);
		}
		return false;
	}
}
