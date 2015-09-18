package de.cpg.oss.verita.service.event_store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import java.io.IOException;
import java.io.Serializable;

public class EventStoreObjectMapper {

    private final ObjectMapper delegate;

    public EventStoreObjectMapper() {
        delegate = new ObjectMapper();

        delegate.registerModule(new JSR310Module());
        delegate.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public String writeValueAsString(final Serializable serializable) throws JsonProcessingException {
        return delegate.writeValueAsString(serializable);
    }

    public <T> T readValue(final String content, final Class<T> valueType) throws IOException {
        return delegate.readValue(content, valueType);
    }
}
