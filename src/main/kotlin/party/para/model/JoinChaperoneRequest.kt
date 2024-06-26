package party.para.model

import java.math.BigDecimal

data class JoinChaperoneRequest(
    var hospital : Int,
    var name: String,
    var startHour : Int,
    var endHour : Int,
    var price : BigDecimal,
    var phone : String
)
