package net.badata.protobuf.converter.resolver;

import net.badata.protobuf.converter.Configuration;

import java.lang.reflect.Field;

/**
 * Factory interface for creating field resolvers.
 *
 * @author jsjem
 * @author Roman Gushel
 */
public interface FieldResolverFactory {

	/**
	 * Create field resolver.
	 * @param config 配置信息
	 * @param field Domain class field.
	 * @return instance of {@link net.badata.protobuf.converter.resolver.FieldResolver FieldResolver}
	 */
	FieldResolver createResolver(final Configuration config, final Field field);
}
