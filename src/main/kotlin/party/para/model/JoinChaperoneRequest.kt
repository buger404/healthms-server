package party.para.model

data class JoinChaperoneRequest(
    var hospital : Int,
    var name: String,
    var startHour : Int,
    var endHour : Int,
    var price : Float,
    var phone : String
)
