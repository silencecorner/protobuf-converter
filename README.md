[![Build Status](https://api.travis-ci.org/BAData/protobuf-converter.svg)](https://travis-ci.org/BAData/protobuf-converter)
[![GitHub release](https://img.shields.io/github/release/BAData/protobuf-converter.svg)](https://github.com/BAData/protobuf-converter/releases)
[![JitPack repository](https://jitpack.io/v/BAData/protobuf-converter.svg)](https://jitpack.io/#BAData/protobuf-converter)
# Protobuf converter
**protobuf-converter** is library for transforming your Domain Model Objects into Google Protobuf Messages and vice versa.

## Maven/Gradle

You can add this library as a dependency to your Maven or Gradle project through [JitPack](https://jitpack.io/#BAData/protobuf-converter).

## How to use it ?
Domain model classes that have to be transformed into protobuf messages must satisfy conditions:

* Class has to be marked by *@ProtoClass* annotaion that contains reference on related protobuf message class.
* Class fields has to be marked by *@ProtoField* annotaion. These fields must have getters and setters.

E.g.:
```java
@ProtoClass(ProtobufUser.class)
public class User {

	@ProtoField
	private String name;
	@ProtoField
	private String password;

	// getters and setters for 'name' and 'password' fields
	...
}
```
Code for conversion User instance into related protobuf message:
```java
User userDomain = new User();
...
ProtobufUser userProto = Converter.create().toProtobuf(ProtobufUser.class, userDomain);
```
Code for backward conversion:
```java
User userDomain = Converter.create().toDomain(User.class, userProto);
```
Conversion of lists of objects is similar to single object conversion.

### @ProtoClass
Annotation attibutes:

* *value* - required. Contains reference to protobuf message class.
* *mapper* - optional. Contains reference to class that performs field value mapping between domain and protobuf objects during conversion. Default value - **DefaultMapperImpl.class**

*mapper* attribute is useful when domain model class and protobuf message class has different structure. In such case you have to create own **Mapper** implementation and specify it as value of *mapper* attribute.

If type of domain class field is complex (marked by **@ProtoClass**) protobuf-converter will convert this field value to related protobuf message (**DefaultMapperImpl.class** has to be specified as mapper attribute value)

### @ProtoField
Annotation attibutes:

1. *name* - optional. Related protobuf field name.
2. *converter* - optional. Contains reference to class that performs field value transformation. Default value - **DefaultConverterImpl.class**
3. *nullValue* - optional. Contains reference to class that test protobuf object field for nullability . Default value - **DefaultNullValueInspectorImpl.class**
4. *defaultValue* - optional. Contains reference to class that initialize domain object field if related protobuf field is not initialized. Default value - **SimpleDefaultValueImpl.class** 

Use *name* attribute when domain class field name is different from name specified in the *.proto file.

*converter* attribute is useful when domain class field type is different from protobuf message field type. E.g.: date value in the domain class is stored into **java.util.Date** and protobuf message uses int64(**java.lang.Long**). For field data tarnsformation you must specify **DateLongConverterImpl.class** as value of *converter* attribute. You can create own field type converter if you implement **TypeConverter** interface.

### Fields Ignore

In case when you need to prevent transforming some domain fields values into protobuf message field values use **FieldsIgnore.class**.

E.g.: field *password* of **User.class** has not be tansformed into **ProtobufUser.class** field when server generates response on user info request sent by client application. You need to add field *password* into ignored list:
```java
User userDomain = new User();
...
FieldsIgnore ignoredFiedls = new FieldsIgnore().add(User.class, "password");
Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFiedls).build();
ProtobufUser userProto = Converter.create(configuration).toProtobuf(ProtobufUser.class, userDomain);
```
Also **FieldsIgnore.class** allows to ignore single field as well as field data types. E.g.: it is possible to ignore all fields with type **java.lang.String**:
```java
User userDomain = new User();
...
FieldsIgnore ignoredFiedls = new FieldsIgnore().add(String.class);
Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFiedls).build();
ProtobufUser userProto = Converter.create(configuration).toProtobuf(ProtobufUser.class, userDomain);
```
### 新功能
- @ProtoField 可不填
- lambda添加ignore字段
- @ProtoField 单独设置proto的命名策略，这里跟java的定义有关系，通过fieldName去查找proto的字段是否存在，默认java跟proto一样的都是驼峰，例如：
> - java `fieldName` proto `fieldName` 匹配 
>  - java `fieldName` proto `field_name` @ProtoField(namingStrategy=UNDERSCORE_SEPARATED_CASE) 匹配 其他都不匹配，java里面非驼峰不能使用，因为proto生成java代码会变成驼峰
- java存在proto字段不存在时不抛异常
- 内置常见转换，具体请查看[转换关系](https://github.com/silencecorner/protobuf-converter/tree/master/src/main/java/net/badata/protobuf/converter/type)
```
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
```
### 注意事项
java的`Boolean`字段不要以`is`开头，无法找到get方法，[测试代码](https://github.com/silencecorner/protobuf-converter/blob/master/src/test/java/net/badata/protobuf/converter/DefaultMapperTest.java#L267-L271)

### Obfuscation
Main Proguard options:
```
-keep interface net.badata.protobuf.converter.** { *; }

-keep public class * implements net.badata.protobuf.converter.mapping.Mapper {
     public <init>();
}

-keep public class * implements net.badata.protobuf.converter.type.TypeConverter {
     public <init>();
}

-keep public class * implements net.badata.protobuf.converter.inspection.DefaultValue {
     public <init>();
}

-keep public class * implements net.badata.protobuf.converter.inspection.NullValueInspector {
     public <init>();
}
```

Keep your domain objects (replace **your.package.name** with name of package where domain objects stored):
```
-keepclassmembers @net.badata.protobuf.converter.annotation.ProtoClass public class your.package.name.** {
     @net.badata.protobuf.converter.annotation.ProtoField <fields>;
     public <init>();
     public void set*(***);
     public boolean is*();
     public  *** get*();
}
```

### Example
**protobuf-converter** capabilities is demonstrated by project located in the _example_ folder.

Assemble example project:
```
gradle assemble
```

Start server:
```
java -jar example.jar "server"
```

Start client:
```
java -jar example.jar
```

# License

MIT License
