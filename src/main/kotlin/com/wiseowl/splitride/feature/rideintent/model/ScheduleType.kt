package com.wiseowl.splitride.feature.rideintent.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ScheduleType.Immediate::class, name = "IMMEDIATE"),
    JsonSubTypes.Type(value = ScheduleType.Future::class, name = "FUTURE")
)
sealed interface ScheduleType{
    object Immediate: ScheduleType
    data class Future(val startTime: String): ScheduleType
}

@Converter(autoApply = false)
class ScheduleTypeConverter : AttributeConverter<ScheduleType, String> {

    override fun convertToDatabaseColumn(attribute: ScheduleType?): String? {
        return when (attribute) {
            null -> null
            ScheduleType.Immediate -> "Immediate"
            is ScheduleType.Future -> "Future|${attribute.startTime}"
        }
    }

    override fun convertToEntityAttribute(dbData: String?): ScheduleType? {
        if (dbData == null) return null

        return when {
            dbData == "Immediate" ->
                ScheduleType.Immediate

            dbData.startsWith("Future|") -> {
                val time = dbData.substringAfter("|")
                ScheduleType.Future(time)
            }

            else -> throw IllegalArgumentException("Invalid schedule type: $dbData")
        }
    }
}