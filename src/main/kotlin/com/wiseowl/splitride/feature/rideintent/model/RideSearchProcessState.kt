package com.wiseowl.splitride.feature.rideintent.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.util.UUID


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RideSearchProcessState.Idle::class, name = "IDLE"),
    JsonSubTypes.Type(value = RideSearchProcessState.Searching::class, name = "SEARCHING"),
    JsonSubTypes.Type(value = RideSearchProcessState.Grouped::class, name = "GROUPED")
)
sealed class RideSearchProcessState{
    object Idle : RideSearchProcessState()
    object Searching : RideSearchProcessState()
    data class Grouped(val groupId: UUID) : RideSearchProcessState()
}


@Converter(autoApply = false)
class RideSearchProcessStateConverter : AttributeConverter<RideSearchProcessState, String> {

    override fun convertToDatabaseColumn(attribute: RideSearchProcessState?): String? {
        return when (attribute) {
            null -> null
            RideSearchProcessState.Idle -> "Idle"
            RideSearchProcessState.Searching -> "Searching"
            is RideSearchProcessState.Grouped -> "Grouped|${attribute.groupId}"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): RideSearchProcessState? {
        if (dbData == null) return null

        return when {
            dbData == "Idle" ->
                RideSearchProcessState.Idle

            dbData == "Searching" ->
                RideSearchProcessState.Searching

            dbData.startsWith("Grouped|") -> {
                val id = dbData.substringAfter("|")
                RideSearchProcessState.Grouped(UUID.fromString(id))
            }

            else -> throw IllegalArgumentException("Invalid RideSearchProcessState type: $dbData")
        }
    }
}