/*
 * Copyright (c) 2023 Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package com.kcrud.admin.env.security.snowflake

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Data class representing the components extracted from a Snowflake ID.
 *
 * @property machineId The unique identifier of the machine or node that generated the Snowflake ID.
 *                     This is used to ensure uniqueness across different machines and is derived from
 *                     a specific portion of the Snowflake ID.
 * @property sequence A sequence number that increments with each ID generated within the same millisecond.
 *                    This is used to ensure uniqueness of IDs generated within the same millisecond.
 *                    If IDs are not generated at a frequency higher than one per millisecond, this value
 *                    will typically be 0.
 * @property utc The UTC timestamp representing the moment the ID was generated,
 *                        providing chronological ordering of IDs.
 * @property local Same as [utc], but converted to the local time zone.
 */
@Serializable
data class SnowflakeData(
    val machineId: Int,
    val sequence: Long,
    val utc: LocalDateTime,
    val local: LocalDateTime
)
