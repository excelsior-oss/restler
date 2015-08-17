package org.restler.integration

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SimpleDto @JsonCreator public constructor(val id: String, val name: String)
