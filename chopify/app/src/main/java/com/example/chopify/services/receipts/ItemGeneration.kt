package com.example.chopify.services.receipts

import com.example.chopify.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig

val outputSchema = Schema(
    name = "groceryItems",
    description = "List of grocery food and drink items",
    type = FunctionType.OBJECT,
    properties = mapOf(
        "grocery_items" to Schema(
            name = "groceryItems",
            description = "List of grocery food and drink items",
            type = FunctionType.ARRAY,
            items = Schema(
                name = "groceryItem",
                description = "A grocery item",
                type = FunctionType.OBJECT,
                properties = mapOf(
                    "name" to Schema(
                        name = "name",
                        description = "Name of the grocery item",
                        type = FunctionType.STRING,
                        nullable = false,
                    ),
                    "quantity" to Schema(
                        name = "quantity",
                        description = "Quantity of the grocery item",
                        type = FunctionType.INTEGER,
                        nullable = true // Optional
                    ),
                    "unit" to Schema(
                        name = "unit",
                        description = "Unit of measurement for the item",
                        type = FunctionType.STRING,
                        enum = listOf(
                            "G", "ML", "PC"
                        ),
                        nullable = true // Optional
                    ),
                    "days_till_expiry" to Schema(
                        name = "days_till_expiry",
                        description = "Estimated number of days until item expires",
                        type = FunctionType.INTEGER,
                        nullable = false // made required to resolve generation issues. non-perishables are dealt with later.
                    )
                ),
                required = listOf("name", "days_till_expiry")
            ),
            nullable = false
        ),
        "date_of_purchase" to Schema(
            name = "date_of_purchase",
            description = "The purchase date of the receipt",
            type = FunctionType.STRING,
            nullable = true // Optional
        )
    ),
    required = listOf("grocery_items")
)

val itemGenModel = GenerativeModel(
    modelName = "gemini-2.0-flash-lite",
    apiKey = BuildConfig.geminiApiKey,
    generationConfig = generationConfig {
        temperature = 1f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 8192
        responseMimeType = "application/json"
        responseSchema = outputSchema
    },
    systemInstruction = content {
        text(
            "Your job is to find grocery items in receipt text and generate a structured output. You are given a string containing raw receipt text. Return a structured output all grocery food and drink items in the string and their associated data. The structured output should include date of purchase listed on the receipt if found. Date of purchase should have format \"MM/DD/YYYY\" if found or \"NULL\" if not found.\n\nEach item must have a name. You may edit abbreviated or contracted item names to be more recognizable.\nEach item may optionally have quantity and unit as well, if found. Quantity should be a whole number. Unit should be one of the given units appropriate for a whole number quantity. Make conversions where they make sense (eg. 0.543 KG = 543 G). The default quantity is 1. The default unit is PC (piece).\nEach item must have days_till_expiry. You should generate this value as a conservative estimate of how many days until the food item expires, assuming it is stored under recommended conditions."
        )
    },
)
