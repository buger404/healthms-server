package party.para.model

data class CheckoutRequest(
    val reservation : String,
    val praise : Boolean,
    val comment : String
)
