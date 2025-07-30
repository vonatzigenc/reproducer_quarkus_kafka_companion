Reproducer based of Quarkus quickstart [kafka-avro-schema-quickstart](https://github.com/quarkusio/quarkus-quickstarts/tree/main/kafka-avro-schema-quickstart)

 - Removed classes that aren't relevant for the reproducer.

Issue:
* With with Quarkus 3.25.0 the KafkaCompanions isn't initialized correctly.
* MovieWithKafkaCompanionTest fails with:
```
java.lang.IllegalArgumentException: Missing registry base url, set apicurio.registry.url

	at io.apicurio.registry.resolver.AbstractSchemaResolver.configure(AbstractSchemaResolver.java:75)
	at io.apicurio.registry.resolver.DefaultSchemaResolver.configure(DefaultSchemaResolver.java:62)
	at io.apicurio.registry.serde.SchemaResolverConfigurer.configure(SchemaResolverConfigurer.java:87)
	at io.apicurio.registry.serde.AbstractKafkaSerDe.configure(AbstractKafkaSerDe.java:70)
	at io.apicurio.registry.serde.avro.AvroKafkaSerializer.configure(AvroKafkaSerializer.java:91)
	at org.apache.kafka.common.serialization.Serdes$WrapperSerde.configure(Serdes.java:42)
	at org.acme.kafka.MovieWithKafkaCompanionTest.consumeWithCompanion(MovieWithKafkaCompanionTest.java:44)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at io.quarkus.test.junit.QuarkusTestExtension.runExtensionMethod(QuarkusTestExtension.java:1000)
	at io.quarkus.test.junit.QuarkusTestExtension.interceptTestMethod(QuarkusTestExtension.java:848)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
```
- With Quarkus 3.24.5 the test passes.
