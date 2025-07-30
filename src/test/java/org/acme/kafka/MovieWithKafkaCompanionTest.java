package org.acme.kafka;

import static io.apicurio.registry.serde.avro.AvroKafkaSerdeConfig.USE_SPECIFIC_AVRO_READER;
import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.acme.kafka.quarkus.Movie;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WithTestResource(value = KafkaCompanionResource.class)
public class MovieWithKafkaCompanionTest {

    @InjectKafkaCompanion
    KafkaCompanion kafkaCompanion;

    @Test
    public void consumeWithCompanion() {
        kafkaCompanion.getCommonClientConfig().put(USE_SPECIFIC_AVRO_READER, "true");
        Serde<Movie> avroSerde = Serdes.serdeFrom(new AvroKafkaSerializer<>(), new AvroKafkaDeserializer<>());
        avroSerde.configure(kafkaCompanion.getCommonClientConfig(), false);
        kafkaCompanion.registerSerde(Movie.class, avroSerde);


        given().contentType(ContentType.JSON)
                .body("{\"title\":\"The Shawshank Redemption\",\"year\":1994}")
                .when()
                .post("/movies")
                .then()
                .statusCode(202);

        given().contentType(ContentType.JSON)
                .body("{\"title\":\"12 Angry Men\",\"year\":1957}")
                .when()
                .post("/movies")
                .then()
                .statusCode(202);

        ConsumerTask<String, Movie> consumer = kafkaCompanion.consume(Movie.class)
                .withGroupId("test-group-" + UUID.randomUUID())
                .withOffsetReset(OffsetResetStrategy.EARLIEST)
                .fromTopics("movies");

        List<ConsumerRecord<String, Movie>> received = consumer.awaitRecords(2, Duration.ofSeconds(5L)).getRecords();
        // check if, after at most 5 seconds, we have at least 2 items collected, and they are what we expect
        await().atMost(5, SECONDS).until(() -> received.size() >= 2);
        List<String> movies = received.stream().map(r -> r.value().getTitle()).toList();
        assertThat(movies, Matchers.hasItems("The Shawshank Redemption", "12 Angry Men"));
    }


}
