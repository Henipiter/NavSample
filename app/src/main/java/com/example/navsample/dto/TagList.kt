package com.example.navsample.dto

import com.example.navsample.entities.database.Tag

data class TagList(
    var selectedTags: List<Tag>,
    var notSelectedTags: List<Tag>
)
