package com.blerpc;

import static com.blerpc.Assert.assertError;
import static com.google.common.truth.Truth.assertThat;

import com.blerpc.device.test.proto.TestMetadata;
import com.blerpc.device.test.proto.TestOptionsDoubleValueRequest;
import com.blerpc.device.test.proto.TestOptionsEqualsIndexesRequest;
import com.blerpc.device.test.proto.TestOptionsFloatValueRequest;
import com.blerpc.device.test.proto.TestOptionsImage;
import com.blerpc.device.test.proto.TestOptionsImageRequest;
import com.blerpc.device.test.proto.TestOptionsNegativeRangeRequest;
import com.blerpc.device.test.proto.TestOptionsNoBytesRangeRequest;
import com.blerpc.device.test.proto.TestOptionsRangeBiggerThanCountRequest;
import com.blerpc.device.test.proto.TestOptionsRangeIntersectRequest;
import com.blerpc.device.test.proto.TestOptionsSmallFourBytesEnumRangeRequest;
import com.blerpc.device.test.proto.TestOptionsSmallThreeBytesEnumRangeRequest;
import com.blerpc.device.test.proto.TestOptionsSmallTwoBytesEnumRangeRequest;
import com.blerpc.device.test.proto.TestOptionsStringValueRequest;
import com.blerpc.device.test.proto.TestOptionsWithFieldNoBytesRequest;
import com.blerpc.device.test.proto.TestOptionsWrongBooleanRangeRequest;
import com.blerpc.device.test.proto.TestOptionsWrongEnumRangeRequest;
import com.blerpc.device.test.proto.TestOptionsWrongIntegerRangeRequest;
import com.blerpc.device.test.proto.TestOptionsWrongLongRangeRequest;
import com.blerpc.device.test.proto.TestOptionsZeroBytesRequest;
import com.blerpc.device.test.proto.TestToken;
import com.blerpc.device.test.proto.TestType;
import java.nio.ByteOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link AnnotationMessageConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnnotationMessageConverterTest {

    private static final TestOptionsImage IMAGE_REQUEST_1 = TestOptionsImage.newBuilder()
            .setVersion(20)
            .setCrc(500)
            .setLength(100000)
            .setRelease(true)
            .setType(TestType.FULL)
            .setMetadata(TestMetadata.newBuilder()
                    .setBleMetadata(20000)
                    .setToken(TestToken.newBuilder()
                            .setToken(30000)))
            .setBuildTime(1519548579757L)
            .build();
    private static final TestOptionsImage IMAGE_REQUEST_2 = TestOptionsImage.newBuilder()
            .setVersion(35)
            .setCrc(800)
            .setLength(130500)
            .setRelease(false)
            .setType(TestType.ONLY_APP)
            .setMetadata(TestMetadata.newBuilder()
                    .setBleMetadata(40000)
                    .setToken(TestToken.newBuilder()
                            .setToken(50000)))
            .setBuildTime(1519576271989L)
            .build();
    private static final TestOptionsImage IMAGE_REQUEST_WRONG_BYTE_STRING = TestOptionsImage.newBuilder()
            .setMetadata(TestMetadata.newBuilder()
                    .setBleMetadata(20000)
                    .setToken(TestToken.newBuilder()
                            .setToken(20000)))
            .build();
    private static final byte[] EMPTY_ARRAY = new byte[0];
    private static final byte[] IMAGE_REQUEST_1_BYTES_LITTLE_ENDIAN =
            new byte[]{20, -12, 1, -96, -122, 1, 0, 1, 2, 0, 32, 78, 0, 0, 0, 48, 117, 0, 0, 0, -83, 63, 39, -52, 97, 1, 0, 0};
    private static final byte[] IMAGE_REQUEST_1_BYTES_BIG_ENDIAN =
            new byte[]{20, 1, -12, 0, 1, -122, -96, 1, 2, 0, 0, 0, 78, 32, 0, 0, 0, 117, 48, 0, 0, 0, 1, 97, -52, 39, 63, -83};
    private static final byte[] IMAGE_REQUEST_2_BYTES_LITTLE_ENDIAN =
            new byte[]{35, 32, 3, -60, -3, 1, 0, 0, 1, 0, 64, -100, 0, 0, 0, 80, -61, 0, 0, 0, 117, -52, -51, -51, 97, 1, 0, 0};
    private static final byte[] IMAGE_REQUEST_2_BYTES_BIG_ENDIAN =
            new byte[]{35, 3, 32, 0, 1, -3, -60, 0, 1, 0, 0, 0, -100, 64, 0, 0, 0, -61, 80, 0, 0, 0, 1, 97, -51, -51, -52, 117};
    private static final TestOptionsZeroBytesRequest ZERO_BYTES_REQUEST = TestOptionsZeroBytesRequest.getDefaultInstance();
    private static final TestOptionsWithFieldNoBytesRequest FIELD_NO_BYTES_REQUEST = TestOptionsWithFieldNoBytesRequest.getDefaultInstance();
    private static final TestOptionsNoBytesRangeRequest NO_BYTES_RANGE_REQUEST = TestOptionsNoBytesRangeRequest.newBuilder()
            .setValue(20)
            .build();
    private static final TestOptionsEqualsIndexesRequest EQUALS_INDEXES_REQUEST = TestOptionsEqualsIndexesRequest.newBuilder()
            .setMetadata(20000)
            .build();
    private static final TestOptionsNegativeRangeRequest NEGATIVE_RANGE_REQUEST = TestOptionsNegativeRangeRequest.newBuilder()
            .setMetadata(20000)
            .build();
    private static final TestOptionsStringValueRequest STRING_VALUE_REQUEST = TestOptionsStringValueRequest.newBuilder()
            .setMessage("Message")
            .build();
    private static final TestOptionsFloatValueRequest FLOAT_VALUE_REQUEST = TestOptionsFloatValueRequest.newBuilder()
            .setWeight(85.5f)
            .build();
    private static final TestOptionsDoubleValueRequest DOUBLE_VALUE_REQUEST = TestOptionsDoubleValueRequest.newBuilder()
            .setImpedance(300.5678d)
            .build();
    private static final TestOptionsRangeBiggerThanCountRequest RANGE_BIGGER_COUNT_REQUEST = TestOptionsRangeBiggerThanCountRequest.newBuilder()
            .setMetadata(20000)
            .build();
    private static final TestOptionsWrongIntegerRangeRequest WRONG_INT_RANGE_REQUEST = TestOptionsWrongIntegerRangeRequest.newBuilder()
            .setValue(20)
            .build();
    private static final TestOptionsWrongLongRangeRequest WRONG_LONG_RANGE_REQUEST = TestOptionsWrongLongRangeRequest.newBuilder()
            .setValue(100000)
            .build();
    private static final TestOptionsWrongEnumRangeRequest WRONG_ENUM_RANGE_REQUEST = TestOptionsWrongEnumRangeRequest.newBuilder()
            .setType(TestType.ONLY_APP)
            .build();
    private static final TestOptionsWrongBooleanRangeRequest WRONG_BOOLEAN_RANGE_REQUEST = TestOptionsWrongBooleanRangeRequest.newBuilder()
            .setRelease(true)
            .build();
    private static final TestOptionsRangeIntersectRequest RANGE_INTERSECT_REQUEST = TestOptionsRangeIntersectRequest.newBuilder()
            .setValue(20000)
            .setMetadata(30000)
            .build();
    private static final TestOptionsSmallTwoBytesEnumRangeRequest SMALL_TWO_BYTES_ENUM_RANGE_REQUEST =
            TestOptionsSmallTwoBytesEnumRangeRequest.newBuilder()
            .setBigEnumValue(2)
            .build();
    private static final TestOptionsSmallThreeBytesEnumRangeRequest SMALL_THREE_BYTES_ENUM_RANGE_REQUEST =
            TestOptionsSmallThreeBytesEnumRangeRequest.newBuilder()
                    .setBigEnumValue(2)
                    .build();
    private static final TestOptionsSmallFourBytesEnumRangeRequest SMALL_FOUR_BYTES_ENUM_RANGE_REQUEST =
            TestOptionsSmallFourBytesEnumRangeRequest.newBuilder()
                    .setBigEnumValue(2)
                    .build();

    AnnotationMessageConverter converter = new AnnotationMessageConverter();
    AnnotationMessageConverter converterLittleEndian = new AnnotationMessageConverter(ByteOrder.LITTLE_ENDIAN);

    @Test
    public void serializeRequestTest() throws Exception {
        assertThat(converterLittleEndian.serializeRequest(null, IMAGE_REQUEST_1)).isEqualTo(IMAGE_REQUEST_1_BYTES_LITTLE_ENDIAN);
        assertThat(converterLittleEndian.serializeRequest(null, IMAGE_REQUEST_2)).isEqualTo(IMAGE_REQUEST_2_BYTES_LITTLE_ENDIAN);
        assertThat(converter.serializeRequest(null, IMAGE_REQUEST_1)).isEqualTo(IMAGE_REQUEST_1_BYTES_BIG_ENDIAN);
        assertThat(converter.serializeRequest(null, IMAGE_REQUEST_2)).isEqualTo(IMAGE_REQUEST_2_BYTES_BIG_ENDIAN);
    }

    @Test
    public void serializeRequestTest_emptyMessage() throws Exception {
        assertThat(converter.serializeRequest(null, TestOptionsImageRequest.getDefaultInstance())).isEqualTo(EMPTY_ARRAY);
    }

    @Test
    public void serializeRequestTest_zeroBytesMessage() throws Exception {
        assertThat(converter.serializeRequest(null, ZERO_BYTES_REQUEST)).isEqualTo(EMPTY_ARRAY);
    }

    @Test
    public void serializeRequestTest_messageWithFieldAndWithoutByteSize() throws Exception {
        assertError(() -> converter.serializeRequest(null, FIELD_NO_BYTES_REQUEST),
                "A non empty message TestOptionsWithFieldNoBytesRequest doesn't have com.blerpc.message_size annotation.");
    }

    @Test
    public void serializeRequestTest_noByteRangeMessage() throws Exception {
        assertError(() -> converter.serializeRequest(null, NO_BYTES_RANGE_REQUEST),
                "Proto field value doesn't have com.blerpc.field_bytes annotation");
    }

    @Test
    public void serializeRequestTest_equalsIndexesMessage() throws Exception {
        assertError(() -> converter.serializeRequest(null, EQUALS_INDEXES_REQUEST),
                "Field metadata has from_bytes = 0 which must be less than to_bytes = 0");
    }

    @Test
    public void serializeRequestTest_negativeRangeMessage() throws Exception {
        assertError(() -> converter.serializeRequest(null, NEGATIVE_RANGE_REQUEST),
                "Field metadata has from_bytes = -1 which is less than zero");
    }

    @Test
    public void serializeRequestTest_rangeBiggerThanCountMessage() throws Exception {
        assertError(() -> converter.serializeRequest(null, RANGE_BIGGER_COUNT_REQUEST),
                "Field metadata has to_bytes = 11 which is bigger than message bytes size = 10");
    }

    @Test
    public void serializeRequestTest_rangeIntersectMessage() throws Exception {
        assertError(() -> converter.serializeRequest(null, RANGE_INTERSECT_REQUEST),
                "Field value bytes range [0, 4] intersects with another field metadata bytes range [2, 6]");
    }

    @Test
    public void serializeRequestTest_unsupportedType() throws Exception {
        assertError(() -> converter.serializeRequest(null, STRING_VALUE_REQUEST), "Unsupported field type: STRING");
        assertError(() -> converter.serializeRequest(null, FLOAT_VALUE_REQUEST), "Unsupported field type: FLOAT");
        assertError(() -> converter.serializeRequest(null, DOUBLE_VALUE_REQUEST), "Unsupported field type: DOUBLE");
    }

    @Test
    public void serializeRequestTest_wrongIntegerRange() throws Exception {
        assertError(() -> converter.serializeRequest(null, WRONG_INT_RANGE_REQUEST),
                "Integer field value has unsupported size 11. Only sizes in [1, 8] are supported.");
    }

    @Test
    public void serializeRequestTest_wrongLongRange() throws Exception {
        assertError(() -> converter.serializeRequest(null, WRONG_LONG_RANGE_REQUEST),
                "Integer field value has unsupported size 13. Only sizes in [1, 8] are supported.");
    }

    @Test
    public void serializeRequestTest_wrongEnumRange() throws Exception {
        assertError(() -> converter.serializeRequest(null, WRONG_ENUM_RANGE_REQUEST),
                "Enum TestType field type has unsupported size 9. Only sizes in [1, 4] are supported.");
    }

    @Test
    public void serializeRequestTest_wrongBooleanRange() throws Exception {
        assertError(() -> converter.serializeRequest(null, WRONG_BOOLEAN_RANGE_REQUEST),
                "Boolean field release has bytes size = 2, but has to be of size 1");
    }

    @Test
    public void serializeRequestTest_notEnoughRangeForEnum() throws Exception {
        assertError(() -> converter.serializeRequest(null, SMALL_TWO_BYTES_ENUM_RANGE_REQUEST),
                "1 byte(s) not enough for TestTwoBytesSizeEnum enum that has 2222 max number");
        assertError(() -> converter.serializeRequest(null, SMALL_THREE_BYTES_ENUM_RANGE_REQUEST),
                "2 byte(s) not enough for TestThreeBytesSizeEnum enum that has 222222 max number");
        assertError(() -> converter.serializeRequest(null, SMALL_FOUR_BYTES_ENUM_RANGE_REQUEST),
                "3 byte(s) not enough for TestFourBytesSizeEnum enum that has 222222222 max number");
    }

    @Test
    public void deserializeResponseTest() throws Exception {
        assertThat(converterLittleEndian.deserializeResponse(null, TestOptionsImage.getDefaultInstance(), IMAGE_REQUEST_1_BYTES_LITTLE_ENDIAN))
                .isEqualTo(IMAGE_REQUEST_1);
        assertThat(converter.deserializeResponse(null, TestOptionsImage.getDefaultInstance(), IMAGE_REQUEST_1_BYTES_BIG_ENDIAN))
                .isEqualTo(IMAGE_REQUEST_1);
        assertThat(converterLittleEndian.deserializeResponse(null, TestOptionsImage.getDefaultInstance(), IMAGE_REQUEST_2_BYTES_LITTLE_ENDIAN))
                .isEqualTo(IMAGE_REQUEST_2);
        assertThat(converter.deserializeResponse(null, TestOptionsImage.getDefaultInstance(), IMAGE_REQUEST_2_BYTES_BIG_ENDIAN))
                .isEqualTo(IMAGE_REQUEST_2);
    }

    @Test
    public void deserializeResponseTest_emptyMessage() throws Exception {
        assertThat(converter.deserializeResponse(null, TestOptionsImageRequest.getDefaultInstance(), EMPTY_ARRAY))
                .isEqualTo(TestOptionsImageRequest.getDefaultInstance());
    }

    @Test
    public void deserializeResponseTest_wrongMessageByteSize() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsImage.getDefaultInstance(), new byte[10]),
                "Declared size 28 of message TestOptionsImage is not equal to device response size 10");
    }

    @Test
    public void deserializeResponseTest_messageWithFieldAndWithoutByteSize() throws Exception {
        assertError(() -> converter.deserializeResponse(null,
                TestOptionsWithFieldNoBytesRequest.getDefaultInstance(),
                IMAGE_REQUEST_1_BYTES_LITTLE_ENDIAN),
                "A non empty message TestOptionsWithFieldNoBytesRequest doesn't have com.blerpc.message_size annotation.");
    }

    @Test
    public void deserializeResponseTest_noByteRangeMessage() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsNoBytesRangeRequest.getDefaultInstance(), new byte[2]),
                "Proto field value doesn't have com.blerpc.field_bytes annotation");
    }

    @Test
    public void deserializeResponseTest_equalsIndexesMessage() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsEqualsIndexesRequest.getDefaultInstance(), new byte[10]),
                "Field metadata has from_bytes = 0 which must be less than to_bytes = 0");
    }

    @Test
    public void deserializeResponseTest_negativeRangeMessage() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsNegativeRangeRequest.getDefaultInstance(), new byte[10]),
                "Field metadata has from_bytes = -1 which is less than zero");
    }

    @Test
    public void deserializeResponseTest_rangeBiggerThanCountMessage() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsRangeBiggerThanCountRequest.getDefaultInstance(), new byte[10]),
                "Field metadata has to_bytes = 11 which is bigger than message bytes size = 10");
    }

    @Test
    public void deserializeResponseTest_unsupportedType() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsStringValueRequest.getDefaultInstance(), new byte[4]),
                "Unsupported field type: STRING");
        assertError(() -> converter.deserializeResponse(null, TestOptionsFloatValueRequest.getDefaultInstance(), new byte[4]),
                "Unsupported field type: FLOAT");
        assertError(() -> converter.deserializeResponse(null, TestOptionsDoubleValueRequest.getDefaultInstance(), new byte[4]),
                "Unsupported field type: DOUBLE");
    }

    @Test
    public void deserializeResponseTest_wrongIntegerRange() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsWrongIntegerRangeRequest.getDefaultInstance(), new byte[11]),
                "Integer field value has unsupported size 11. Only sizes in [1, 8] are supported.");
    }

    @Test
    public void deserializeResponseTest_wrongLongRange() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsWrongLongRangeRequest.getDefaultInstance(), new byte[13]),
                "Integer field value has unsupported size 13. Only sizes in [1, 8] are supported.");
    }

    @Test
    public void deserializeResponseTest_wrongEnumRange() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsWrongEnumRangeRequest.getDefaultInstance(), new byte[9]),
                "Enum TestType field type has unsupported size 9. Only sizes in [1, 4] are supported.");
    }

    @Test
    public void deserializeResponseTest_wrongBooleanRange() throws Exception {
        assertError(() -> converter.deserializeResponse(null, TestOptionsWrongBooleanRangeRequest.getDefaultInstance(), new byte[2]),
                "Boolean field release has bytes size = 2, but has to be of size 1");
    }
}
