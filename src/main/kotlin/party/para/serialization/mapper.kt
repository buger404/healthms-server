package party.para.serialization

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        findAndRegisterModules()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}