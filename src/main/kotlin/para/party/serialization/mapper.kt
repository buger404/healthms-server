package para.party.serialization

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        findAndRegisterModules()
    }
}